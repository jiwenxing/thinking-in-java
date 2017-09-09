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

	public static void consumer(Interface iface) {
		iface.doSth();
		iface.doSthElse("dynamic proxy");
	}

	public static void main(String[] args) {
		RealObject realObject = new RealObject();
		// consumer(realObject);
		Interface proxy = (Interface) Proxy.newProxyInstance(Interface.class.getClassLoader(),
				new Class[] { Interface.class }, new DynamicProxyHandler(realObject));
		consumer(proxy);
	}

}
