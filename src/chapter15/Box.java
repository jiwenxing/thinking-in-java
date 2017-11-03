/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter15;

/**
 * @author Jverson
 *
 */
public class Box<T> {
	private T data;

	public Box() {
	}

	public Box(T data) {
		this.data = data;
	}

	public T getData() {
		return data;
	}

	/*
	 * public static void main(String[] args) { Box<String> name = new
	 * Box<String>("corn"); Box<Integer> age = new Box<Integer>(712);
	 * 
	 * System.out.println("name class:" + name.getClass()); // chapter15.Box
	 * System.out.println("age class:" + age.getClass()); // chapter15.Box
	 * System.out.println(name.getClass() == age.getClass()); // true }
	 */

	public static void main(String[] args) {
		Box<Number> name = new Box<Number>(99);
		Box<Integer> age = new Box<Integer>(712);
		
		getData(name);
		getData(age);   //error
	}

	public static void getData(Box<? extends Number> data) {
		System.out.println("data :" + data.getData());
	}
}
