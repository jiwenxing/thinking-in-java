/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter14;

/**
 * @author Jverson
 *
 */
public class RealObject implements MyInterface {

	@Override
	public void doSth() {
		System.out.println("do something");

	}

	@Override
	public void doSthElse(String args) {
		System.out.println("do sth else "+ args);
	}

}
