/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter6;

/**
 * @author jverson
 * 限制了Sundae构造函数的访问权限
 * 于是调用方不能再通过构造函数创建Sundae对象，而只能通过暴露的makeASundae方式去获取Sundae对象。
 * 还有一个效果就是私有的构造函数限制了Sundae被继承
 */
public class IceCream {

	public static void main(String[] args) {
		Sundae sundae = Sundae.makeASundae();
//		Sundae sundae2 = new Sundae();  //这里不可访问，会报错
		
		Sundae2.access().f();
		
		Integer a = 111;
		Integer b =  111;
		System.out.println(a==b);
	}
	
}

class Sundae{

	private Sundae() {}  //构造函数限制为private
	
	static Sundae makeASundae(){
		return new Sundae();
	}
	
}

//Sundae2使用了单例（singleton）单例模式，因为你始终只能创建一个Sundae2对象
class Sundae2{
	private Sundae2() {}  //构造函数限制为private
	private static Sundae2 sundae2 = new Sundae2();
	public static Sundae2 access() {
		return sundae2;
	}
	public void f() {}
}