package io.quasar.core;


import io.quasar.core.eventbus.context.AppContext;
import io.quasar.core.ext.freemarker.ExtFreeMarkerEngineImpl;
import io.quasar.core.handle.DefaultFailureHandler;
import io.quasar.core.handle.DefaultNotFoundHandler;
import io.quasar.core.handle.PageAuthCheckHandler;
import io.quasar.core.handle.RouteAction;
import io.quasar.core.session.CookieStoreSessionImpl;
import io.quasar.core.session.DefaultCookieHandlerImpl;
import io.quasar.core.util.JsonConfigUtil;
import io.quasar.core.util.PatternUtil;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.*;
import io.vertx.ext.web.sstore.LocalSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.ext.web.templ.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Created by well on 2017/3/20.
 */
public class AutoVerticle extends AbstractVerticle {

    private Logger log = LoggerFactory.getLogger(getClass());

    private AppContext appContext;

    public AutoVerticle(AppContext context) {
        appContext = context;
    }

    @Override
    public void start() throws Exception {
        Router router = Router.router(vertx);

        router.route("/assets/*").handler(CorsHandler.create("*").allowedMethod(HttpMethod.GET));

        StaticHandler staticHandler = StaticHandler.create();
        staticHandler.setAllowRootFileSystemAccess(true);
        staticHandler.setWebRoot(config().getString("assets.dir"));
        if("env".equals(config().getString(JsonConfigUtil.envKey))||"test".equals(config().getString(JsonConfigUtil.envKey))){
            staticHandler.setCachingEnabled(false);
        }

        router.route("/assets/*").handler(staticHandler);

        router.route().handler(new DefaultCookieHandlerImpl());

        SessionStore store = LocalSessionStore.create(vertx);
        SessionHandler sessionHandler = SessionHandler.create(store);
        router.route().handler(sessionHandler);

        JsonObject cookieConfig = config().getJsonObject(JsonConfigUtil.cookieKey);
        router.route().handler(new CookieStoreSessionImpl(cookieConfig.getString("domain"), cookieConfig.getString("path"),
                cookieConfig.getBoolean("secure"), cookieConfig.getBoolean("httpOnly"), cookieConfig.getLong("maxAge"),
                cookieConfig.getString("sessionKey"), cookieConfig.getString("checkKey"), cookieConfig.getString("encryptSalt"),getCookieExcludePath(cookieConfig)));

        //page auth
        JsonObject authConfig = config().getJsonObject(JsonConfigUtil.authKey);
        if (authConfig!=null&&authConfig.getBoolean(JsonConfigUtil.usePageAuthKey) && authConfig.getString(JsonConfigUtil.pageAuthFailUrlKey) != null) {
            router.route().handler(new PageAuthCheckHandler(authConfig.getString(JsonConfigUtil.pageAuthFailUrlKey),getAuthExcludePath(authConfig)));
        }
        //page auth end
        router.route().handler(BodyHandler.create());

        //必须在CookieStoreSessionImpl，BodyHandler后面执行
        router.route().handler(CSRFHandler.create(UUID.randomUUID().toString()));

        TemplateEngine engine = new ExtFreeMarkerEngineImpl(config());
        List<RouteAction> routeList = appContext.getAllRouteActionList();
        routeList.forEach(routeAction -> {
            routeAction.getActionInvokeHandler().setTemplateEngine(engine);
            String requestUri = routeAction.getOriginalPath();
            if (PatternUtil.isPattern(requestUri)) {
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

        router.route("/*").handler(new DefaultNotFoundHandler(engine, "404.ftl"));
        router.route().failureHandler(new DefaultFailureHandler(engine, "error.ftl"));

        HttpServer server = vertx.createHttpServer();
        server.requestHandler(router::accept).listen(config().getInteger("http.port"));
    }

    private List<String> getCookieExcludePath(JsonObject cookieConfig){
        if(cookieConfig==null){
            return Collections.emptyList();
        }
        JsonArray jsonArray = cookieConfig.getJsonArray(JsonConfigUtil.cookieExcludeKey);
        if(jsonArray==null){
            return Collections.emptyList();
        }
        return jsonArray.getList();

    }

    private List<String> getAuthExcludePath(JsonObject authConfig){
        if(authConfig==null){
            return Collections.emptyList();
        }
        JsonArray jsonArray = authConfig.getJsonArray(JsonConfigUtil.authExcludeKey);
        if(jsonArray==null){
            return Collections.emptyList();
        }
        return jsonArray.getList();

    }

    public static void main(String[] args){
        String s="assets/css/font-awesome-4.7.0/fonts/fontawesome-webfont.woff?v=4.7.0";
        Pattern pattern=Pattern.compile("\\.(eot|ttf|woff|woff2|js|css).*$");
        System.out.println(pattern.matcher(s).find());
    }


}
