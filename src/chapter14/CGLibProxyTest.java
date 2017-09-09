/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter14;

/**
 * @author Jverson
 *
 */
public class CGLibProxyTest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		CGLibProxy proxy = new CGLibProxy();
		Train t = (Train)proxy.getProxy(new Train());
		t.move();
	}

}
