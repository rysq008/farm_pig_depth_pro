package com.farm.innovation.biz.login;

public interface ILoginView {

    void onAuthenticate();

    int onLoginError(String message);

    void onLoginSuccess(String message);

    void onLogin();
}
