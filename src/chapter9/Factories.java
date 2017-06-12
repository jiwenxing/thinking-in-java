/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter9;

/**
 * @author jverson
 *
 */
interface Service {
	void method1();
	void method2();
}

interface ServiceFactory{
	Service getService();
}

class impl1 implements Service{
	@Override
	public void method1() {
		System.out.println("impl1.method1");
		
	}
	@Override
	public void method2() {
		System.out.println("impl1.method2");
	}
}

class impl1Factory implements ServiceFactory{
	@Override
	public Service getService() {
		return new impl1();
	}
}

class impl2 implements Service{
	@Override
	public void method1() {
		System.out.println("impl2.method1");
		
	}
	@Override
	public void method2() {
		System.out.println("impl2.method2");
	}
}

class impl2Factory implements ServiceFactory{
	@Override
	public Service getService() {
		return new impl2();
	}
}

public class Factories{
	public static void serviceConsumer(ServiceFactory serviceFactory){
		Service service = serviceFactory.getService();
		service.method1();
		service.method2();
	}
	public static void main(String[] args) {
		serviceConsumer(new impl1Factory());
		serviceConsumer(new impl2Factory());
	}
}