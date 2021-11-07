/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter14;

import java.lang.reflect.Proxy;

/**
 * @author Jverson 动态代理示例
 */
public class DynamicProxyDemo {

	public static void consumer(MyInterface iface) {
		iface.doSth();
		iface.doSthElse("dynamic proxy");
	}

	public static void main(String[] args) {
		RealObject realObject = new RealObject();
		// consumer(realObject);
		MyInterface proxy = (MyInterface) Proxy.newProxyInstance(MyInterface.class.getClassLoader(),
				new Class[] { MyInterface.class }, new DynamicProxyHandler(realObject));
//		consumer(proxy);
		proxy.doSth();
	}

}
