package io.quasar.demo.dao;


import io.quasar.demo.dao.model.UserDO;

/**
 * Created by well on 2017/3/20.
 */
public interface UserDAO {

    UserDO getUser(String username);
}
