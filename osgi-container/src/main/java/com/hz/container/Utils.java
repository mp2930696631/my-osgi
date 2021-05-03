package com.hz.container;

import com.hz.container.classLoader.OsgiClassLoader;
import com.hz.container.constant.Constant;
import com.hz.container.entity.MetadataObj;
import com.hz.core.BundleContext;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author zehua
 * @date 2021/5/3 14:53
 *
 * 大杂烩。。工具包
 */
public class Utils {

    // 用于保存bundle jar路径-》classLoader的映射
    public static Map<String, ClassLoader> bundlePathClassLoaderMap = new HashMap<>();

    // 每个bundle的application.properties可能有export.package属性，这个map对象用于保存packageName-》classLoader的映射，方便根据包名获取对应的ClassLoader
    public static Map<String, ClassLoader> exportPkNameClassLoaderMap = new HashMap<>();

    // activator标识-》classLoader的映射，方便进行activator反射调用（activator标识=jar包的名称+,+activator 类的完全限定名）
    public static Map<String, ClassLoader> activatorClassLoaderMap = new HashMap<>();

    // 保存context对象
    public static BundleContext bundleContext;

    // 多播器
    public static Multicast multicast = new Multicast();

    // 自增
    private static AtomicInteger atomicInteger = new AtomicInteger();

    // 为每个osgiClassLoader都分配一个id
    public static int getId() {
        return atomicInteger.incrementAndGet();
    }

    // 获取jar包所在的路径
    public static String getDeployPath() {
        final String path =
                Utils.class.getResource("").getPath();
        final int i = path.lastIndexOf(Constant.MAIN_JAR_NAME);

        return path.substring(Constant.PREFIX.length(), i) + Constant.DEPLOY + "/";

        // idea中运行使用这个
        // return "F:/zehua/test/deploy/";
    }

    // 获取某路径下的所有jar包的URL
    public static URL[] findResourceURLs(String baseDir) throws MalformedURLException {
        File file = new File(baseDir);
        URL[] urls = null;
        if (file.isDirectory()) {
            final File[] files = file.listFiles((File dir, String name) -> name.endsWith(".jar"));
            urls = new URL[files.length];
            for (int i = 0; i < files.length; i++) {
                urls[i] = files[i].toURI().toURL();
            }
        }

        return urls;
    }

    // 获取某路径下面的所有jar包的绝对路径
    public static String[] getJarsAbsPath(String baseDir) {
        File file = new File(baseDir);
        String[] jarsPath = null;
        if (file.isDirectory()) {
            final File[] files = file.listFiles((File dir, String name) -> name.endsWith(".jar"));
            jarsPath = new String[files.length];
            for (int i = 0; i < files.length; i++) {
                jarsPath[i] = files[i].getAbsolutePath();
            }
        }

        return jarsPath;
    }

    // 构建Utils.bundlePathClassLoaderMap
    public static void setBundleClassLoaderMap() {
        final String deployPath = getDeployPath();
        System.out.println(deployPath);
        try {
            final URL[] resourceURLs = findResourceURLs(deployPath);

            for (int i = 0; i < resourceURLs.length; i++) {
                final String path = resourceURLs[i].getPath();
                bundlePathClassLoaderMap.put(path, new OsgiClassLoader(resourceURLs[i]));
            }

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    // 解析bundle的resources/META_INF/MANIFEST.MF文件
    public static void resolveMetadata(Map<String, ClassLoader> bundleClassLoaderMap) {
        final String deployPath = getDeployPath();


        try {
            final URL[] resourceURLs = findResourceURLs(deployPath);

            for (int i = 0; i < resourceURLs.length; i++) {
                String jarPath = resourceURLs[i].getPath().substring(1);
                final OsgiClassLoader classLoader = (OsgiClassLoader) bundleClassLoaderMap.get(resourceURLs[i].getPath());

                doResolveMetadata(classLoader, jarPath);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // 解析bundle的resources/META_INF/MANIFEST.MF文件
    public static void doResolveMetadata(OsgiClassLoader osgiClassLoader, String jarPath) throws IOException {
        JarFile jarFile = new JarFile(jarPath);

        final Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            final JarEntry jarEntry = entries.nextElement();
            jarEntry.clone();
            if (jarEntry.getName().endsWith(Constant.APPLICATION_PROPERTIES)) {
                final InputStream inputStream = jarFile.getInputStream(jarEntry);
                Properties properties = new Properties();
                properties.load(inputStream);
                inputStream.close();

                MetadataObj metadataObj = new MetadataObj(jarPath);

                //  import.package解析
                final String importPk = properties.getProperty(Constant.IMPORT_PACKAGE);
                if (importPk != null) {
                    final String[] importPks = importPk.split(",");
                    final ArrayList<String> importPkList = arrayToList(importPks);
                    metadataObj.setImportPkNames(importPkList);
                }

                // export.package解析
                final String exportPk = properties.getProperty(Constant.EXPORT_PACKAGE);
                if (exportPk != null) {
                    final String[] exportPks = exportPk.split(",");
                    final ArrayList<String> exportPkList = arrayToList(exportPks);
                    metadataObj.setExportPkName(exportPkList);

                    for (int i = 0; i < exportPks.length; i++) {
                        exportPkNameClassLoaderMap.put(exportPks[i], osgiClassLoader);
                    }
                }

                osgiClassLoader.setMetadataObj(metadataObj);

                // activator.class解析
                final String activatorClassName = properties.getProperty(Constant.ACTIVATOR_CLASS);
                String jarName = jarPath.substring(jarPath.lastIndexOf("/") + 1);
                activatorClassLoaderMap.put(jarName + "," + activatorClassName, osgiClassLoader);

                break;
            }
        }
        jarFile.close();
    }

    private static ArrayList<String> arrayToList(String[] strs) {
        ArrayList<String> al = new ArrayList<>();
        for (int i = 0; i < strs.length; i++) {
            al.add(strs[i]);
        }

        return al;
    }
}
