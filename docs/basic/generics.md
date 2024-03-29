# Java 泛型（Generics）的理解和使用
---

一般情况下，类的成员变量、方法的参数这些只能使用具体的类型（要么基本类型，要么自定义的类），那有没有什么方法可以编写能应用于多种类型的代码呢？

首先我们会想到多态（也算是一种泛化机制），例如将方法的参数设为基类（或接口），那么任何继承该基类（实现该接口）的类都可作为参数传入。但是这样感觉还是受限太多（为了使用这个方法，必须要去继承或实现某个类或接口），有没有一种更彻底更通用的方法能使代码应用于“某种不具体的类型”，而不是一个具体的基类或接口。

jdk1.5 引入了 **“泛型”** 的概念，泛型实现了**参数化类型**，使得代码可以真正应用于多种类型。另外你会发现泛型广泛应用于容器类（list、set、map等持有对象），而据说也是为了创造容器类，才最终促成了jdk引入了泛型机制。

## 泛型实现原理

泛型是一种类似”模板代码“的技术，不同语言的泛型实现方式不一定相同。Java 语言的泛型实现方式是擦拭法（Type Erasure）。所谓擦拭法是指虚拟机对泛型其实一无所知，所有的工作都是编译器做的。编译器内部永远把所有类型 T 视为 Object 处理，但是，在需要转型的时候，编译器会根据 T 的类型自动为我们实行安全地强制转型。

举个简单的例子

```java
// 定义一个泛型类
public class Pair<T, K> {
    private T first;
    private K last;
    public Pair(T first, K last) {
        this.first = first;
        this.last = last;
    }
    public T getFirst() { ... }
    public K getLast() { ... }
}

// 编译器处理后虚拟机看到的这个类的定义如下这样，将所有的泛型 T、 都当做
public class Pair {
    private Object first;
    private Object last;
    public Pair(Object first, Object last) {
        this.first = first;
        this.last = last;
    }
    public Object getFirst() {
        return first;
    }
    public Object getLast() {
        return last;
    }
}

// 我们编写一段使用该类的方法
Pair<String, Integer> p = new Pair<>("Hello", 100);
String first = p.getFirst();
Integer last = p.getLast();

// 编译器编译之后虚拟机看到的代码其实是这样的
Pair p = new Pair("Hello", 100);
String first = (String) p.getFirst();
Integer last = (Integer) p.getLast();

```

## 泛型局限

上面了解了Java泛型的实现方式——擦拭法，我们就知道了Java泛型的局限

**局限一**：<T>不能是基本类型，例如int，因为实际类型是Object，Object类型无法持有基本类型

**局限二**：无法取得带泛型的 Class。观察以下代码，所有泛型实例，无论T的类型是什么，getClass() 返回同一个 Class 实例，因为编译后它们全部都是 `Pair<Object>`

```Java
public class Main {
    public static void main(String[] args) {
        Pair<String> p1 = new Pair<>("Hello", "world");
        Pair<Integer> p2 = new Pair<>(123, 456);
        Class c1 = p1.getClass();
        Class c2 = p2.getClass();
        System.out.println(c1==c2); // true
        System.out.println(c1==Pair.class); // true

    }
}
```

**局限三**：无法判断带泛型的类型，如下所示，并不存在 `Pair<String>.class`，而是只有唯一的 `Pair.class`。

```Java
Pair<Integer> p = new Pair<>(123, 456);
// Compile error:
if (p instanceof Pair<String>) {
}
```

**局限四**：不能实例化T类型

```Java
// 编译报错
public class Pair<T> {
    private T first;
    private T last;
    public Pair() {
        // Compile error:
        first = new T();
        last = new T();
    }
}

// 可以写成下面这样
public class Pair<T> {
    private T first;
    private T last;
    public Pair(Class<T> clazz) {
        first = clazz.newInstance();
        last = clazz.newInstance();
    }
}
```

## 泛型接口

```Java
public interface Comparable<T> {
    int compareTo(T o);
}


class Person implements Comparable<Person> {
    String name;
    int score;
    Person(String name, int score) {
        this.name = name;
        this.score = score;
    }
    public int compareTo(Person other) {
        return this.name.compareTo(other.name);
    }
    public String toString() {
        return this.name + "," + this.score;
    }
}
```


## 泛型类

