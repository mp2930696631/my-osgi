package com.hz;

import com.hz.core.*;
import com.hz.service.MyService;

/**
 * @author zehua
 * @date 2021/5/3 14:48
 */
public class MyActivator implements BundleActivator, ServiceListener {
    private BundleContext context;

    public void start(BundleContext context) {
        this.context = context;
        context.addListener(this);
    }

    public void stop(BundleContext context) {

    }

    @Override
    public void doListen(Event event) {
        // 监听start事件，并打印classLoader
        if (event.eventType == EventTypeEnum.START_EVENT) {
            final MyService service = context.getService(MyService.class);
            System.out.println(service.doService());
            System.out.println("consumer bundle start..." + this.getClass().getClassLoader());
        }
    }
}
