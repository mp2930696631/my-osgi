package com.hz.provider;

import com.hz.service.MyService;

/**
 * @author zehua
 * @date 2021/5/3 19:22
 *
 * MyService接口的显示，用于发布该服务
 */
public class MyServiceImpl implements MyService {
    @Override
    public String doService() {
        return "version3.0 provider MyServiceImpl classloader=" + this.getClass().getClassLoader();
    }
}
