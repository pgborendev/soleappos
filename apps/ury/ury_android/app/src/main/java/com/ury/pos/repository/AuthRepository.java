package com.ury.pos.repository;

import android.content.Context;

import com.ury.pos.api.ApiClient;
import com.ury.pos.api.ApiService;
import com.ury.pos.api.response.GenericResponse;
import com.ury.pos.api.response.LoginResponse;
import com.ury.pos.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AuthRepository {

    public interface AuthCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }

    private final ApiService api;
    private final SessionManager session;

    public AuthRepository(Context context) {
        this.api = ApiClient.getService(context);
        this.session = SessionManager.getInstance(context);
    }

    public void login(String username, String password, AuthCallback<String> callback) {
        api.login(username, password).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    session.saveUser(response.body().fullName);
                    session.saveUsername(username);
                    callback.onSuccess(response.body().fullName);
                } else {
                    callback.onError("Invalid credentials");
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void logout(AuthCallback<Void> callback) {
        api.logout().enqueue(new Callback<GenericResponse<String>>() {
            @Override
            public void onResponse(Call<GenericResponse<String>> call,
                                   Response<GenericResponse<String>> response) {
                session.clearSession();
                ApiClient.reset();
                callback.onSuccess(null);
            }

            @Override
            public void onFailure(Call<GenericResponse<String>> call, Throwable t) {
                session.clearSession();
                ApiClient.reset();
                callback.onSuccess(null); // still clear session on failure
            }
        });
    }
}
