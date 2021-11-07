/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter8;

/**
 * @author jverson
 *
 */
class Useful {
	public void f(){
		System.out.println("Useful.f");
	};
	public void g(){
		System.out.println("Useful.g");
	};
}

class MoreUseful extends Useful {
	public void f(){
		System.out.println("MoreUseful.f");
	};
	public void g(){
		System.out.println("MoreUseful.g");
	};
	public void h(){
		System.out.println("MoreUseful.h");
	};
}

public class RTTI {
	static void testCast(Useful useful){
		useful.f();
	}
	public static void main(String[] args) {
		Useful useful = new MoreUseful();
		((MoreUseful) useful).f();  //输出MoreUseful.f
		
		Useful useful1 = new MoreUseful();
		((MoreUseful) useful1).h();  //输出MoreUseful.h
		
//		Useful useful1 = new Useful();
//		((MoreUseful) useful1).f(); //向下转型，这时会抛出java.lang.ClassCastException
		
		MoreUseful moreUseful = new MoreUseful();
		testCast(moreUseful); //向上转型,多态，输出MoreUseful.f
	}
	
}
