# 单例模式
---

单例模式（Singleton Pattern）是 Java 中最简单的设计模式之一。这种模式涉及到一个单一的类，该类负责创建自己的对象，同时确保只有单个对象被创建。这个类提供了一种访问其唯一的对象的方式，可以直接访问，不需要实例化该类的对象。

> 一句话概括，全局有且只有一个对象，并能够全局访问得到。


单例模式的核心有以下几点：

1. 构造方法私有，防止外部调用创建实例，私有静态引用指向自己实例 
2. 确保多线程并发下只有一个实例被创建
3. 注重效率的场景

## 使用场景

许多时候整个系统只需要拥有一个的全局对象，这样有利于我们协调系统整体的行为。以下为一些常见的场景：

- 服务器的配置信息的读取，一般应用单例模式，这个是由于配置文件是共享的资源。
- 数据库连接池的设计一般也是采用单例模式，因为数据库连接是一种数据库资源。数据库软件系统中使用数据库连接池，主要是节省打开或者关闭数据库连接所引起的效率损耗，这种效率上的损耗还是非常昂贵的，因为何用单例模式来维护，就可以大大降低这种损耗。
- 多线程的线程池的设计一般也是采用单例模式，这是由于线程池要方便对池中的线程进行控制。
- Spring 管理的 Bean 默认也都是单例模式

总结以下不难看出，单例模式应用的场景一般发现在以下条件下：

1. 资源共享的情况下，避免由于资源操作时导致的性能或损耗等。如上述中的应用配置。
2. 控制资源的情况下，方便资源之间的互相通信。如线程池等。

但是如果单例不需要维护任何状态，仅仅提供全局访问的方法，这种情况应该考虑使用静态类，静态方法比单例更快，因为静态的绑定是在编译期就进行。 



## 实现方式

单例模式有多种实现方式，不同的实现方式适用不同的场景，下面简单总结一下

### 懒汉式（第一次使用时才创建实例）

```Java
public class Singleton1 {
    private Singleton1() {
    }
    private static Singleton1 singleton1;
    public synchronized static Singleton1 getInstance(){
        if (singleton1 == null){
            singleton1 = new Singleton1();
        }
        return singleton1;
    }
}
```

这种方式虽然线程安全，但是每次调用都有加锁的操作（其实只有第一次才有用），会对程序的性能产生影响，因此并不是很常用！更常见的是下面这种双重锁的实现方式！

### 双重锁方式（推荐使用）

注意需要对静态变量（线程共享）加上 volatile 修饰使线程之间实现可见性，要不然仍然是线程不安全的。还需要注意的一点是在同步代码块（synchronized）里外都需要做一次是否为 null 的判断，外面的判断是为了实例创建好以后再次访问避免加锁影响性能；里面的判断是为了线程安全，试想两个线程 t1 和 t2 并发访问 getInstance 方法，执行到 synchronized 的时候 t1 先拿到锁，然后创建了实例并返回，这时 t2 获取到锁，如果在里面不再加一层 null 的判断，就会直接进入内部创建实例了！

```Java
/**
 * 单例模式的双重锁方式实现
 */
public class Singleton2 {
    private static volatile Singleton2 singleton2;
    private Singleton2() {
    }
    public static Singleton2 getInstance() {
        if (singleton2 == null) {
            synchronized (Singleton2.class) {
                if (singleton2 == null){
                    singleton2 = new Singleton2();
                }
            }
        }
        return singleton2;
    }
}
```

这种采用双锁机制的方式，安全且在多线程情况下能保持高性能。除了代码稍显繁琐意外可以推荐使用！


### 饿汉式（直接创建类实例）不推荐

```Java
/**
 * 单例模式的饿汉方式实现
 */
public class Singleton3 {
    private static Singleton3 singleton3 = new Singleton3();
    private Singleton3() {
    }
    public static Singleton3 getInstance() {
        return singleton3;
    }
}
```

这种方式最简单，也没有并发问题和效率问题，但是在类加载时就初始化，有些浪费内存，因为有可能这个方法自始至终都不会被调用到，尤其是在一些对外提供的工具包或 API 时应该尽量避免这种方式。

### 静态内部类方式（推荐使用）

这种方式能达到双检锁方式一样的功效，但实现更简单。它利用了 JVM 的类机制来保证初始化 instance 时只有一个线程。并且**外部类加载时并不需要立即加载内部类，这就实现了 lazy Load 的效果。**只有在第一次显式调用 getInstance 方法时，才会装载 SingletonHolder 类，从而实例化 instance。

```Java
/**
 * 单例模式的静态内部类实现方式
 */
public class Singleton4 {
    private Singleton4() {
    }
    private static class InnerClass {
        private static final Singleton4 singleton4 = new Singleton4();
    }
    public static Singleton4 getInstance() {
        return InnerClass.singleton4;
    }
}
```


### 枚举方式

这种方式是 Effective Java 作者 Josh Bloch 提倡的方式，他认为单元素的枚举类型被作者认为是实现 Singleton 的最佳方法。这种方式不仅能避免多线程同步问题，而且还自动支持序列化机制，防止反序列化和反射攻击重新创建新的对象，绝对防止多次实例化。

```Java
/**
 * 单例模式的枚举方式实现
 */
public enum Singleton5 {
    INSTANCE;
    public void whateverMethod(){
        //do what you want
    }
}
```

这种方式的原理是什么呢？趁这个机会在这里好好梳理一下枚举的概念。

枚举是 JDK5 中提供的一种语法糖，所谓**语法糖**就是在计算机语言中添加的某种语法，这种语法对语言的功能并没有影响，但是但是更方便程序员使用。只是在编译器上做了手脚，却没有提供对应的指令集来处理它。

其实 Enum 就是一个普通的类，它继承自 java.lang.Enum 类，这个可以通过反编译枚举类的字节码来理解。

```Java
public enum DataSourceEnum {
    DATASOURCE;
} 

// 对上面的代码编译后的字节码进行反编译可以得到下面的代码

public final class DataSourceEnum extends Enum<DataSourceEnum> {
    public static final DataSourceEnum DATASOURCE;
    public static DataSourceEnum[] values();
    public static DataSourceEnum valueOf(String s);
    static {};
} 
```

由反编译后的代码可知，DATASOURCE 被声明为 static 的，虚拟机会保证一个类的 `<clinit>()`方法在多线程环境中被正确的加锁、同步。所以，枚举实现在实例化时是线程安全。

另外 Java 规范中规定，每一个枚举类型及其定义的枚举变量在 JVM 中都是唯一的，因此在枚举类型的序列化和反序列化上，Java 做了特殊的规定。在序列化的时候 Java 仅仅是将枚举对象的 name 属性输出到结果中，反序列化的时候则是通过 java.lang.Enum 的 valueOf() 方法来根据名字查找枚举对象，因此反序列化后的实例也会和之前被序列化的对象实例相同。

## 总结

一般情况下，使用饿汉方式即可，简单又不涉及线程安全和效率问题。其它需要实现懒加载的场景中使用双重锁（常规）、静态内部类（较常规）或者枚举（炫技）的方式都行。

## 参考

- [单例模式](https://www.runoob.com/design-pattern/singleton-pattern.html)
- [枚举实现单例原理](https://blog.csdn.net/zj20142213/article/details/81415206)

