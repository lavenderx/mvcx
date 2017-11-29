package io.quasar.core.handle;


import io.quasar.core.auth.PageAuthService;
import io.quasar.core.auth.security.AuthUser;
import io.vertx.core.Handler;
import io.vertx.ext.web.RoutingContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by well on 2017/3/22.
 */
public class PageAuthCheckHandler implements Handler<RoutingContext> {

    private Logger log = LoggerFactory.getLogger(PageAuthCheckHandler.class);
    private String authFailUrl;

    private PageAuthService pageAuthService = new PageAuthService();

    private List<String> excludePathList;

    public PageAuthCheckHandler(String failUrl, List<String> excludePath) {
        authFailUrl = failUrl;
        excludePathList = excludePath;
    }


    @Override
    public void handle(RoutingContext context) {
        String path = context.request().path();
        if (ignorePath(path)) {
            context.next();
            return;
        }
        AuthUser authUser = (AuthUser) context.session().data().get(AuthUser.sessionKey);
        if (authUser == null) {
            authUser=new AuthUser();
            authUser.setLoginName("anonymous");
            authUser.setRoles(new String[]{"anonymous"});
        }

        AuthUser.setCurrentUser(authUser);
        boolean result = pageAuthService.isAllow(path, authUser.getUserName(), authUser.getRoles());
        if (result) {
            context.next();
            return;
        }
        context.response().putHeader("location", authFailUrl).setStatusCode(302).end();


    }

    private boolean ignorePath(String path) {
        if (excludePathList == null || path == null) {
            return false;
        }
        for (String s : excludePathList) {
            if (path.startsWith(s)) {
                return true;
            }
        }
        return false;


    }
}
