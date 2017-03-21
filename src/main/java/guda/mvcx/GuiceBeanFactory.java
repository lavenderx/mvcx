package guda.mvcx;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.name.Names;
import guda.mvcx.annotation.action.Action;
import guda.mvcx.annotation.action.Req;
import guda.mvcx.annotation.biz.Biz;
import guda.mvcx.annotation.dao.DAO;
import guda.mvcx.handle.ActionInvokeHandler;
import guda.mvcx.handle.RouteAction;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import org.apache.ibatis.transaction.jdbc.JdbcTransactionFactory;
import org.mybatis.guice.MyBatisModule;
import org.mybatis.guice.datasource.druid.DruidDataSourceProvider;
import org.mybatis.guice.datasource.helper.JdbcHelper;
import org.reflections.Reflections;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;

/**
 * Created by well on 2017/3/20.
 */
public class GuiceBeanFactory {

    private JsonObject config;
    private Injector injector;
    private List<RouteAction> actionList = new ArrayList<>();
    private List<Class> actionClassList = new ArrayList<>();


    public GuiceBeanFactory(JsonObject jsonObject) {
        config = jsonObject;
        setupGuice();
        actionList = resolveAction(actionClassList);
    }


    public DeploymentOptions readOpts() {
        final DeploymentOptions options = new DeploymentOptions();
        options.setHa(false);
        options.setInstances(1);
        options.setWorker(false);
        options.setMultiThreaded(false);
        return options;

    }

    public void setupGuice() {
        Properties myBatisProperties = new Properties();
        JsonObject dbConfig = config.getJsonObject("db");
        myBatisProperties.setProperty("mybatis.environment.id", dbConfig.getString("environment.id"));
        myBatisProperties.setProperty("JDBC.driverClassName", dbConfig.getString("driverClassName"));
        myBatisProperties.setProperty("JDBC.host", dbConfig.getString("host"));
        myBatisProperties.setProperty("JDBC.port", dbConfig.getString("port"));
        myBatisProperties.setProperty("JDBC.schema", dbConfig.getString("database"));
        myBatisProperties.setProperty("JDBC.username", dbConfig.getString("username"));
        myBatisProperties.setProperty("JDBC.password", dbConfig.getString("password"));
        myBatisProperties.setProperty("JDBC.autoCommit", dbConfig.getString("autoCommit"));

        injector = Guice.createInjector(new MyBatisModule() {
                                            @Override
                                            protected void initialize() {
                                                install(JdbcHelper.MySQL);
                                                bindDataSourceProviderType(DruidDataSourceProvider.class);
                                                bindTransactionFactoryType(JdbcTransactionFactory.class);
                                                Names.bindProperties(binder(), myBatisProperties);
                                                addMapperClasses();

                                                String bizPackage = config.getString("biz.package");
                                                if (bizPackage != null) {
                                                    String[] ps = bizPackage.split(",");
                                                    for (String s : ps) {
                                                        try {
                                                            Reflections reflections = new Reflections(s);
                                                            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Biz.class);
                                                            classes.forEach(clazz -> {
                                                                bind(clazz);
                                                            });
                                                        } catch (Throwable e) {
                                                            throw new UnsupportedOperationException("can't add biz classes");
                                                        }
                                                    }

                                                }
                                                String actionPackage = config.getString("action.package");
                                                if (actionPackage != null) {
                                                    String[] ps = actionPackage.split(",");
                                                    for (String s : ps) {
                                                        try {
                                                            Reflections reflections = new Reflections(s);
                                                            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Action.class);
                                                            classes.forEach(clazz -> {
                                                                actionClassList.add(clazz);
                                                                bind(clazz);
                                                            });


                                                        } catch (Throwable e) {
                                                            throw new UnsupportedOperationException("can't add action classes");
                                                        }
                                                    }
                                                }


                                            }


                                            private void addMapperClasses() {
                                                String daoPackage = config.getString("dao.package");
                                                if (daoPackage != null) {
                                                    String[] ps = daoPackage.split(",");
                                                    for (String s : ps) {
                                                        try {
                                                            Reflections reflections = new Reflections(s);
                                                            Set<Class<?>> classes = reflections.getTypesAnnotatedWith(DAO.class);
                                                            addMapperClasses(classes);
                                                        } catch (Throwable e) {
                                                            throw new UnsupportedOperationException("can't add dao classes");
                                                        }
                                                    }
                                                }

                                            }
                                        }
        );
    }


    private List<RouteAction> resolveAction(List<Class> clazzList) {
        if (clazzList == null) {
            return null;
        }
        List<RouteAction> routeActions = new ArrayList<>(clazzList.size());
        clazzList.forEach(clazz -> {
            Object instance = injector.getInstance(clazz);
            Req actionAnnotation = instance.getClass().getAnnotation(Req.class);
            Method[] declaredMethods = clazz.getDeclaredMethods();
            for (Method method : declaredMethods) {
                Req methodAnno = method.getAnnotation(Req.class);
                if (methodAnno != null) {
                    String path = normalPath(actionAnnotation, methodAnno);
                    ActionInvokeHandler actionInvokeHandler = new ActionInvokeHandler(instance, method);
                    RouteAction routeAction = new RouteAction();
                    routeAction.setRequestUri(path);
                    routeAction.setActionInvokeHandler(actionInvokeHandler);
                    if (methodAnno.method() != null) {
                        routeAction.setHttpMethod(methodAnno.method());
                    }

                    routeActions.add(routeAction);
                }
            }
        });
        return routeActions;
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

    public Injector getInjector() {
        return injector;
    }

    public List<RouteAction> getRouteList() {
        return actionList;
    }

    public void start() {

    }
}
