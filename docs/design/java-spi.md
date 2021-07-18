# Java SPI
---

# Overview

一个可扩展的应用程序一般要求我们可以不改动程序的源代码而只是通过实现特定接口并提供插件就能对源程序进行各种功能扩展。而从 Java6 开始 JDK 引入了一个可以发现和加载特定接口的实现类的一种机制，是一套用来被第三方实现或者扩展的API，使用它就可以很方便的实现框架扩展和替换组件。

在面向对象中我们推荐基于接口编程，模块之间基于接口交互，这样的好处显而易见，不需要在代码中进行具体实现的硬编码，而让不同的实现者按照接口规范实现自己内部操作，然后在使用的时候再根据 SPI 的规范去获取对应的服务提供者的服务实现。通过 SPI 服务加载机制进行服务的注册和发现，可以有效的避免在代码中将服务提供者写死。从而可以基于接口编程，实现模块间的解耦。

> An extensible application is one that you can extend without modifying its original code base. You can enhance its functionality with new plug-ins or modules. Developers, software vendors can add new functionality or application programming interfaces (APIs) by adding a new Java Archive (JAR) file onto the application class path or into an application-specific extension directory.

接下来我们就基于 Oracle 的这篇文章 [Creating Extensible Applications](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html) 来介绍一下 Java SPI 机制。

# Dictionary Service Example

假设我们要给某个编辑器设计一个字典的功能，要求用户可以根据自己的需要对字典进行扩展

## Define the Service Provider Interface

```Java
public interface Dictionary {
    String getDefinition(String word);
}
```

## 定义一个单例的 DictionaryService 代理具体实现提供服务（不是必须）

```Java
public class DictionaryService {
    private static DictionaryService service;
    private ServiceLoader<Dictionary> loader;

    private DictionaryService() { // Singleton
        loader = ServiceLoader.load(Dictionary.class);
    }

    public static synchronized DictionaryService getInstance() {
        if (service == null) {
            service = new DictionaryService();
        }
        return service;
    }


    public String getDefinition(String word) {
        String definition = null;

        try {
            Iterator<Dictionary> dictionaries = loader.iterator();
            while (definition == null && dictionaries.hasNext()) {
                Dictionary d = dictionaries.next();
                definition = d.getDefinition(word);
            }
        } catch (ServiceConfigurationError serviceError) {
            definition = null;
            serviceError.printStackTrace();

        }
        return definition;
    }
}
```

## Implement the Service Provider

基础实现

```Java
public class GeneralDictionary implements Dictionary {
    private SortedMap<String, String> map;

    public GeneralDictionary() {
        map = new TreeMap<String, String>();
        map.put(
                "book",
                "a set of written or printed pages, usually bound with " +
                        "a protective cover");
        map.put(
                "editor",
                "a person who edits");
    }

    @Override
    public String getDefinition(String word) {
        return map.get(word);
    }
}
```


扩展实现

```Java
public class ExtendedDictionary implements Dictionary {
    private SortedMap<String, String> map;

    public ExtendedDictionary() {
        map = new TreeMap<>();
        map.put(
                "xml",
                "a document standard often used in web services, among other " +
                        "things");
        map.put(
                "REST",
                "an architecture style for creating, reading, updating, " +
                        "and deleting data that attempts to use the common " +
                        "vocabulary of the HTTP protocol; Representational State " +
                        "Transfer");
    }

    @Override
    public String getDefinition(String word) {
        return map.get(word);
    }
}
```

## 注册 Service Providers

在 resources 下创建 `META-INF/services` 目录，以接口全路径名为文件名创建一个配置文件，内容为 fully qualified class names of your service providers, one name per line. The file must be UTF-8 encoded。

```
library/src/main/resources
└── META-INF
    └── services
        └── com.jverson.springboot.service.Dictionary

```

其中 `com.jverson.springboot.service.Dictionary` 文件内容如下

```
com.jverson.springboot.impl.GeneralDictionary
com.jverson.springboot.impl.ExtendedDictionary
```

## 测试

```Java
public class TestJavaSPI {

    @Test
    public void testSPI() {
        DictionaryService dictionary = DictionaryService.getInstance();
        System.out.println(TestJavaSPI.lookup(dictionary, "book"));
        System.out.println(TestJavaSPI.lookup(dictionary, "editor"));
        System.out.println(TestJavaSPI.lookup(dictionary, "xml"));
        System.out.println(TestJavaSPI.lookup(dictionary, "REST"));
        System.out.println(TestJavaSPI.lookup(dictionary, "html"));
    }

    public static String lookup(DictionaryService dictionary, String word) {
        String outputString = word + ": ";
        String definition = dictionary.getDefinition(word);
        if (definition == null) {
            return outputString + "Cannot find definition for this word.";
        } else {
            return outputString + definition;
        }
    }

}
```

## Terms and Definitions of Java SPI

由上面的例子可以看到 SPI 包含几个基本的组件

