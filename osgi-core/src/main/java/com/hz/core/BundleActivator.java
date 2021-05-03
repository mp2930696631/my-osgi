package com.hz.core;

/**
 * @author zehua
 * @date 2021/5/3 14:43
 */
public interface BundleActivator {
    void start(BundleContext context);

    void stop(BundleContext context);
}
