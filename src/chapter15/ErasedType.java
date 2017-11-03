/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter15;

import java.util.ArrayList;
import java.util.List;

/**
 * 演示泛型的类型擦除 即在泛型代码内部，无法获得任何有关参数类型的信息
 * 
 * @author Jverson
 *
 */
public class ErasedType {

	// ArrayList<String> 和 ArrayList<Integer> 属于同一个类，不会因为参数而不同
	/*
	 * public static void main(String[] args) { Class c1 = new
	 * ArrayList<String>().getClass(); Class c2 = new
	 * ArrayList<Integer>().getClass(); System.out.println(c1 == c2); //true }
	 */

	public static void main(String[] args) {
		Box<String> name = new Box<String>("corn");
		Box<Integer> age = new Box<Integer>(712);

		System.out.println("name class:" + name.getClass()); // chapter15.Box
		System.out.println("age class:" + age.getClass()); // chapter15.Box
		System.out.println(name.getClass() == age.getClass()); // true
	}

}
