# 怎么玩

## git clone

```
将程序下载到本地，将每个模块进行打包
注意：现将osgi-core-》osgi-service这两个模块使用mvn install进行安装到本地仓库
```

## jar 包的位置

```
一共有五个模块
osgi-consumer：服务消费者（引用方）
osgi-container：OSGi容器（整个项目的关键）
osgi-core：整个OSGi模块都需要依赖它（类似于org.osgi.core），其他模块只需要引入它既可以进行正常工作了
osgi-provider：服务提供者（发布方）
osgi-service：服务接口

分别打好包后（注意：osgi-core、osgi-service这两个模块是需要使用mvn install进行安装的，其他的倒无所谓）
osgi-container的jar包随便放在什么目录，当选好目录后，在统计目录下创建deploy目录，放置osgi-consumer jar包、osgi-provider jar包、osgi-service jar包，并且将将osgi-container打包生成的lib目录拷贝至osgi-container的jar包所在的目录
```

## 运行

```
本程序支持四个基本的指令：help、ls、start、stop，可以先使用help命令查看帮助

因为是动态模块化的，所以，在不停止程序的情况下，可以动态安装和卸载模块——start安装，stop卸载，但卸载了某模块后，修改代码，重新打包，再安装，既可以看到最新的运行结果！
```

## 注意

```
如果要本地调试的话，首先需要找到osgi-container/com.hz.container.Utils.getDeployPath()根据注释修改即可，而osgi-consumer jar包、osgi-provider jar包、osgi-service jar包的路径就固定死了，需要放在对应的路径下！
```

# OSGi简介

```
OSGi：open service gateway initiative（开放服务网关），是以java为技术平台的动态模块化标准

这里的动态模块化意思是：在同一jvm进程中，可以通过外部命令控制来实现热插拔java模块
```

# OSGi组成

```
简单说就是：OSGi容器+用户编写的模块（bundle）

（网上面的资料说的是模块层+生命周期层+服务层，但是这些我感觉都是OSGi容器提供的功能）

OSGi容器：简单理解就是运行用户自定义模块的地方，我们将编写好的osgi模块放在容器要求的特定的目录下，然后就可以控制在外部通过osgi提供的命令控制该bundle的生命周期，如start、stop、uninstall等（这里的容器就像平时我们所说的tomcat容器一样，只不过这里运行的是bundle，常见的osgi容器例如：felix）
```

# OSGi模块

```
其实就是一个jar包，只不过带有一个设置了OSGi模块元数据的META-INF/MANIFEST.MF文件而已，该文件包含了一些bundle必要的元数据信息，如Import-Package、Export-Package等
```

# OSGi入口

```
这里的入口有两层意思，如果单说OSGi程序的入口，因为是java编写的，当然就是main函数了，但是由于OSGi分为OSGi容器+用户编写的模块，所以，分别讨论下
OSGi容器入口：main函数，这个不用说
bundle入口：实现了BundleActivtor接口的类，并在META-INF/MANIFEST.MF文件中做出了声明
```

# OSGi原理

```
其实他的原理很简单，就是java的类加载机制。在java中，一个class对象的唯一标识=加载该class的ClassLoader+类的完全限定名。所以，为了实现OSGi的动态模块化，只需要使用不同的ClassLoader加载不同的模块就可以了！（说起来很简单，做起来难，通过本程序就晓得了）
```

## 类加载

```
在OSGi中，每一个bundle都有一个与之相对应的ClassLoader，它负责加载该bundle中所包含的所有类（Import-Package该元数据中声明的包中的class除外！）
OSGi中的类加载其实是有特定顺序的！比较复杂，就不粘贴在这了，网上有一大堆，大致如下：
1、java.开头的class由默认的类加载器加载
2、Import-Package声明的包中的类委托给Export-Package该包的类加载器进行加载（委托，其实就是找到对应的classLoader进行加载，由于没有看源码，只能猜一下具体是怎么实现的：OSGi容器中应该有一个map对象保存了包-》classLoader的一个映射，本程序也是这么实现的）
3、在满足一、二两点下，一个bundle对应一个独立的classLoader负责加载本bundle的class
```

## 双亲委派

```
java中，类的查找与加载遵循一个叫“双亲委派”的机制，每一个类加载器中都有一个缓存，在加载一个类的时候，首先查缓存，没有的话，请求父classLoader（只是一个叫parent的属性，并没有继承关系），如果父ClassLoader没有加载完成，本classLoader在真正执行类加载逻辑，每一个classLoader都遵循这相同的逻辑，父classLoader又可能会有父classLoader，所以，类似于递归调用——先向上，然后再函数返回！

在OSGi中，当需要“委派”给其他bundle的类加载器进行加载的时候，并不一定是双亲委派模型（正如上面所说，一个map也可以达到同样的目的）

当然“双亲委派”是可以被打破的，只需要重新定义classLoader的loadClass的逻辑就可以了
```

