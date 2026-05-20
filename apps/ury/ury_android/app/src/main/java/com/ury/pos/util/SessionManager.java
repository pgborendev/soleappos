package com.ury.pos.util;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {

    private static final String PREF_NAME    = "ury_pos_prefs";
    private static final String KEY_BASE_URL = "base_url";
    private static final String KEY_SID      = "session_sid";
    private static final String KEY_USER     = "logged_user";
    private static final String KEY_USERNAME = "username";

    private static SessionManager instance;
    private final SharedPreferences prefs;

    private SessionManager(Context context) {
        prefs = context.getApplicationContext()
                       .getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) instance = new SessionManager(context);
        return instance;
    }

    // ── Base URL ──────────────────────────────────────────────────────────────

    public void saveBaseUrl(String url) {
        if (!url.endsWith("/")) url = url + "/";
        prefs.edit().putString(KEY_BASE_URL, url).apply();
    }

    public String getBaseUrl() {
        return prefs.getString(KEY_BASE_URL, "http://localhost:8000/");
    }

    // ── Session cookie ────────────────────────────────────────────────────────

    public void saveSessionCookie(String sid) {
        prefs.edit().putString(KEY_SID, sid).apply();
    }

    public String getSessionCookie() {
        return prefs.getString(KEY_SID, null);
    }

    // ── User ──────────────────────────────────────────────────────────────────

    public void saveUser(String user) {
        prefs.edit().putString(KEY_USER, user).apply();
    }

    public String getUser() {
        return prefs.getString(KEY_USER, null);
    }

    public void saveUsername(String username) {
        prefs.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getUsername() {
        return prefs.getString(KEY_USERNAME, null);
    }

    // ── Session state ─────────────────────────────────────────────────────────

    public boolean isLoggedIn() {
        String sid = getSessionCookie();
        return sid != null && !sid.isEmpty() && !"Guest".equals(sid);
    }

    public void clearSession() {
        prefs.edit()
             .remove(KEY_SID)
             .remove(KEY_USER)
             .apply();
    }

    public void clearAll() {
        prefs.edit().clear().apply();
    }
}
