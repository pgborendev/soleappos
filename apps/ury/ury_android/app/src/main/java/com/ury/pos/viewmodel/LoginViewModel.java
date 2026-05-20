package com.ury.pos.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;

import com.ury.pos.repository.AuthRepository;
import com.ury.pos.util.SessionManager;

public class LoginViewModel extends AndroidViewModel {

    public final MutableLiveData<Boolean>  loading = new MutableLiveData<>(false);
    public final MutableLiveData<String>   error   = new MutableLiveData<>();
    public final MutableLiveData<Boolean>  success = new MutableLiveData<>();

    private final AuthRepository repo;
    private final SessionManager session;

    public LoginViewModel(@NonNull Application app) {
        super(app);
        repo    = new AuthRepository(app);
        session = SessionManager.getInstance(app);
    }

    public void saveBaseUrl(String url) {
        session.saveBaseUrl(url);
        AuthRepository.class.cast(repo); // reset client when URL changes
        com.ury.pos.api.ApiClient.reset();
    }

    public String getSavedBaseUrl() {
        return session.getBaseUrl();
    }

    public void login(String username, String password) {
        loading.setValue(true);
        error.setValue(null);
        repo.login(username, password, new AuthRepository.AuthCallback<String>() {
            @Override
            public void onSuccess(String result) {
                loading.postValue(false);
                success.postValue(true);
            }

            @Override
            public void onError(String message) {
                loading.postValue(false);
                error.postValue(message);
            }
        });
    }
}