```java
public class Box<T> {
	private T data;
	
	public Box() {
	}
	
	public Box(T data){
		this.data = data;
	}
	
	public T getData() {
		return data;
	}
	
	public static void main(String[] args) {
		Box<String> name = new Box<String>("corn");
		Box<Integer> age = new Box<Integer>(712);
	
		System.out.println("name class:" + name.getClass()); // chapter15.Box
		System.out.println("age class:" + age.getClass()); // chapter15.Box
		System.out.println(name.getClass() == age.getClass()); // true
   }
	
}
```
我们发现，在使用泛型类时，虽然传入了不同的泛型实参，但并没有真正意义上生成不同的类型，究其原因，在于Java中的泛型只是作用于代码编译阶段，在编译过程中，对于正确检验泛型结果后，会将泛型的相关信息擦出，也就是说，成功编译过后的class文件中是不包含任何泛型信息的。泛型信息不会进入到运行时阶段。**泛型类型在逻辑上可以看成是多个不同的类型，实际上都是相同的基本类型。**

编写泛型类时，要特别注意，**静态泛型方法需要在方法声明所有使用到的泛型，all generic types must be added to the method signature**。如下所示

```Java
public class Pair<T> {
    private T first;
    private T last;
    public Pair(T first, T last) {
        this.first = first;
        this.last = last;
    }
    public T getFirst() { ... }
    public T getLast() { ... }

    // 对静态方法使用<T>, 这样写会导致编译错误，我们无法在静态方法create()的方法参数和返回类型上使用泛型类型T
    public static Pair<T> create(T first, T last) {
        return new Pair<T>(first, last);
    }

    // 在 static 修饰符后面加一个<T>，编译就能通过
    public static <T> Pair<T> create(T first, T last) {
        return new Pair<T>(first, last);
    }

    // all generic types must be added to the method signature,  <T, G>
    public static <T, G> List<G> fromArrayToList(T[] a, Function<T, G> mapperFunction) {
	    return Arrays.stream(a)
	      .map(mapperFunction)
	      .collect(Collectors.toList());
    }
}




// 

```

## 类型通配符

```java
public static void main(String[] args) {
	Box<Number> name = new Box<Number>(99);
	Box<Integer> age = new Box<Integer>(712);
	
	getData(name);
	getData(age);   //error! The method getData(Box<Number>) in the type Box<T> is not applicable for the arguments (Box<Integer>
}

public static void getData(Box<Number> data) {
	System.out.println("data :" + data.getData());
}
```
由于 Number 是 Integer 的父类，理所当然认为`getData(Box<Number> data)`方法可以应用于`Box<Integer>`，然而会看到编译报错，这说明了 `Box<Number>` 在逻辑上不能视为 `Box<Integer>` 的父类。但是我们需要一个在逻辑上可以用来表示同时是 `Box<Integer>` 和 `Box<Number>` 的父类的一个引用类型，由此，类型通配符应运而生。

**类型通配符一般是使用`?`代替具体的类型实参。且 `Box<?>` 在逻辑上是`Box<Integer>`、`Box<Number>`...等所有`Box<具体类型实参>`的父类。由此，我们依然可以定义泛型方法，来完成此类需求。** 所以将`getData()`方法改为以下这样即可：

另外注意无限定通配符 `<?>` 很少使用，一般可以用 `<T>` 替换，同时它是所有 `<T>` 类型的超类。

```java
//无界通配符：类型实参的类型不限
public static void getData(Box<?> data) {
	System.out.println("data :" + data.getData());
}
//或者, 类型实参只能是Number类及其子类
public static void getData(Box<? extends Number> data) {
	System.out.println("data :" + data.getData());
}

//支持多继承
public static void getData(Box<T extends Number & Comparable> data) {
	System.out.println("data :" + data.getData());
}
```

然后再来看一个 JDK 的例子，Collections 的 copy() 方法，可以发现第一个参数用了 <? super T>，而第二个参数用了 `<? extends T>`，这有啥区别呢？

