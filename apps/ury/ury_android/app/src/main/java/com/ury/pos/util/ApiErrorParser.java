package com.ury.pos.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import retrofit2.Response;

public class ApiErrorParser {

    public static String parse(Response<?> response) {
        try {
            String raw = response.errorBody() != null ? response.errorBody().string() : "";
            Log.e("URY_API_ERROR", "HTTP " + response.code() + " " + response.message()
                    + "\n" + raw);

            if (raw.isEmpty()) return "Server error " + response.code();

            JSONObject json = new JSONObject(raw);

            // Frappe server_messages contain user-facing messages
            if (json.has("_server_messages")) {
                String serverMessages = json.getString("_server_messages");
                JSONArray arr = new JSONArray(serverMessages);
                if (arr.length() > 0) {
                    JSONObject msg = new JSONObject(arr.getString(0));
                    if (msg.has("message")) return msg.getString("message");
                }
            }

            // Frappe exception message
            if (json.has("exception")) return json.getString("exception");
            if (json.has("message"))   return json.getString("message");
            if (json.has("exc_type"))  return json.getString("exc_type");

            return "Server error " + response.code();
        } catch (Exception e) {
            return "Server error " + response.code();
        }
    }
}
