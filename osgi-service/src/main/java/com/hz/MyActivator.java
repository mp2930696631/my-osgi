package com.hz;

import com.hz.core.*;

/**
 * @author zehua
 * @date 2021/5/3 16:06
 *
 * MyService接口
 */
public class MyActivator implements BundleActivator, ServiceListener {
    @Override
    public void start(BundleContext context) {
        context.addListener(this);
    }

    @Override
    public void stop(BundleContext context) {
        System.out.println("serviceIf bundle stop...");
    }

    @Override
    public void doListen(Event event) {
        if (event.eventType == EventTypeEnum.START_EVENT) {
            System.out.println("serviceIf bundle start..." + this.getClass().getClassLoader());
        }
    }
}
