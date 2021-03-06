package io.quasar.core.eventbus.context;

import io.quasar.core.annotation.action.Req;
import io.quasar.core.factory.AppBeanFactory;
import io.quasar.core.factory.GuiceBeanFactory;
import io.quasar.core.handle.ActionInvokeHandler;
import io.quasar.core.handle.RouteAction;
import io.quasar.core.handle.RouteRequest;
import io.quasar.core.util.JsonConfigUtil;
import io.quasar.core.util.PatternUtil;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class AppContextImpl implements AppContext {

    private static final Pattern RE_OPERATORS_NO_STAR = Pattern.compile("([\\(\\)\\$\\+\\.])");

    private Map<RouteRequest, RouteAction> fullMatchActionMap = new ConcurrentHashMap<>();
    private List<RouteAction> patternRouteActionList = new ArrayList<>();

    private AppBeanFactory appBeanFactory;

    private static JsonObject contextConfig;

    private List<RouteAction> allRouteActionList = new ArrayList<>();

    public AppContextImpl(JsonObject jsonObject) {
        if (jsonObject == null) {
            return;
        }
        contextConfig = jsonObject;
        appBeanFactory = new GuiceBeanFactory(jsonObject);
        resolveAction(appBeanFactory.getActionClassList());
    }

    public JsonObject getContextConfig() {
        return contextConfig;
    }

    public void setContextConfig(JsonObject contextConfig) {
        this.contextConfig = contextConfig;
    }

    public AppBeanFactory getAppBeanFactory() {
        return appBeanFactory;
    }

    public void setAppBeanFactory(AppBeanFactory appBeanFactory) {
        this.appBeanFactory = appBeanFactory;
    }

    public Map<RouteRequest, RouteAction> getFullMatchActionMap() {
        return fullMatchActionMap;
    }

    public void setFullMatchActionMap(Map<RouteRequest, RouteAction> fullMatchActionMap) {
        this.fullMatchActionMap = fullMatchActionMap;
    }

    public List<RouteAction> getPatternRouteActionList() {
        return patternRouteActionList;
    }

    @Override
    public List<RouteAction> getAllRouteActionList() {
        return allRouteActionList;
    }


    public static JsonObject getConfig() {
        return contextConfig;
    }

    public static String v() {
        if (contextConfig == null) {
            return null;
        }
        return contextConfig.getString(JsonConfigUtil.assetsVersionKey);
    }

    public void setPatternRouteActionList(List<RouteAction> patternRouteActionList) {
        this.patternRouteActionList = patternRouteActionList;
    }

    private void resolveAction(List<Class> clazzList) {
        if (clazzList == null) {
            return;
        }
        clazzList.forEach(clazz -> {
            Object instance = appBeanFactory.getBean(clazz);
            Req actionAnnotation = instance.getClass().getAnnotation(Req.class);
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for (Method method : declaredMethods) {
                Req methodAnno = method.getAnnotation(Req.class);
                if (methodAnno != null) {
                    String path = normalPath(actionAnnotation, methodAnno);
                    ActionInvokeHandler actionInvokeHandler = new ActionInvokeHandler(instance, method);
                    RouteAction routeAction = new RouteAction();
                    if (path.indexOf(":") > -1) {
                        routeAction = createWithPatternRegex(path);
                    } else {
                        routeAction.setRequestUri(path);
                    }
                    routeAction.setActionInvokeHandler(actionInvokeHandler);
                    if (methodAnno.method() != null) {
                        routeAction.setHttpMethod(methodAnno.method());
                    }
                    routeAction.setOriginalPath(path);
                    if (PatternUtil.isPattern(path)) {
                        routeAction.setPattern(Pattern.compile(path));
                        patternRouteActionList.add(routeAction);
                        if (log.isInfoEnabled()) {
                            log.info("register route:uri[" + path + "]to action[" + routeAction.getActionInvokeHandler().getTargetAction().getClass() + "."
                                    + routeAction.getActionInvokeHandler().getTargetMethod().getName() + "]");
                        }
                    } else {
                        if (methodAnno == null) {
                            RouteRequest routeRequest = new RouteRequest(path, HttpMethod.GET);
                            fullMatchActionMap.put(routeRequest, routeAction);

                            routeRequest = new RouteRequest(path, HttpMethod.POST);
                            fullMatchActionMap.put(routeRequest, routeAction);

                            routeRequest = new RouteRequest(path, HttpMethod.HEAD);
                            fullMatchActionMap.put(routeRequest, routeAction);

                            routeRequest = new RouteRequest(path, HttpMethod.DELETE);
                            fullMatchActionMap.put(routeRequest, routeAction);

                            routeRequest = new RouteRequest(path, HttpMethod.PUT);
                            fullMatchActionMap.put(routeRequest, routeAction);

                            routeRequest = new RouteRequest(path, HttpMethod.CONNECT);
                            fullMatchActionMap.put(routeRequest, routeAction);

                            routeRequest = new RouteRequest(path, HttpMethod.OTHER);
                            fullMatchActionMap.put(routeRequest, routeAction);

                            routeRequest = new RouteRequest(path, HttpMethod.TRACE);
                            fullMatchActionMap.put(routeRequest, routeAction);


                            if (log.isInfoEnabled()) {
                                log.info("register route:uri[" + path + "]to action[" + routeAction.getActionInvokeHandler().getTargetAction().getClass() + "."
                                        + routeAction.getActionInvokeHandler().getTargetMethod().getName() + "]");
                            }

                        } else {
                            RouteRequest routeRequest = new RouteRequest(path, methodAnno.method());
                            fullMatchActionMap.put(routeRequest, routeAction);

                            if (log.isInfoEnabled()) {
                                log.info("register route:uri[" + path + "]method:[" + methodAnno.method() + "]to action[" + routeAction.getActionInvokeHandler().getTargetAction().getClass() + "."
                                        + routeAction.getActionInvokeHandler().getTargetMethod().getName() + "]");
                            }
                        }
                    }

                    allRouteActionList.add(routeAction);
                }
            }
        });

    }

    private String normalPath(Req actionAnno, Req methodAnno) {
        String actionPath = "";
        String methodPath = "";
        if (actionAnno != null) {
            actionPath = actionAnno.value();
            if (actionPath.endsWith("/")) {
                actionPath = actionPath.substring(0, actionPath.length() - 1);
            }

            if (!actionPath.startsWith("/")) {
                actionPath = "/" + actionPath;
            }
        }
        if (methodAnno != null) {
            methodPath = methodAnno.value();
            if (actionPath.length() == 0) {
                if (methodPath.startsWith("/")) {
                    return methodPath;
                } else {
                    return "/" + methodPath;
                }
            } else {
                if (methodPath.startsWith("/")) {
                    return actionPath + methodPath;
                } else {
                    return actionPath + "/" + methodPath;
                }
            }

        }
        return actionPath;
    }


    private RouteAction createWithPatternRegex(String path) {
        RouteAction routeAction = new RouteAction();
        path = RE_OPERATORS_NO_STAR.matcher(path).replaceAll("\\\\$1");
        if (path.charAt(path.length() - 1) == 42) {
            path = path.substring(0, path.length() - 1) + ".*";
        }

        Matcher m = Pattern.compile(":([A-Za-z][A-Za-z0-9_]*)").matcher(path);
        StringBuffer sb = new StringBuffer();
        List<String> pathParamName = new ArrayList();

        for (int index = 0; m.find(); ++index) {
            String param = "p" + index;
            String group = m.group().substring(1);
            if (pathParamName.contains(group)) {
                throw new IllegalArgumentException("Cannot use identifier " + group + " more than once in pattern string");
            }

            m.appendReplacement(sb, "(?<" + param + ">[^/]+)");
            pathParamName.add(group);
        }

        m.appendTail(sb);
        path = sb.toString();
        routeAction.setPattern(Pattern.compile(path));
        routeAction.setRequestUri(path);
        routeAction.setPathParamNameList(pathParamName);
        return routeAction;
    }
}
