package com.hz.core;

/**
 * @author zehua
 * @date 2021/5/3 14:44
 */
public interface BundleContext {

    void addService(Class<?> clazz, Object impl);

    <T> T getService(Class<T> tClass);

    void addListener(ServiceListener listener);

    void test();
}
