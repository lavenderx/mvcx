package io.quasar.demo.biz.impl;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import io.quasar.demo.biz.UserService;
import io.quasar.demo.dao.UserDAO;
import io.quasar.demo.dao.model.UserDO;

@Singleton
public class UserServiceImpl implements UserService {

    @Inject
    private UserDAO userDAO;

    @Override
    public UserDO index() {
        return userDAO.getUser("admin");
    }
}