## 全盘委托

```
我们都知道，java中，类加载是按需加载的，程序入口只有一个（main函数），那么jvm是怎么选择类加载器的？？
这就用到了全盘委托，也就是加载某个类的classLoader是该类所依赖的所有类的classLoader的起始类加载器——假设，classLoaderA加载了AClass，AClass依赖了BClas，那么，jvm在加载BClass的时候，起始的类加载器就是classLoaderA，然后再在classLoaderA的基础上进行双亲委派
（这一点是理解OSGi的关键！！）
```

## 线程上下文类加载器

```
=Thread.currentThread().getContextClassLoader();
可以用来打破双亲委派，根据双亲委派机制和全盘委托机制，会造成类加载的时候，无法逆向加载，也就是在有BootstrapClassLoader加载的类中无法依赖classpath下的类！（SPI就需要这么干，因为SPI的实现就是在classPath下的，需要逆行）

public static Class<?> forName(String name, boolean initialize, ClassLoader loader)的第三个参数可以指定类加载器，所以，我们可以根据需要进行类加载，其实并不一定需要线程上下文类加载器，只需要方便获取一个类加载器就可以了，但是，就像ThreadLocal一样，他们可以很方便的在同一线程不同的地方传递参数而已
```

# OSGi的本质

```
其实就是classLoader隔离，也就是在java原先的包可见性的隔离机制上加上了classLoader隔离
```

# OSGi原理的实现

```
本程序就是根据上面的知识实现了一个精简版的OSGi程序，它包括OSGi容器+bundle
OSGi容器：osgi-container
osgi-core是所有模块共同的依赖，也算是osgi-container的东西，只不过抽离出来了，它并不是一个bundle！！
其他的都是bundle

其实，关键的一个类就是osgi-container下的com.hz.container.classLoader.OsgiClassLoader（然而，这个类加载器又写得特别简单易懂）
```

## 注意

```
1、本程序的bundle与标准的OSGi bundle有点不同，本程序的bundle的元数据放在了resources/application.properties中，但是原理是一样的

2、如果要本地调试的话，首先需要找到osgi-container/com.hz.container.Utils.getDeployPath()根据注释修改即可，然后在osgi-container模块的main函数中打一个断点即可
```

## 类加载的实现

```
其实我就是建立了一个映射关系，一个map（osgi-container/com.hz.container.Utils.bundlePathClassLoaderMap属性），key为bundle jar包所对应的路径，value为加载这个bundle的classLoader（OsgiClassLoader，所有的bundle的类加载器都是这个类的实例，该类继承自URLClassLoader）
```

## 关键点

```
有一个关键点：
什么叫一个bundle对应一个classLoader？
--其实就是使用Class.forName指定classLoader反射调用BundleActivator的start方法！！这时，由于入口使用了OsgiClassLoader，所以，接下来，根据“全盘委托”机制，该bundle所有的class的加载时的起点classLoader就是该OsgiClassLoader——这个就是“什么叫一个bundle对应一个classLoader”
```

## 服务的发布与引用

```
看起来很神奇，其实就是讲服务保存在了BundleContext的实现类中的一个成员变量里面了（map对象）。。。
```

# NoClassDefFoundError

```
这个算是OSGi中最常见的异常了。。。。

原因其实很简单：就是相应的类既没有被默认的三个类加载器加载到（也就是不在classpath下，可能是maven打包的时候并没有将依赖的jar包打入），也没有被自定义的osgiClassLoader加载到。

由于OSGi终究还是在同一个jvm中，所以，对于公共的class，只需要保证在进行“双亲委派”的时候，被同一个classLoader加载到就可以了，所以，公共的依赖只需要放在OSGi容器下，使用AppClassLoader进行加载！（也就是mavn的依赖），并且保证当容器被打成jar包的时候，可以访问到这些依赖即可。而其他的bundle如果需要使用这些公共依赖，只需要在pom文件找中引入依赖，并不需要将依赖打入jar包了！！

（由于程序是由OSGi容器启动的，所以，classpath指的是与OSGi容器相关的路径，而与其他的bundle没有任何关系）

根据上面的原理，出现这样的错误，要么是OSGi容器中没有引入，要么，在是该类没有在相应的Import-Package、Export-Package中声明
```

