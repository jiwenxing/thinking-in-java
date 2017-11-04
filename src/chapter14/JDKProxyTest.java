/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter14;

/**
 * @author Jverson
 *
 */
public class JDKProxyTest {

	public static void main(String[] args) {
		RealObject realObject = new RealObject();
		MyInvocatioHandler handler = new MyInvocatioHandler(realObject);
		MyInterface myInterface = (MyInterface) handler.getProxy();
		myInterface.doSth();
	}
	
}
