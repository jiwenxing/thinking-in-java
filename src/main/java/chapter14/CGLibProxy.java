///**
// * thinking-in-java
// * this is test code for learning java
// */
//package chapter14;
//
//import java.lang.reflect.Method;
//
//import net.sf.cglib.proxy.Enhancer;
//import net.sf.cglib.proxy.MethodInterceptor;
//import net.sf.cglib.proxy.MethodProxy;
//
//
///**
// *
// * CGlib proxy demo
// * @author Jverson
// *
// */
//public class CGLibProxy implements MethodInterceptor {
//
//	private Object proxied;
//
//	public CGLibProxy(Object proxied) {
//		this.proxied = proxied;
//	}
//
//	public Object getProxy(){
//		//cglib 中增强器，用来创建动态代理
//		Enhancer enhancer = new Enhancer();
//		//设置要创建动态代理的类
//		enhancer.setSuperclass(proxied.getClass());
//		//设置回调，这里相当于是对于代理类上所有方法的调用，都会调用CallBack，而Callback则需要实现intercept()方法进行拦截
//		enhancer.setCallback(this);
//		//创建代理类
//		return enhancer.create();
//	}
//
//	@Override
//	public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
//		System.out.println("cglib proxy start...");
//		proxy.invokeSuper(obj, args);
//		System.out.println("cglib proxy end...");
//		return null;
//	}
//
//}
