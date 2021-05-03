package com.hz.container.coreImpl;

import com.hz.container.Utils;
import com.hz.core.BundleContext;
import com.hz.core.ServiceListener;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author zehua
 * @date 2021/5/3 15:48
 */
public class BundleContextImpl implements BundleContext {
    private Map<ClassLoader, Map<Class<?>, Object>> map = new HashMap<>();

    @Override
    public void addService(Class<?> clazz, Object impl) {
        final ClassLoader classLoader = impl.getClass().getClassLoader();
        Map<Class<?>, Object> classObjectMap = map.get(classLoader);
        if (classObjectMap == null) {
            classObjectMap = new HashMap<>();
            map.put(classLoader, classObjectMap);
        }
        classObjectMap.put(clazz, impl);
    }

    @Override
    public <T> T getService(Class<T> tClass) {
        final Set<Map.Entry<ClassLoader, Map<Class<?>, Object>>> entries = map.entrySet();

        for (Map.Entry<ClassLoader, Map<Class<?>, Object>> entry : entries) {
            final Map<Class<?>, Object> value = entry.getValue();
            if (value.get(tClass) != null) {
                return (T) value.get(tClass);
            }
        }

        return null;
    }

    @Override
    public void addListener(ServiceListener listener) {
        Utils.multicast.addListener(listener);
    }

    public void removeServiceByClassLoader(ClassLoader classLoader) {
        map.remove(classLoader);
    }

    @Override
    public void test() {
        System.out.println("bundlecontext classLoader=" + BundleContext.class.getClassLoader());
    }
}
