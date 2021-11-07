/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter14;

/**
 * @author Jverson
 *
 */
public class ClassCastMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Object aString = "2";
		Integer aInteger = Integer.valueOf(aString.toString());
		System.out.println(aInteger);

	}

}
