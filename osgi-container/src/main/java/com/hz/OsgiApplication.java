package com.hz;

import com.hz.container.TerminalCmd;
import com.hz.container.Utils;
import com.hz.container.classLoader.OsgiClassLoader;
import com.hz.container.coreImpl.BundleContextImpl;
import com.hz.core.BundleActivator;
import com.hz.core.BundleContext;
import com.hz.core.Event;
import com.hz.core.EventTypeEnum;

import java.util.Map;

/**
 * @author zehua
 * @date 2021/5/3 14:47
 */
public class OsgiApplication {

    public static void main(String[] args) {
        BundleContext bundleContext = new BundleContextImpl();
        Utils.bundleContext = bundleContext;

        OsgiApplication application = new OsgiApplication();
        Utils.setBundleClassLoaderMap();
        Utils.resolveMetadata(Utils.bundlePathClassLoaderMap);

        application.start();

        TerminalCmd terminal = new TerminalCmd(application);
        terminal.run();
    }

    public void start() {
        final Map<String, ClassLoader> activatorClassLoaderMap = Utils.activatorClassLoaderMap;
        activatorClassLoaderMap.forEach((activatorClassName, classLoader) -> {
            final OsgiClassLoader osgiClassLoader = (OsgiClassLoader) classLoader;
            if (osgiClassLoader.isStatus()) {
                return;
            }
            try {
                final Class<?> aClass = Class.forName(activatorClassName.split(",")[1], true, classLoader);
                final BundleActivator activator = (BundleActivator) aClass.getConstructor().newInstance();
                activator.start(Utils.bundleContext);
                osgiClassLoader.setStatus(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        Utils.multicast.doMulticast(new Event(EventTypeEnum.START_EVENT),Utils.multicast.getStartListenersMap());
    }

}
