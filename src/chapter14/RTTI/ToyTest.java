/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter14.RTTI;

/**
 * @author Jverson
 *
 */
public class ToyTest {

	static void printInfo(Class cc){
		System.out.println(cc.getName()+"\n"+cc.isInterface()+"\n"+cc.getSimpleName()+"\n"+cc.getCanonicalName());
	}
	
	public static void main(String[] args) {
		Class clazz = null;
		try {
			clazz = Class.forName("chapter14.RTTI.FancyToy");
		} catch (Exception e) {
			e.printStackTrace();
		}
		printInfo(clazz);

	}

}
