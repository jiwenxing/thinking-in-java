# Java 代理
---

代理是基本的设计模式之一，它是你为了提供额外的或不同的操作而插入的用以代替实际的“对象”的对象，该代理对象通常继承自实际对象或将实际对象作为自己的成员变量，因此能够在提供额外操作的同时与“实际对象”通信调用其原有的功能。

> 代理模式定义：给某一个对象提供一个代理，并由代理对象控制对原对象的引用。

代理模式包含如下角色：

![](http://ochyazsr6.bkt.clouddn.com/87728b697edf17a7988228877bb8fd28.jpg)


* ISubject: 抽象主题角色，是一个接口。该接口是对象和它的代理共用的接口。
* RealSubject: 真实主题角色，是实现抽象主题接口的类。
* Proxy: 代理角色，内部含有对真实对象RealSubject的引用，从而可以操作真实对象。代理对象提供与真实对象相同的接口，以便在任何时刻都能代替真实对象。同时，代理对象可以在执行真实对象操作时，附加其他的操作，相当于对真实对象进行封装。

代理以代理类创建方式的不同可以划分为静态代理和动态代理两种。

## 静态代理

所谓静态代理是指：由程序员创建或特定工具自动生成源代码，再对其编译。在程序运行前，代理类的.class文件就已经存在了。 

```java
interface Subject {
    void request();
}

class RealSubject implements Subject {
    public void request(){
        System.out.println("RealSubject");
    }
}

class Proxy implements Subject {
    private Subject subject;

    public Proxy(Subject subject){
        this.subject = subject;
    }
    public void request(){
        System.out.println("begin");
        subject.request();
        System.out.println("end");
    }
}

public class ProxyTest {
    public static void main(String args[]) {
        RealSubject subject = new RealSubject();
        Proxy p = new Proxy(subject);
        p.request();
    }
}
```

从上例可以看到静态代理实现中，一个委托类对应一个代理类，代理类在编译期间就已经确定。另外，如果没有使用接口，代理类也可以通过继承委托类实现静态代理，和上面类似就不再举例。
 
## 动态代理

动态代理：代理类是在程序运行时运用反射机制动态创建而成。主要有 JDK 动态代理和 CGLib 动态代理。

JDK 实现动态代理需要实现类通过接口定义业务方法，对于没有接口的类，如何实现动态代理呢，这就需要 CGLib了。CGLib 采用了非常底层的字节码技术，其原理是通过字节码技术为一个类创建子类(继承的方式)，并在子类中采用方法拦截的技术拦截所有父类方法的调用，顺势织入横切逻辑。JDK 动态代理与 CGLib 动态代理均是实现 Spring AOP 的基础。

### JDK 动态代理

JDK 动态代理类的字节码在程序运行时由 Java 反射机制动态生成，无需手工编写它的源代码。动态代理类不仅简化了编程工作，而且提高了软件系统的可扩展性，因为 Java 反射机制可以生成任意类型的动态代理类。`java.lang.reflect` 包中的`Proxy`类和`InvocationHandler`接口提供了生成动态代理类的能力。
 
一、定义业务接口及实现

```java
public interface MyInterface {
	void doSth();	
	void doSthElse(String args);	
}

public class RealObject implements MyInterface {
	@Override
	public void doSth() {
		System.out.println("do something");
	}

	@Override
	public void doSthElse(String args) {
		System.out.println("do sth else "+ args);
	}
}
```

二、实现InvocationHandler接口，并调用Proxy的静态方法创建一个代理类

```java
public class MyInvocatioHandler implements InvocationHandler {

	private Object target; //添加被代理类引用

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
		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		//类可以实现多个接口，因此这里的接口是个数组
		Class<?>[] interfaces = target.getClass().getInterfaces();
		//this即MyInvocatioHandler实例，其包含被代理类的引用，以及重写的方法，newProxyInstance方法将利用这些参数创建一个代理类的实例
		return Proxy.newProxyInstance(loader, interfaces, this);
	}

}
```

### 通过代理调用方法

```java
public class JDKProxyTest {

	public static void main(String[] args) {
	    // 1. 创建被代理实例
		RealObject realObject = new RealObject();
		// 2. 创建自己实现的InvocatioHandler实例
		MyInvocatioHandler handler = new MyInvocatioHandler(realObject);
		// 3. 创建代理实例
		MyInterface myInterface = (MyInterface) handler.getProxy();
		// 4. 通过代理调用方法
		myInterface.doSth();
	}
	
}
```

### CGLib 动态代理

JDK 中提供的生成动态代理类的机制有个鲜明的特点是：某个类必须有实现的接口，如果某个类没有实现接口，那么这个类就不能通过 JDK 产生动态代理了！不过幸好我们有 CGLib。CGLIB（Code Generation Library）是一个强大的、高性能、高质量的Code生成类库，它可以在运行期扩展Java类与实现Java接口。

CGLib 创建某个类 `A` 的动态代理类的模式是：

1.   查找 A 上的所有非 final 的 public 类型的方法定义；
2.   将这些方法的定义转换成字节码；
3.   将组成的字节码转换成相应的代理的 class 对象；
4.   实现 MethodInterceptor 接口，用来处理 对代理类上所有方法的请求（这个接口和JDK动态代理InvocationHandler的功能和角色是一样的）

下面看一个具体实现：

一、首先定义一个委托类，注意就是一个普通的类

```java
public final class Train {
	public void move(){  
        System.out.println("train running…");  
    }  	
}
```

二、实现`MethodInterceptor`方法

```java
public class CGLibProxy implements MethodInterceptor {

	private Object proxied;
	
	public CGLibProxy(Object proxied) {
		this.proxied = proxied;
	}
	
	public Object getProxy(){
		//cglib 中增强器，用来创建动态代理
		Enhancer enhancer = new Enhancer();
		//设置要创建动态代理的类
		enhancer.setSuperclass(proxied.getClass());
		//设置回调，这里相当于是对于代理类上所有方法的调用，都会调用CallBack，而Callback则需要实现intercept()方法进行拦截  
		enhancer.setCallback(this);
		//创建代理类
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
```

三、测试代理类

```java
public static void main(String[] args) {
	CGLibProxy proxy = new CGLibProxy(new Train());
	Train t = (Train)proxy.getProxy();
	t.move();
}
```


另外注意由于 CGLib 动态代理采用的是继承委托类的方式，因此不能代理 final 修饰的类。如果将上例中的类`Train`添加 final 修饰符，再次运行则会看到如下错误信息：
> Exception in thread "main" java.lang.IllegalArgumentException: Cannot subclass final class chapter14.Train
	at net.sf.cglib.proxy.Enhancer.generateClass(Enhancer.java:565)
	at net.sf.cglib.core.DefaultGeneratorStrategy.generate(DefaultGeneratorStrategy.java:25)
	at net.sf.cglib.core.AbstractClassGenerator.generate(AbstractClassGenerator.java:329)

### 两种动态代理区别总结

Java 动态代理是利用反射机制生成一个实现代理接口的匿名类，在调用具体方法前调用 InvokeHandler 来处理。而 CGLIB 动态代理是利用 ASM 开源包，对代理对象类的 class 文件加载进来，通过修改其字节码生成子类来处理。

*tips: [ASM](http://asm.ow2.org/)*
> ASM is an all purpose Java bytecode manipulation and analysis framework. It can be used to modify existing classes or dynamically generate classes, directly in binary form. Provided common transformations and analysis algorithms allow to easily assemble custom complex transformations and code analysis tools.

## 使用场景

### 实现 AOP 功能

Spring 的 AOP 功能就是利用动态代理的原理实现的。其会根据被代理对象是否实现了接口选择不同的生成代理对象的方式，如果被代理对象实现了需要被代理的接口，则使用 JDK 的动态代理，否则便使用 CGLIB 代理。

1. 如果目标对象实现了接口，默认情况下会采用 JDK 的动态代理实现 AOP，对应的包装类为 `JdkDynamicAopProxy`。
2. 如果目标对象实现了接口，可以强制使用 CGLIB 实现 AOP 
3. 如果目标对象没有实现了接口，必须采用 CGLIB 库，spring 会自动在 JDK 动态代理和 CGLIB 之间转换


### 延迟加载

延迟加载的核心思想是：如果当前并没有使用这个组件，则不需要真正地初始化它，使用一个代理对象替代它的原有的位置，只要在真正需要的时候才对它进行加载。

使用代理模式的延迟加载是非常有意义的，首先，它可以在时间轴上分散系统压力，尤其在系统启动时，不必完成所有的初始化工作，从而加速启动时间；其次，对很多真实主题而言，在软件启动直到被关闭的整个过程中，可能根本不会被调用，初始化这些数据无疑是一种资源浪费。

例如使用代理类封装数据库查询类后，系统的启动过程这个例子。若系统不使用代理模式，则在启动时就要初始化 DBQuery 对象，而使用代理模式后，启动时只需要初始化一个轻量级的对象 DBQueryProxy。

延迟加载代码示例：
    
```java
public interface IDBQuery {
	String request();
}

public class DBQuery implements IDBQuery {
	public DBQuery() {
		try {
			Thread.sleep(1000);// 假设数据库连接等耗时操作
		} catch (InterruptedException ex) {
			ex.printStackTrace();
		}
	}

	@Override
	public String request() {
		// TODO Auto-generated method stub
		return "request string";
	}

}

public class DBQueryProxy implements IDBQuery {
	private DBQuery real = null;

	@Override
	public String request() {
		// TODO Auto-generated method stub
		// 在真正需要的时候才能创建真实对象，创建过程可能很慢
		if (real == null) {
			real = new DBQuery();
		} // 在多线程环境下，这里返回一个虚假类，类似于 Future 模式
		return real.request();
	}

}

public class Main {
	public static void main(String[] args) {
		IDBQuery q = new DBQueryProxy(); // 使用代里
		q.request(); // 在真正使用时才创建真实对象
	}
}
```


## References

- [代理模式原理及实例讲解（主要讲解了延迟加载）](https://www.ibm.com/developerworks/cn/java/j-lo-proxy-pattern/index.html)
- [Java设计模式——代理模式实现及原理](http://blog.csdn.net/goskalrie/article/details/52458773)