# 说说 Java 反射机制
---


## 白话 Java 反射

> 通过反射，我们可以在**运行时**获得程序或程序集中每一个类型的成员和成员的信息。程序中一般的对象的类型都是在编译期就确定下来的，而 Java 反射机制可以动态地创建对象并调用其属性，这样的对象的类型在编译期是未知的。所以我们可以通过反射机制直接创建对象，即使这个对象的类型在编译期是未知的。

上面是反射的官方定义，是不是看完有点不太理解。下面我们用白话翻译一下，正常情况下我们在代码阶段通过 new 的方式可以创建一个类的实例，设置它的属性，然后编译为 class 字节码文件，运行时通过 JVM 加载到内存解释执行，注意这种方式下待实例化的对象在编码阶段或者说在编译器就已经确定了。但现在存在一些特殊的情况使我在编码的时候没办法通过 new 去创建实例了，比如说我在写一段解析 spring xml bean 配置的代码（此时只能从 xml 中获取到类的全限定名的字符串），或者调用外部实现的扩展接口（只知道接口定义，并不知道外部实现的类信息），又或者待实例化的对象需要运行期间动态从配置文件中(配置文件中配类的全限定名，一般是字符串)读取，这些情况下仍然需要去创建类的示例并调用其中的方法完成我们的工作，该怎么实现呢？其实反射就是干这个事情的.

> 反射机制无非就是在程序运行期间根据一个 String（全限定类名） 来得到想要的实体对象，然后调用它的方法，访问它的属性，它不需要事先（写代码的时候或编译期）知道运行对象是谁。

## 使用方法

Java 反射主要提供以下功能：

- 在运行时获取任意对象所属的类；       
```java
Class clazz = Class.forName(driver); //方式1：Class.forName 静态方法
Class<?> clazz = int.class; //方式2：直接获取
StringBuilder str = new StringBuilder("123");
Class<?> clazz = str.getClass(); //方式3：调用对象的 getClass() 方法
```

- 在运行时构造任意类的对象；      
```java
//方式1：使用Class对象的newInstance()方法来创建Class对象对应类的实例
Class<?> c = String.class;
Object str = c.newInstance(); 
//方式2：用指定的构造器构造类的实例。
Class<?> c = String.class;
Constructor constructor = c.getConstructor(String.class);
Object obj = constructor.newInstance("23333");
```

- 在运行时获取类对象的成员变量和方法（包括 private 方法和 private 成员变量）；      
```java
//获取方法
public Method[] getDeclaredMethods() throws SecurityException //返回类或接口声明的所有方法，包括公共、保护、默认（包）访问和私有方法，但不包括继承的方法。
public Method[] getMethods() throws SecurityException //返回所有公用（public）方法
public Method getMethod(String name, Class<?>... parameterTypes) //返回特定名称的方法
public Field[] getFields() throws SecurityException //访问公有的成员变量
public Field[] getDeclaredFields() throws SecurityException //所有已声明的成员变量，但不能得到其父类的成员变量
```

- 运行时调用类对象的方法         
```java
Object result = MethodClass.class.getMethod("add",int.class,int.class).invoke(MethodClass.class.newInstance(),1,4);
//其中 MethodClass 定义如下
class MethodClass {
    public final int lala = 3;
    public int add(int a,int b) {
        return a+b;
    }
}
```


## 使用场景

反射在我们日常的应用层编码中使用不多，但广泛用于一些框架和设计当中，甚至有人说**反射机制是很多 Java 框架的基石**。换句话说其实我们的代码底层大量使用了反射技术，这也是为什么反射是 Java 最重要的特性之一。总的来说，虽然平时用的不多，也应该尽量避免在业务编码中使用反射，但是想深入框架源码甚至自己写框架的时候肯定需要用到，多学有益！

以下是几个最常见的应用反射的例子

1. JDBC 的数据库的连接    
```java
public static final String driver = "com.mysql.jdbc.Driver";
public static void main(String[] args) throws Exception {  
    Connection con = null; 
    Class.forName(driver); //反射机制，使用 Class 类加载驱动程序
    con = DriverManager.getConnection(url,user,psd);  
    System.out.println(con);  
    con.close(); 
}
```

2. Spring 中 XML Bean 配置解析    
```xml
<bean class="org.springframework.beans.factory.config.PropertyPlaceholderConfigurer">
    <property name="locations">
        <list>
            <value>classpath:profile/${spring.profiles.active}/alias.properties</value>
        </list>
    </property>
</bean>
```

3. JDK 动态代理，[Java 代理](https://jverson.com/thinking-in-java/JDK%20%E4%BB%A3%E7%90%86%E5%8F%8A%20CGLib%20%E4%BB%A3%E7%90%86.html) 这一篇中有讲过，可以温习一下    
JDK 动态代理核心是重写 InvocationHandler 中的 invoke 方法，在该方法中织入逻辑的同时通过反射调用被代理对象 target 的方法，其实这也是 AOP 的实现方式。 
```java
@Override
public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    System.out.println("-----织入逻辑-----");
    Object result = method.invoke(target, args);
    System.out.println("-----织入逻辑-----");
    return result;
}
```


## 总结

了解反射的原理和特性很有必要，有助于我们深入理解一些编程思想，但同时在平时的编码中应该尽量避免直接使用反射，除非在必要的情况下（例如通过自定义注解实现 AOP），原因有一下几点：

1. 反射的代码更难维护，首先在 IDE 中无法通过调用链关联进入对应方法，自然 IDE 也无法帮我们在编译期间就发现一些问题，同时代码中也会不可避免存在大量“魔术值”

2. 反射的执行效率更低，由于反射比普通的编码方式多了一些解释操作导致其效率低下

3. 反射可以突破类的权限检查调用私有方法、修改私有变量等，从而导致安全问题

## 参考

- [深入解析 Java 反射](https://www.sczyh30.com/posts/Java/java-reflection-1/)



