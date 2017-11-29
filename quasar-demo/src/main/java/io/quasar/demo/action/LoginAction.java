package io.quasar.demo.action;

import com.google.inject.Singleton;
import io.quasar.core.annotation.action.Action;
import io.quasar.core.annotation.action.Req;
import io.quasar.demo.form.LoginForm;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.RoutingContext;

/**
 * Created by well on 2017/3/23.
 */
@Action
@Singleton
public class LoginAction {

    @Req(value = "/login", method = HttpMethod.GET)
    public String login() {
        return "login.ftl";
    }

    @Req(value = "/login", method = HttpMethod.POST)
    public String doLogin(RoutingContext context, LoginForm loginForm) {
        if (loginForm.validateError()) {
            return "login.ftl";
        }
        if ("test".equals(loginForm.getUserName()) && "test".equals(loginForm.getPassword())) {
            return "redirect:/index";
        }

        loginForm.reject("password", "用户名或者密码错误");
        return "login.ftl";
    }
}
