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
 *
 * OSGi容器的启动类
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

    /**
     * 在相关准备工作都做完后，进行反射调用各bundle的BundleActivator的start方法
     */
    public void start() {
        final Map<String, ClassLoader> activatorClassLoaderMap = Utils.activatorClassLoaderMap;
        activatorClassLoaderMap.forEach((activatorClassName, classLoader) -> {
            final OsgiClassLoader osgiClassLoader = (OsgiClassLoader) classLoader;
            // 因为在外部输入start命令的时候，也会调用此方法。 每个OsgiClassLoader都有一个是否已经加载的状态，防止重复加载
            if (osgiClassLoader.isStatus()) {
                return;
            }
            try {
                // 反射调用各bundle的BundleActivator的start方法
                final Class<?> aClass = Class.forName(activatorClassName.split(",")[1], true, classLoader);
                final BundleActivator activator = (BundleActivator) aClass.getConstructor().newInstance();
                activator.start(Utils.bundleContext);
                osgiClassLoader.setStatus(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        // 所有的bundle的start方法都已经调用完成，发送start事件
        Utils.multicast.doMulticast(new Event(EventTypeEnum.START_EVENT),Utils.multicast.getStartListenersMap());
    }

}
