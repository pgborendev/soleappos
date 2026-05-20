package com.ury.pos.api;

import android.content.Context;
import android.util.Log;

import com.ury.pos.util.SessionManager;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Cookie;
import okhttp3.CookieJar;
import okhttp3.HttpUrl;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import okio.Buffer;
import okio.BufferedSource;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static Retrofit retrofit;
    private static ApiService apiService;
    private static String currentBaseUrl;

    public static ApiService getService(Context context) {
        String baseUrl = SessionManager.getInstance(context).getBaseUrl();
        if (apiService == null || !baseUrl.equals(currentBaseUrl)) {
            retrofit = buildRetrofit(context, baseUrl);
            apiService = retrofit.create(ApiService.class);
            currentBaseUrl = baseUrl;
        }
        return apiService;
    }

    public static void reset() {
        retrofit = null;
        apiService = null;
        currentBaseUrl = null;
    }

    private static Retrofit buildRetrofit(Context context, String baseUrl) {
        SessionManager session = SessionManager.getInstance(context);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(
                msg -> Log.d("URY_HTTP", msg));
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        Interceptor errorInterceptor = chain -> {
            okhttp3.Request request = chain.request();
            Response response = chain.proceed(request);
            if (!response.isSuccessful()) {
                ResponseBody errorBody = response.peekBody(Long.MAX_VALUE);
                String bodyStr = "";
                try { bodyStr = errorBody.string(); } catch (IOException ignored) {}
                Log.e("URY_API_ERROR",
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━\n" +
                        "URL    : " + request.url() + "\n" +
                        "Method : " + request.method() + "\n" +
                        "Status : " + response.code() + " " + response.message() + "\n" +
                        "Body   :\n" + bodyStr + "\n" +
                        "━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━");
            }
            return response;
        };

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(errorInterceptor)
                .addInterceptor(logging)
                .cookieJar(new CookieJar() {
                    @Override
                    public void saveFromResponse(HttpUrl url, List<Cookie> cookies) {
                        for (Cookie cookie : cookies) {
                            if ("sid".equals(cookie.name())) {
                                session.saveSessionCookie(cookie.value());
                            }
                        }
                    }

                    @Override
                    public List<Cookie> loadForRequest(HttpUrl url) {
                        String sid = session.getSessionCookie();
                        if (sid != null) {
                            Cookie cookie = new Cookie.Builder()
                                    .name("sid")
                                    .value(sid)
                                    .domain(url.host())
                                    .build();
                            return List.of(cookie);
                        }
                        return List.of();
                    }
                })
                .build();

        if (!baseUrl.endsWith("/")) baseUrl = baseUrl + "/";

        return new Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }
}
