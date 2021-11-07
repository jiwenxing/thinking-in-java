/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter14;

/**
 * @author Jverson
 *
 */
public class SimpleProxyDemo {

	public static void consumer(MyInterface iFace){
		iFace.doSth();
		iFace.doSthElse("babababa");
	}
	
	public static void main(String[] args) {
//		consumer(new RealObject());
		consumer(new SimpleProxy(new RealObject()));
	}
	
}
