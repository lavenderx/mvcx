package io.quasar.core.factory;

import java.util.List;

public interface AppBeanFactory extends BeanFactory {

    List<Class> getActionClassList();
}
