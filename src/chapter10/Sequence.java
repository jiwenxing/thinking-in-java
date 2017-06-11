/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter10;

import com.sun.org.apache.bcel.internal.generic.NEW;

/**
 * @author jverson
 */
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
		//创建内部类对象，注意：在拥有外部类对象之前是无法创建内部类的
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