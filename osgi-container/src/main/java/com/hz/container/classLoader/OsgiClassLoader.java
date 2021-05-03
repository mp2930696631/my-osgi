package com.hz.container.classLoader;

import com.hz.container.Utils;
import com.hz.container.entity.MetadataObj;

import java.net.URL;
import java.net.URLClassLoader;

/**
 * @author zehua
 * @date 2021/5/3 14:58
 */
public class OsgiClassLoader extends URLClassLoader {
    private MetadataObj metadataObj;
    private int id;
    private boolean status;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public OsgiClassLoader(URL url) {
        super(new URL[]{url});
        id = Utils.getId();
    }

    public MetadataObj getMetadataObj() {
        return metadataObj;
    }

    public void setMetadataObj(MetadataObj metadataObj) {
        this.metadataObj = metadataObj;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {

        // 使用默认的三个类加载器进行加载
        if (name.startsWith("java.") || name.startsWith("com.hz.core") || name.startsWith("com.hz.container")) {
            return super.loadClass(name);
        }

        // 获取包名
        final String pkName = name.substring(0, name.lastIndexOf("."));
        // 获取导出了该包的bundle对应的classloader
        final ClassLoader classLoader = Utils.exportPkNameClassLoaderMap.get(pkName);

        // 如果是本bundle导出的，则直接使用本classLoader进行加载
        if (classLoader == this) {
            final boolean contains = metadataObj.getExportPkName().contains(pkName);
            if (contains) {
                return super.loadClass(name);
            }

            System.out.println("...error..");
        } else if (classLoader == null) {
            return super.loadClass(name);
        } else {
            return classLoader.loadClass(name);
        }

        return null;

        //上面的代码逻辑实际上等同于下面这段代码
        // 获取包名
        /*final String pkName = name.substring(0, name.lastIndexOf("."));
        // 获取导出了该包的bundle对应的classloader
        final ClassLoader classLoader = Utils.exportPkNameClassLoaderMap.get(pkName);
        if (classLoader==null||classLoader==this){
            return super.loadClass(name);
        }else {
            return classLoader.loadClass(name);
        }*/
    }

    @Override
    public String toString() {
        return "[" + id + "]";
    }
}
