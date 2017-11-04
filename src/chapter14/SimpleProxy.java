/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter14;

/**
 * @author Jverson
 *
 */
public class SimpleProxy implements MyInterface {

	private MyInterface proxied;
	/**
	 * 
	 */
	public SimpleProxy(MyInterface proxied) {
		this.proxied = proxied;
	}
	/* (non-Javadoc)
	 * @see chapter14.Interface#doSth()
	 */
	@Override
	public void doSth() {
		System.out.println("proxy do sth");
		proxied.doSth();

	}

	/* (non-Javadoc)
	 * @see chapter14.Interface#doSthElse(java.lang.String)
	 */
	@Override
	public void doSthElse(String args) {
		System.out.println("proxy do sth else");
		proxied.doSthElse(args);

	}

}
