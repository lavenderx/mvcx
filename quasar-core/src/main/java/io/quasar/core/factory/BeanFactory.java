package io.quasar.core.factory;

public interface BeanFactory {

    <T> T getBean(Class<T> requireType);
}