PECS 原则：Producer Extends Consumer Super。即：如果需要返回T，它是生产者（Producer），要使用extends通配符；如果需要写入T，它是消费者（Consumer），要使用super通配符。详细解释可以参考 [super通配符](https://www.liaoxuefeng.com/wiki/1252599548343744/1265105920586976)

```Java
public class Collections {
    public static <T> void copy(List<? super T> dest, List<? extends T> src) {
        for (int i=0; i<src.size(); i++) {
            T t = src.get(i); // src是producer
            dest.add(t); // dest是consumer
        }
    }
}
```

## 泛型继承

一个类可以继承自一个泛型类。在继承了泛型类型的情况下，编译器就必须把类型T（对 IntList 来说，也就是 Integer 类型）保存到子类的 class 文件中，不然编译器就不知道 IntList 只能存取 Integer 这种类型。**因此这种情况下子类是可以在运行期获取父类的泛型类型的。注意这个特性非常重要，后面提到的 fastjson 及 gson 的泛型反序列化都用到了这个特性。**

IntList 可以获取到父类的泛型类型 Integer 举例。

```Java
public class IntList extends ArrayList<Integer> {
    public static void main(String[] args) {
        // 测试
        IntList intList = new IntList();
        intList.add(1);
        intList.add(2);
        System.out.println(intList);

        // 子类获取父泛型类的类型
        Class<IntList> intListClass = IntList.class;
        Type t = intListClass.getGenericSuperclass();
        if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            Type[] types = pt.getActualTypeArguments(); // 可能有多个泛型类型
            Type firstType = types[0]; // 取第一个泛型类型
            Class<?> typeClass = (Class<?>) firstType;
            System.out.println(typeClass); // class java.lang.Integer
        }

    }
}
```

这里用到了泛型类中的 Type 及 ParameterizedType 等，这些下面会详细介绍

![](https://jverson.oss-cn-beijing.aliyuncs.com/967934a6e803828193172554ade5aca9.jpg)


## 泛型类的反序列化

前面我们说过泛型的类型信息只作用于编译期，执行期虚拟机都只是当做 Object 处理。那么如果我们想在运行期获取泛型类型信息该怎么实现呢？

先举一个经常遇到的例子，为什么使用 `new TypeToken<Map<String, Integer>>(){}.getType()` 这样就可以在 runtime 获取到泛型类型进行反序列化呢？这里就用到了前面提到的继承自泛型类的子类在运行期可以获取泛型类型！

```Java
@Test
public void test() {
    Gson gson = new Gson();
    Map<String, Integer> map0 = new HashMap<>();
    map0.put("aa", 1);
    map0.put("bb", 2);
    map0.put("cc", 3);
    System.out.println("map0: " + map0); // {aa=1, bb=2, cc=3} 原始 map
    String json = gson.toJson(map0);
    System.out.println("json: " + json); // {"aa":1,"bb":2,"cc":3} jsonstr
    Map map1 = gson.fromJson(json, Map.class); // 写成 Map<String, Integer>.class 会报错，因为 runtime 会擦除类型信息
    System.out.println("map1: " + map1); // {aa=1.0, bb=2.0, cc=3.0} 不指定泛型类型
    Map map2 = gson.fromJson(json, new TypeToken<Map<String, Integer>>(){}.getType()); // 如果使用 fastjson 则将 TypeToken 换为 TypeReference
    System.out.println("map2: " + map2); // {aa=1, bb=2, cc=3} 指定泛型类型
}
```

fastjson 及 gson 都是利用这个特性来实现的，我们看一下 TypeToken 的注释，

```Java
/**
 * Represents a generic type {@code T}. Java doesn't yet provide a way to
 * represent generic types, so this class does. Forces clients to create a
 * subclass of this class which enables retrieval the type information even at
 * runtime.
 *
 * <p>For example, to create a type literal for {@code List<String>}, you can
 * create an empty anonymous inner class:
 *
 * <p>
 * {@code TypeToken<List<String>> list = new TypeToken<List<String>>() {};}
 *
 * <p>This syntax cannot be used to create type literals that have wildcard
 * parameters, such as {@code Class<?>} or {@code List<? extends CharSequence>}.
 *
 * @author Bob Lee
 * @author Sven Mawson
 * @author Jesse Wilson
 */

// 代码内容作了简化，只保留核心代码
public class TypeToken<T> { 
    private final Type type;

    public TypeToken() {
        Type superclass = getClass().getGenericSuperclass();
        type = ((ParameterizedType) superclass).getActualTypeArguments()[0];
    }

    public Type getType() {
        return type;
    }
}
```

这里的核心就是 `new TypeToken<Map<String, Integer>>(){}` 创建了一个 `Map<String, Integer>` 的匿名内部子类。如果看不懂这个匿名类是怎么创建的，可以参考 [Anonymous Classes in Java](https://www.baeldung.com/java-anonymous-classes)

![](https://jverson.oss-cn-beijing.aliyuncs.com/54b9bf281a6e24a0cff38ca5573e1ed8.jpg)


## 总结

泛型使用最多的还是各种容器类，一定程度上可以说正是因为创造容器类的需要Java才引入了泛型的概念，但是泛型的思想也可以应用到其它适合的地方，**泛型是一种方法，通过它可以编写出更`泛化`的代码，这些代码对于它所能作用的类型有更少的限制，因此单个代码可以应用到更多的类型上。**

## 参考

- [The Basics of Java Generics - baeldung](https://www.baeldung.com/java-generics)
- [什么是泛型 - 廖雪峰](https://www.liaoxuefeng.com/wiki/1252599548343744/1265102638843296)
- [Super Type Tokens in Java](https://www.baeldung.com/java-super-type-tokens)