1. Service(e.g. DictionaryService)：A well-known set of programming interfaces and classes that provide access to some specific application functionality or feature.
2. Service Provider Interface（e.g. Dictionary）：An interface or abstract class that acts as a proxy or an endpoint to the service.
3. Service Provider(e.g. GeneralDictionary)：A specific implementation of the SPI. A Service Provider is configured and identified through a provider configuration file which we put in the resource directory META-INF/services. The file name is the fully-qualified name of the SPI and its content is the fully-qualified name of the SPI implementation. The Service Provider is installed in the form of extensions, a jar file which we place in the application classpath, the Java extension classpath or the user-defined classpath.
4. ServiceLoader：At the heart of the SPI is the ServiceLoader class. This has the role of discovering and loading implementations lazily. It uses the context classpath to locate provider implementations and put them in an internal cache.

## ServiceLoader 源码解析

直接从 ServiceLoader 类的 load 方法看起

```Java
public static <S> ServiceLoader<S> load(Class<S> service) {
    //1.获取当前线程的类加载
    ClassLoader cl = Thread.currentThread().getContextClassLoader();
    return ServiceLoader.load(service, cl);
}

//构造函数
private ServiceLoader(Class<S> svc, ClassLoader cl) {
    //判断入参是否为null
    service = Objects.requireNonNull(svc, "Service interface cannot be null");
    //2.加载器如果不存在，获取系统类加载器，通常是applicationLoader，应用程序加载器
    loader = (cl == null) ? ClassLoader.getSystemClassLoader() : cl;
    //3.获取访问控制器
    acc = (System.getSecurityManager() != null) ? AccessController.getContext() : null;
    reload();
}

public void reload() {
    // 清空缓存
    providers.clear();
    // 初始化内部类，用于遍历提供者
    lookupIterator = new LazyIterator(service, loader);
}

// 这里再来看 ServiceLoader 的一些属性

private static final String PREFIX = "META-INF/services/";

// 要加载的类
private final Class<S> service;

// 用于加载实现类的类加载器
private final ClassLoader loader;

// 访问控制器
private final AccessControlContext acc;

// 提供者的缓存
private LinkedHashMap<String,S> providers = new LinkedHashMap<>();

// 一个内部类，用于遍历实现类
private LazyIterator lookupIterator;

// 可以发现重点就在于 LazyIterator 这个内部类上，我们获取实现类都看这个内部类

private class LazyIterator
    implements Iterator<S>
{

    Class<S> service;
    ClassLoader loader;
    Enumeration<URL> configs = null;
    Iterator<String> pending = null;
    String nextName = null;

  //构造函数
  private LazyIterator(Class<S> service, ClassLoader loader) {
      this.service = service;
      this.loader = loader;
  }

  private boolean hasNextService() {
        if (nextName != null) {
            return true;
        }
        if (configs == null) {
            try {
                //获取META-INF/services下文件全称
                String fullName = PREFIX + service.getName();
                if (loader == null)
                    configs = ClassLoader.getSystemResources(fullName);
                else
                    //获取配置文件内具体实现的枚举类
                    configs = loader.getResources(fullName);
            } catch (IOException x) {
                fail(service, "Error locating configuration files", x);
            }
        }
        while ((pending == null) || !pending.hasNext()) {
            if (!configs.hasMoreElements()) {
                return false;
            }
            pending = parse(service, configs.nextElement());
        }
        nextName = pending.next();
        return true;
    }

  private S nextService() {
      if (!hasNextService())
          throw new NoSuchElementException();
      //循环遍历获取实现类的全限定名
      String cn = nextName;
      nextName = null;
      Class<?> c = null;
      try {
          //实例化实现类
          c = Class.forName(cn, false, loader);
      } catch (ClassNotFoundException x) {
          fail(service,
               "Provider " + cn + " not found");
      }
      if (!service.isAssignableFrom(c)) {
          fail(service,
               "Provider " + cn  + " not a subtype");
      }
      try {
          //这一行将实例化的类强转成所表示的类型
          S p = service.cast(c.newInstance());
          //缓存实现类
          providers.put(cn, p);
          //返回对象
          return p;
      } catch (Throwable x) {
          fail(service,
               "Provider " + cn + " could not be instantiated",
               x);
      }
      throw new Error();          // This cannot happen
  }

```


# SPI Samples in the Java Ecosystem

Java provides many SPIs. Here are some samples of the service provider interface and the service that it provides:

- CurrencyNameProvider: provides localized currency symbols for the Currency class.
- LocaleNameProvider: provides localized names for the Locale class.
- TimeZoneNameProvider: provides localized time zone names for the TimeZone class.
- DateFormatProvider: provides date and time formats for a specified locale.
- NumberFormatProvider: provides monetary, integer and percentage values for the NumberFormat class.
- Driver: as of version 4.0, the JDBC API supports the SPI pattern. Older versions uses the Class.forName() method to load drivers.
- PersistenceProvider: provides the implementation of the JPA API.
- JsonProvider: provides JSON processing objects.
- JsonbProvider: provides JSON binding objects.
- Extension: provides extensions for the CDI container.
- ConfigSourceProvider: provides a source for retrieving configuration properties.


# Summary

The easiest way to create an extensible application is to use the ServiceLoader, which is available for Java SE 6 and later. Using this class, you can add provider implementations to the application class path to make new functionality available. The ServiceLoader class is final, so you cannot modify its abilities.

# References

- [Creating Extensible Applications](https://docs.oracle.com/javase/tutorial/ext/basics/spi.html)
- [Java Service Provider Interface](https://www.baeldung.com/java-spi)
- [Java SPI源码解析及demo讲解](https://segmentfault.com/a/1190000022101812)
