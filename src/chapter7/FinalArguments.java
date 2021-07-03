/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter7;

/**
 * @author jverson
 *
 */
public class FinalArguments {
	void with(final Gizmo gizmo){
//		gizmo = new Gizmo();  //illegal because gizmo is final
	}
	void f(final int i){
//		i++;  //illegal because i is final
	}
	int g(final int i){
		return i+1;  // no problem
	}
}
class Gizmo{
	public void spin(){}
}