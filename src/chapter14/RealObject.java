/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter14;

/**
 * @author Jverson
 *
 */
public class RealObject implements Interface {

	/* (non-Javadoc)
	 * @see chapter14.Interface#doSth()
	 */
	@Override
	public void doSth() {
		System.out.println("do something");

	}

	/* (non-Javadoc)
	 * @see chapter14.Interface#doSthElse()
	 */
	@Override
	public void doSthElse(String args) {
		System.out.println("do sth else "+ args);

	}

}
