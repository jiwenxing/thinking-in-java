# Java 内部类
---

> 可以将一个类的定义放在另一个类的定义内部，这就是内部类

总的来说，内部类是一个略显晦涩的概念，实际使用到的场合不是很多，但是对于特定的问题却是最优雅的解决方式，因此有必要掌握，至少应该完全理解内部类的语法和语义，当见到使用内部类的实际应用时能够最终理解它们。

## 内部类的特点

1. 内部类拥有外围类所有元素的访问权
2. 可以创建匿名内部类
3. 可以创建局部内部类（在方法体中定义类）
4. 可以在接口中定义内部类（见Selector接口中的示例）

内部类示例：

```java
public class Sequence {
	private Object[] items;
	private int next = 0;
	public Sequence(int size) {
		items = new Object[size];
	}
	public void add(Object x) {
		if (next < items.length) {
			items[next++] = x;
		}
	}
	//使用内部类实现Sequence对象的迭代器功能
	private class SequenceSelector implements Selector{
		private int i = 0;
		@Override
		public boolean end() {
			return i == items.length; //内部类可以访问外围类的private属性
		}
		@Override
		public Object current() {
			return items[i];
		}
		@Override
		public void next() {
			if (i < items.length) {
				i++;
			}
		}
	}
	//返回一个内部类对象
	public Selector selector() {
		return new SequenceSelector();
	}
	public static void main(String[] args) {
		//初始化一个Sequence对象
		Sequence sequence = new Sequence(10);
		for(int i=0; i<10; i++){
			sequence.add(Integer.toString(i));
		}
		//创建内部类对象（有一个向上转型的过程），另外注意：在拥有外部类对象之前是无法创建内部类的
		Selector selector = sequence.selector(); //构建内部类对象时，必须有一个指向外围类对象的引用（这里是sequence）
		//使用内部类定义的Selector遍历Sequence
		while (!selector.end()) {
			System.out.println(selector.current() + " ");
			selector.next();
		}
	}
}

/**
 * 迭代器的接口定义
 * @author jverson
 */
interface Selector{
	boolean end();
	Object current();
	void next();
	
	//示例接口中定义内部类，甚至可以在内部类中实现外围接口
	class Test implements Selector{
		@Override
		public boolean end() {
			System.out.println("end");
			return false;
		}
		@Override
		public Object current() {
			System.out.println("current");
			return null;
		}
		@Override
		public void next() {
			System.out.println("next");
		}
		public static void main(String[] args) {
			System.out.println(new Test().current());
		}
	}
}
```
注意，上面的这个示例只是用于展示内部类的写法，因为完全可以不用内部类实现，Sequence类直接实现Selector接口即可，可参看[Sequence2.java](https://github.com/jiwenxing/thinking-in-java/blob/master/src/chapter10/Sequence2.java)


## 为什么需要内部类

到底内部类有什么用呢，它能解决什么问题，什么情况才必须使用内部类呢？

> 内部类允许继承多个非接口类型，换句话说**内部类可以实现java的多重继承**，尽管通过实现多个接口也可以达到多重继承的目的，但是当你拥有的是抽象类或具体类时，则只能使用内部类才能实现多重继承。

如果不需要解决“多重继承”的问题，不使用内部类都可以解决问题，但是如果使用内部类，还可以获得其它一些特性：

1. 内部类可以有多个实例，每个实例都有自己的状态信息，并且与其外围类对象的信息相互独立
2. 在单个外围类中，可以让内部类以不同的方式实现同一个接口，或继承同一个类
3. 内部类没有令人迷惑的“is-a”关系，它就是一个独立的实体。例如上面的示例假如使用`Sequence2.java` 的方式实现则相当于声明“Sequence是一个Selector”，而且使用内部类可以很容的再创建一个反向遍历的reverseSelector()方法，这就是内部类带来的便利
