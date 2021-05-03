package com.hz.core;

/**
 * @author zehua
 * @date 2021/5/3 16:22
 */
public enum EventTypeEnum {
    // 服务启动事件
    START_EVENT("start"),
    // 服务关闭事件
    STOP_EVENT("stop");

    private String eventName;

    EventTypeEnum(String eventName) {
        this.eventName = eventName;
    }
}
