package com.hz.core;

/**
 * @author zehua
 * @date 2021/5/3 14:44
 */
public interface BundleContext {

    // 添加服务
    void addService(Class<?> clazz, Object impl);

    // 获取服务
    <T> T getService(Class<T> tClass);

    // 添加监听
    void addListener(ServiceListener listener);

    void test();
}
