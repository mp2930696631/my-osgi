package com.hz.container;

import com.hz.OsgiApplication;
import com.hz.container.classLoader.OsgiClassLoader;
import com.hz.container.constant.Constant;
import com.hz.container.coreImpl.BundleContextImpl;

import java.net.URL;
import java.util.*;

/**
 * @author zehua
 * @date 2021/5/3 16:33
 * <p>
 * 接受终端输入的线程
 */
public class TerminalCmd {
    private static final String PREFIX = "hz.osgi> ";
    private static final String WELCOME = "welcome~ to osgi";
    private static final String[] commands = new String[]{"start", "stop", "ls", "help"};
    private static final String NOT_SUPPORT_COMMAND_ERROR = "not a support command!!";
    private static final Scanner scan = new Scanner(System.in);

    private OsgiApplication application;

    public TerminalCmd(OsgiApplication application) {
        this.application = application;
    }

    public void run() {
        System.out.println(WELCOME);

        while (true) {
            System.out.print(PREFIX);
            String commandStr = scan.nextLine();
            doCmd(commandStr);
        }
    }

    private void doCmd(String commandStr) {
        final String[] s = commandStr.split(" ");
        switch (s[0]) {
            case "start":
                doStartCmd(s[1]);
                break;
            case "stop":
                doStopCmd(s[1]);
                break;
            case "ls":
                doLsCmd();
                break;
            case "help":
                doHelpCmd();
                break;
            default:
                System.out.println(NOT_SUPPORT_COMMAND_ERROR);
                break;
        }
    }

    /*private boolean isSupportCommand(String command) {
        for (int i = 0; i < commands.length; i++) {
            if (commands[i].equals(command)) {
                return true;
            }
        }

        return false;
    }*/

    private void doStartCmd(String jarPath) {
        try {
            final URL url = new URL(Constant.JAR_URL_PREFIX + jarPath + Constant.JAR_URL_SUFFIX);
            OsgiClassLoader osgiClassLoader = new OsgiClassLoader(url);
            Utils.doResolveMetadata(osgiClassLoader, jarPath);
            Utils.bundlePathClassLoaderMap.put("/" + jarPath, osgiClassLoader);

            application.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doStopCmd(String jarPath) {
        doRemoveEntries(jarPath, Utils.exportPkNameClassLoaderMap);
        doRemoveEntries(jarPath, Utils.activatorClassLoaderMap);

        final BundleContextImpl bundleContext = (BundleContextImpl) Utils.bundleContext;
        bundleContext.removeServiceByClassLoader(Utils.bundlePathClassLoaderMap.get("/" + jarPath));

        Utils.bundlePathClassLoaderMap.remove("/" + jarPath);

        System.gc();
    }

    private void doRemoveEntries(String jarPath, Map<String, ClassLoader> map) {
        final Set<Map.Entry<String, ClassLoader>> exportPkEntries = map.entrySet();
        final List<String> removeKeys = new ArrayList<>();
        exportPkEntries.forEach(entry -> {
            final ClassLoader value = entry.getValue();
            final String key = entry.getKey();
            if (value == Utils.bundlePathClassLoaderMap.get("/" + jarPath)) {
                removeKeys.add(key);
            }
        });

        removeKeys.forEach(str -> {
            map.remove(str);
        });
    }

    private void doLsCmd() {
        final Map<String, ClassLoader> bundlePathClassLoaderMap = Utils.bundlePathClassLoaderMap;
        bundlePathClassLoaderMap.forEach((path, classLoader) -> {
            System.out.println(((OsgiClassLoader) classLoader).getId() + " " + path.substring(1));
        });
    }

    private void doHelpCmd() {
        System.out.println("支持的命令如下：\n" +
                "help: 查看帮助文档\n" +
                "ls: 列出当前系统运行的bundle\n" +
                "start: 格式<start bundle全路径> 启动一个bundle\n" +
                "stop: 格式<stop bundle全路径> 卸载一个bundle");
    }
}
