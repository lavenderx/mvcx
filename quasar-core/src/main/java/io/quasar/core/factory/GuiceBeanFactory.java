package io.quasar.core.factory;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;
import io.quasar.core.annotation.action.Action;
import io.quasar.core.annotation.biz.Biz;
import io.vertx.core.json.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.guice.XMLMyBatisModule;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
public class GuiceBeanFactory implements AppBeanFactory {

    private final List<Class> actionClassList = new ArrayList<>();
    private final JsonObject config;
    private volatile Injector injector;

    public GuiceBeanFactory(JsonObject jsonObject) {
        this.config = jsonObject;
        setupGuice();

    }

    public void setupGuice() {
        JsonObject dbConfig = config.getJsonObject("db");
        List<Module> moduleList = new ArrayList<>();
        if (dbConfig != null) {
            Module mybatisModule = createMybatisModule(dbConfig);
            if (mybatisModule != null) {
                moduleList.add(mybatisModule);
            }
        }
        String bizPackage = config.getString("biz.package");
        Module bizModule = createBizModule(bizPackage);
        if (bizModule != null) {
            moduleList.add(bizModule);
        }

        String actionPackage = config.getString("action.package");
        Module actionModule = createActionModule(actionPackage);
        if (actionModule != null) {
            moduleList.add(actionModule);
        }

        injector = Guice.createInjector(Stage.PRODUCTION, moduleList);
    }

    private Module createMybatisModule(JsonObject dbConfig) {
        if (dbConfig == null) {
            return null;
        }
        return new XMLMyBatisModule() {
            @Override
            protected void initialize() {
                setEnvironmentId(dbConfig.getString("environment.id"));
            }

        };
    }

    private Module createBizModule(String bizPackage) {
        if (bizPackage == null) {
            return null;
        }
        return new AbstractModule() {
            @Override
            protected void configure() {
                String[] packages = bizPackage.split(",");
                for (String _package : packages) {
                    try {
                        Reflections reflections = new Reflections(_package);
                        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Biz.class, true);
                        classes.forEach(clazz -> {
                            if (log.isInfoEnabled()) {
                                log.info("bind class:" + clazz.getName());
                            }
                            bind(clazz);
                        });
                    } catch (Exception e) {
                        log.error("Can not add biz classes", e);
                        throw new UnsupportedOperationException(e);
                    }
                }
            }
        };
    }

    private Module createActionModule(String actionPackage) {
        if (actionPackage == null) {
            return null;
        }
        return new AbstractModule() {
            @Override
            protected void configure() {
                String[] packages = actionPackage.split(",");
                for (String _package : packages) {
                    try {
                        Reflections reflections = new Reflections(_package);
                        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(Action.class, true);
                        classes.forEach(clazz -> {
                            if (log.isInfoEnabled()) {
                                log.info("bind class:" + clazz.getName());
                            }
                            bind(clazz);
                            actionClassList.add(clazz);
                        });
                    } catch (Exception e) {
                        log.error("Can not add action classes", e);
                        throw new UnsupportedOperationException(e);
                    }
                }
            }
        };
    }


    public Injector getInjector() {
        return injector;
    }


    @Override
    public <T> T getBean(Class<T> requireType) {
        if (injector == null) {
            return null;
        }
        return injector.getInstance(requireType);
    }


    @Override
    public List<Class> getActionClassList() {
        return actionClassList;
    }
}