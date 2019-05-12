# Java Class 类介绍
---

## Class 类介绍

Class 对象是一种描述对象类的元对象，它主要用于 Java 的反射功能。

在 Java 中存在两种对象，一种是我们 new 出来的**实例对象**，另一种既是 jvm 生成的用来保存对应类的信息的 **Class 对象**。当我们定义好一个类文件并编译成 .class 字节码后，编译器同时为我们定义了一个 Class 对象并将它保存 .class 文件中。jvm 在类加载的时候将 .class 文件和对应的 Class 对象加载到内存中。

Class 没有公共构造方法。Class 对象是在加载类时由 Java 虚拟机通过调用类加载器中的 defineClass 方法**自动构造**的，因此不能显式地声明一个 Class 对象。

每个类都有且只有一个 Class 对象。运行程序时，jvm 首先检查类对应的 Class 对象是否已经加载。如果没有加载就会根据类名查找 .class 文件，并将其 Class 对象载入。虚拟机为每种类型管理一个独一无二的 Class 对象，但可以根据 Class 对象生成多个对象实例。某个类的 Class 对象被载入内存，它就会被用来创建这个类的所有对象。


## 获取 Class 对象

由于 java.lang.Class 的构造方法是私有的，我们没法通过 new 的方式进行创建。有以下三种方式获取 Class 对象：`类名.class`、`Class.forName() 静态方法` 及 `对象.getClass()`。

### 类名.class 方式（字面常量方式）

```java
public class Test {
    static {
        System.out.println("Run static initialization block.");
    }
}

// 类名获取，注意这里不会执行类中的静态代码块内容
Class t = Test.class;
```

执行时 jvm 会先检查 Class 对象是否装入内存，如果没有装入内存，则将其装入内存，然后返回 Class 对象，如果已装入内存，则直接返回。**在加载 Class对 象后，不会对 Class 对象进行初始化**，这个特性很重要，我们在使用的过程中也推荐这种方式。

### Class.forName() 方式

```java
try {
    Class t = Class.forName("com.test.Test"); //会打印出 Run static initialization block.
} catch (ClassNotFoundException e) {
    e.printStackTrace();
}
```

这种方式在加载 Class 对象后，会对类进行初始化，即执行类的静态代码块。

### 对象.getClass() 方式

```java
Test t = new Test();
Class test = t.getClass();
```

通过类的实例获取，显然实例已经被创建，静态代码块肯定会被执行。

总结一下这三种方式，其中实例类的 getClass 方法和 Class 类的静态方法 forName 都将会触发类的初始化阶段，而字面常量获取 Class 对象的方式则不会触发初始化。这一点需要在使用的过程中特别注意。

## Hotpot JVM Class 对象是在方法区还是堆中

JDK6 中 Class 实例在方法区。但 JDK7/8 创建的 Class 实例在 Java heap 中，并且 JDK8 移除了永久代，转而使用元空间 MetaSpace 来实现方法区。


## instanceof 关键字与 isInstance 方法

```java
//instanceof关键字
if(obj instanceof Animal){
    Animal animal = (Animal) obj;
}

//isInstance方法
if(Animal.class.isInstance(obj)){
    Animal animal = (Animal) obj;
}
```

两种方法的执行效果是一样的，需要注意的是 instanceOf 关键字只被用于对象引用变量，检查左边对象是不是右边类或接口的实例化。如果被测对象是 null 值，则测试结果总是 false。而 isInstance 方法则是 Class 类的 Native 方法，其中 obj 是被测试的对象或者变量，如果 obj 是调用这个方法的 class 或接口的实例，则返回 true。如果被检测的对象是 null 或者基本类型，那么返回值是 false;

## 封装类的 TYPE

基本类型的 Class 对象和封装类的 TYPE 是同一个 Class 对象

![](https://jverson.oss-cn-beijing.aliyuncs.com/f2105249d330e63819857eb8ffa7b9cb.jpg)


```java
System.out.println(boolean.class == Boolean.TYPE);//true

// Boolean 类中有如下定义
/**
 * The Class object representing the primitive type boolean.
 *
 * @since   JDK1.1
 */
@SuppressWarnings("unchecked")
public static final Class<Boolean> TYPE = (Class<Boolean>) Class.getPrimitiveClass("boolean");
```

## Class 对象和 Java 反射

某种程度上讲，Class 类是 Java 反射机制的起源和入口，反射正是在运行期间通过 Class 对象访问类的属性、方法（包括构造方法）以及创建类实例的。[说说 Java 反射机制](https://jverson.com/thinking-in-java/jvm/java-reflection.html) 中详细讲了 Java 的反射特性。

