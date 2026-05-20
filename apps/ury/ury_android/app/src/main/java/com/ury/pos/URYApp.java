package com.ury.pos;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.ury.pos.util.SessionManager;

public class URYApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        SessionManager.getInstance(this);
    }
}
