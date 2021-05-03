package com.hz;

import com.hz.core.*;
import com.hz.provider.MyServiceImpl;
import com.hz.service.MyService;

/**
 * @author zehua
 * @date 2021/5/3 15:54
 *
 * 服务发布者，同时监听了start事件
 */
public class MyActivator implements BundleActivator, ServiceListener {
    @Override
    public void start(BundleContext context) {
        context.addListener(this);

        context.addService(MyService.class, new MyServiceImpl());
    }

    @Override
    public void stop(BundleContext context) {
        System.out.println("provider bundle stop...");
    }

    @Override
    public void doListen(Event event) {
        if (event.eventType == EventTypeEnum.START_EVENT) {
            System.out.println("provider bundle start..." + this.getClass().getClassLoader());
        }
    }
}
