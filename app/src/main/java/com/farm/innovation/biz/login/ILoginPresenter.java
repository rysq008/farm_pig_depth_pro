package com.farm.innovation.biz.login;

import com.farm.innovation.model.user.User;

public interface ILoginPresenter {
    void login(User user);
    void showVersonNumber();
    void startRegister();
}
