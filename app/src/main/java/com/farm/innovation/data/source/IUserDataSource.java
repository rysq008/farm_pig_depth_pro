package com.farm.innovation.data.source;

import com.farm.innovation.model.user.User;

public interface IUserDataSource {

    void saveUser(User user);

    User getUser(int uId);
}
