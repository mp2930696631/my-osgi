package com.hz.core;

/**
 * @author zehua
 * @date 2021/5/3 14:43
 *
 * bundle的入口类需要实现的接口
 */
public interface BundleActivator {
    void start(BundleContext context);

    // 本程序的stop没有继续实现，（不重要）
    void stop(BundleContext context);
}
