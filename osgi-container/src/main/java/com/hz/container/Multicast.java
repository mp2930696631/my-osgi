package com.hz.container;

import com.hz.core.Event;
import com.hz.core.ServiceListener;

import java.util.*;

/**
 * @author zehua
 * @date 2021/5/3 16:19
 */
public class Multicast {
    private Map<Class, ServiceListener> startListenersMap = new HashMap<>();
    private Map<Class, ServiceListener> stopListenersMap = new HashMap<>();

    public Map<Class, ServiceListener> getStartListenersMap() {
        return startListenersMap;
    }

    public void setStartListenersMap(Map<Class, ServiceListener> startListenersMap) {
        this.startListenersMap = startListenersMap;
    }

    public Map<Class, ServiceListener> getStopListenersMap() {
        return stopListenersMap;
    }

    public void setStopListenersMap(Map<Class, ServiceListener> stopListenersMap) {
        this.stopListenersMap = stopListenersMap;
    }

    public void doMulticast(Event event, Map<Class, ServiceListener> listenersMap) {
        final Set<Map.Entry<Class, ServiceListener>> entries = listenersMap.entrySet();

        final List<Class> removeKeys = new ArrayList<>();

        entries.forEach(entry -> {
            final Class key = entry.getKey();
            final ServiceListener value = entry.getValue();
            value.doListen(event);
            removeKeys.add(key);
        });

        removeKeys.forEach(clazz -> {
            listenersMap.remove(clazz);
        });
    }

    public void addListener(ServiceListener listener) {
        final Class<? extends ServiceListener> aClass = listener.getClass();
        startListenersMap.put(aClass, listener);
        stopListenersMap.put(aClass, listener);
    }

}
