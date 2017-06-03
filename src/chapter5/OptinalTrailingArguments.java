/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter5;

/**
 * @author jverson
 * 可以定义可变参数方法，当指定可变参数时编译器会自动将其填充到数组
 */
public class OptinalTrailingArguments {

	static void f(int required, String... trailing){
		System.out.println("---------start---------");
		System.out.println("required: " + required);
		System.out.println("String args: ");
		for (String string : trailing) {
			System.out.print(string + " ");
		}
		System.out.println();
		System.out.println("---------end---------");
	}
	
	public static void main(String[] args) {
		f(0);
		f(1, "str1");
		f(2, "str1","str2");
		f(3, "str1","str2","str3");
	}
}
