/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter14;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * JDK 动态代理实现
 * @author Jverson
 * 1. 实现 InvocationHandler 接口，并添加被代理类的引用 target
 */
public class MyInvocatioHandler implements InvocationHandler {

	private Object target;

	public MyInvocatioHandler(Object target) {
		this.target = target;
	}
	
	@Override
	public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
		System.out.println("-----before-----");
        Object result = method.invoke(target, args);
        System.out.println("-----end-----");
        
        return result;
	}
	
	public Object getProxy() {
		// 获取对应的ClassLoader  
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		// 获取实现的所有接口  
		Class<?>[] interfaces = target.getClass().getInterfaces(); //类可以实现多个接口，因此这里的接口是个数组
		return Proxy.newProxyInstance(loader, interfaces, this);
	}

}
