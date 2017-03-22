package guda.mvcx;


import guda.mvcx.ext.freemarker.ExtFreeMarkerEngineImpl;
import guda.mvcx.handle.RouteAction;
import guda.mvcx.session.CookieStoreSessionImpl;
import guda.mvcx.session.DefaultCookieHandlerImpl;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.handler.StaticHandler;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.templ.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Created by well on 2017/3/20.
 */
public class AutoVerticle extends AbstractVerticle {
    private Logger log = LoggerFactory.getLogger(getClass());

    private GuiceBeanFactory guiceBeanFactory;

    public AutoVerticle(GuiceBeanFactory beanFactory) {
        guiceBeanFactory = beanFactory;
    }


    @Override
    public void start() throws Exception {

        Router router = Router.router(vertx);

        router.route().handler(new DefaultCookieHandlerImpl());


        SessionStore store = LocalSessionStore.create(vertx);
        SessionHandler sessionHandler = SessionHandler.create(store);
        router.route().handler(sessionHandler);

        JsonObject cookieConfig = config().getJsonObject("cookie");
        router.route().handler(new CookieStoreSessionImpl(cookieConfig.getString("domain"), cookieConfig.getString("path"),
                cookieConfig.getBoolean("secure"), cookieConfig.getBoolean("httpOnly"), cookieConfig.getLong("maxAge"),
                cookieConfig.getString("sessionKey"), cookieConfig.getString("checkKey"), cookieConfig.getString("encryptSalt")));


        StaticHandler staticHandler = StaticHandler.create();
        staticHandler.setAllowRootFileSystemAccess(true);
        staticHandler.setWebRoot(config().getString("assets.dir"));
        staticHandler.setCachingEnabled(false);
        router.route("/assets/*").handler(staticHandler);

        TemplateEngine engine = new ExtFreeMarkerEngineImpl(config().getString("template.dir"));
        List<RouteAction> routeList = guiceBeanFactory.getRouteActionList();
        routeList.forEach(routeAction -> {
            routeAction.getActionInvokeHandler().setTemplateEngine(engine);
            String requestUri = routeAction.getRequestUri();
            if (requestUri.contains("\\*")) {
                if (routeAction.getHttpMethod() == null) {
                    router.route().pathRegex(requestUri).handler(routeAction.getActionInvokeHandler());
                    if (log.isInfoEnabled()) {
                        log.info("register route:uri[" + requestUri + "]to action[" + routeAction.getActionInvokeHandler().getTargetAction().getClass() + "."
                                + routeAction.getActionInvokeHandler().getTargetMethod().getName() + "]");
                    }
                } else {
                    router.routeWithRegex(routeAction.getHttpMethod(), requestUri).handler(routeAction.getActionInvokeHandler());
                    if (log.isInfoEnabled()) {
                        log.info("register route:uri[" + requestUri + "]method[" + routeAction.getHttpMethod() + "]to action[" + routeAction.getActionInvokeHandler().getTargetAction().getClass() + "."
                                + routeAction.getActionInvokeHandler().getTargetMethod().getName() + "]");
                    }
                }
            } else {
                if (routeAction.getHttpMethod() == null) {
                    router.route(requestUri).handler(routeAction.getActionInvokeHandler());
                    if (log.isInfoEnabled()) {
                        log.info("register route:uri[" + requestUri + "]to action[" + routeAction.getActionInvokeHandler().getTargetAction().getClass() + "."
                                + routeAction.getActionInvokeHandler().getTargetMethod().getName() + "]");
                    }
                } else {
                    router.route(routeAction.getHttpMethod(), requestUri).handler(routeAction.getActionInvokeHandler());
                    if (log.isInfoEnabled()) {
                        log.info("register route:uri[" + requestUri + "]method[" + routeAction.getHttpMethod() + "]to action[" + routeAction.getActionInvokeHandler().getTargetAction().getClass() + "."
                                + routeAction.getActionInvokeHandler().getTargetMethod().getName() + "]");
                    }
                }
            }


        });


        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router::accept).listen(config().getInteger("http.port"), "0.0.0.0");
    }

}