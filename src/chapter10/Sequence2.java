/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter10;


/**
 * @author jverson
 * 不使用内部类实现Sequence.java同样的功能
 */
public class Sequence2 implements Selector{
	private Object[] items;
	private int next = 0;
	public Sequence2(int size) {
		items = new Object[size];
	}
	public void add(Object x) {
		if (next < items.length) {
			items[next++] = x;
		}
	}
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
	public static void main(String[] args) {
		//初始化一个Sequence对象
		Sequence2 sequence = new Sequence2(10);
		for(int i=0; i<10; i++){
			sequence.add(Integer.toString(i));
		}
		//使用内部类定义的Selector遍历Sequence
		while (!sequence.end()) {
			System.out.println(sequence.current() + " ");
			sequence.next();
		}
	}
}

