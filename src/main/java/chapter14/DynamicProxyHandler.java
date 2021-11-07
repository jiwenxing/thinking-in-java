/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter14;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author Jverson
 * dynamic proxy example
 */
public class DynamicProxyHandler implements InvocationHandler {

	private Object proxied;
	
	public DynamicProxyHandler(Object proxied) {
		super();
		this.proxied = proxied;
	}

	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("proxy do sth...");
		if (method.getName().equals("doSthElse")) {
			System.out.println("do extra thing for special method...");
		}
		return method.invoke(proxied, args);
	}

}
