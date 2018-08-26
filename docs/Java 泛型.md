## 泛型想解决的问题

一般情况下，类的成员变量、方法的参数这些只能使用具体的类型（要么基本类型，要么自定义的类），那有没有什么方法可以编写能应用于多种类型的代码呢？

首先我们会想到多态（也算是一种泛化机制），例如将方法的参数设为基类（或接口），那么任何继承该基类（实现该接口）的类都可作为参数传入。但是这样感觉还是受限太多（为了使用这个方法，必须要去继承或实现某个类或接口），有没有一种更彻底更通用的方法能使代码应用于“某种不具体的类型”，而不是一个具体的基类或接口。

jdk1.5 引入了 **“泛型”** 的概念，泛型实现了**参数化类型**，使得代码可以真正应用于多种类型。另外你会发现泛型广泛应用于容器类（list、set、map等持有对象），而据说也是为了创造容器类，才最终促成了jdk引入了泛型机制。下面看一个简单的例子：


## 如何做到一次方法调用返回多个对象

我们知道return语句只能返回一个对象，如何才能实现返回对个对象呢，显然解决方法就是专门创建一个对象，它去持有想要返回的多个对象。但是这样每次都去创建一个专门的对象感觉很烦啊，怎么办？JDK早就替你想好方法了，使用map就可以，再看看map的源码，其实你会发现最终还是泛型解决了这个问题。

```java
public interface Map<K,V>{
	//...
}
```

## 自定义泛型类、接口及方法

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
由于Number是Integer的父类，理所当然认为`getData(Box<Number> data)`方法可以应用于`Box<Integer>`，然而会看到编译报错，这说明了Box<Number>在逻辑上不能视为Box<Integer>的父类。但是我们需要一个在逻辑上可以用来表示同时是Box<Integer>和Box<Number>的父类的一个引用类型，由此，类型通配符应运而生。

**类型通配符一般是使用`?`代替具体的类型实参。且Box<?>在逻辑上是`Box<Integer>`、`Box<Number>`...等所有`Box<具体类型实参>`的父类。由此，我们依然可以定义泛型方法，来完成此类需求。** 所以将`getData()`方法改为以下这样即可：

```java
//无界通配符：类型实参的类型不限
public static void getData(Box<?> data) {
	System.out.println("data :" + data.getData());
}
//或者, 类型实参只能是Number类及其子类
public static void getData(Box<? extends Number> data) {
	System.out.println("data :" + data.getData());
}
```


## 总结

泛型使用最多的还是各种容器类，一定程度上可以说正是因为创造容器类的需要Java才引入了泛型的概念，但是泛型的思想也可以应用到其它适合的地方，**泛型是一种方法，通过它可以编写出更`泛化`的代码，这些代码对于它所能作用的类型有更少的限制，因此单个代码可以应用到更多的类型上。**