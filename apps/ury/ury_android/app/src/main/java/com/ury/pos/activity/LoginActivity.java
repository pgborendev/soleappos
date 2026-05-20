package com.ury.pos.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.ury.pos.R;
import com.ury.pos.util.SessionManager;
import com.ury.pos.viewmodel.LoginViewModel;

public class LoginActivity extends AppCompatActivity {

    private LoginViewModel viewModel;
    private EditText etBaseUrl, etUsername, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Skip to POS if already logged in
        if (SessionManager.getInstance(this).isLoggedIn()) {
            startPOS();
            return;
        }

        setContentView(R.layout.activity_login);

        etBaseUrl   = findViewById(R.id.et_base_url);
        etUsername  = findViewById(R.id.et_username);
        etPassword  = findViewById(R.id.et_password);
        btnLogin    = findViewById(R.id.btn_login);
        progressBar = findViewById(R.id.progress_bar);
        tvError     = findViewById(R.id.tv_error);

        viewModel = new ViewModelProvider(this).get(LoginViewModel.class);
        etBaseUrl.setText(viewModel.getSavedBaseUrl());

        viewModel.loading.observe(this, loading -> {
            progressBar.setVisibility(loading ? View.VISIBLE : View.GONE);
            btnLogin.setEnabled(!loading);
        });

        viewModel.error.observe(this, msg -> {
            if (msg != null) {
                tvError.setVisibility(View.VISIBLE);
                tvError.setText(msg);
            } else {
                tvError.setVisibility(View.GONE);
            }
        });

        viewModel.success.observe(this, ok -> {
            if (Boolean.TRUE.equals(ok)) startPOS();
        });

        btnLogin.setOnClickListener(v -> attemptLogin());
    }

    private void attemptLogin() {
        String url  = etBaseUrl.getText().toString().trim();
        String user = etUsername.getText().toString().trim();
        String pass = etPassword.getText().toString();

        if (TextUtils.isEmpty(url)) {
            etBaseUrl.setError("Enter server URL");
            return;
        }
        if (TextUtils.isEmpty(user)) {
            etUsername.setError("Enter username");
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            etPassword.setError("Enter password");
            return;
        }

        viewModel.saveBaseUrl(url);
        viewModel.login(user, pass);
    }

    private void startPOS() {
        startActivity(new Intent(this, POSActivity.class));
        finish();
    }
}
