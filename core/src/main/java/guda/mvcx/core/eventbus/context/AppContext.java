package guda.mvcx.core.eventbus.context;

import guda.mvcx.core.factory.AppBeanFactory;
import guda.mvcx.core.handle.RouteAction;
import guda.mvcx.core.handle.RouteRequest;
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
