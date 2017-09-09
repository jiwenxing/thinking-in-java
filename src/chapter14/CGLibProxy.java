/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter14;

import java.lang.reflect.Method;

import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;

/**
 * @author Jverson
 *
 */
public class CGLibProxy implements MethodInterceptor {

	private Object proxied;
	
	public Object getProxy(Object proxied){
		this.proxied = proxied;
		Enhancer enhancer = new Enhancer();
		enhancer.setSuperclass(proxied.getClass());
		enhancer.setCallback(this);
		return enhancer.create();
	}
	
	@Override
	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
		System.out.println("cglib proxy start...");
		proxy.invokeSuper(obj, args);
		System.out.println("cglib proxy end...");
		return null;
	}

	

}
