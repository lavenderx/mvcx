package io.quasar.demo.biz;

import com.google.inject.ImplementedBy;
import io.quasar.core.annotation.biz.Biz;
import io.quasar.demo.biz.impl.UserServiceImpl;
import io.quasar.demo.dao.model.UserDO;

@Biz
@ImplementedBy(UserServiceImpl.class)
public interface UserService {

    UserDO index();
}
