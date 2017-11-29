package io.quasar.core.eventbus.context;

import io.quasar.core.factory.AppBeanFactory;
import io.quasar.core.handle.RouteAction;
import io.quasar.core.handle.RouteRequest;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.Map;

public interface AppContext {

    AppBeanFactory getAppBeanFactory();

    Map<RouteRequest, RouteAction> getFullMatchActionMap();

    List<RouteAction> getPatternRouteActionList();

    List<RouteAction> getAllRouteActionList();


    static AppContext create(JsonObject config) {
        return new AppContextImpl(config);
    }
}
