/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter7;

import java.util.Random;

/**
 * @author jverson
 *
 */
public class FinalData {
	private static Random rand = new Random(47);
	private String id;
	public FinalData(String id) {
		this.id = id;
	}
	private final int valueOne = 9;  //compile-time constants
	private  static final int VALUE_TWO = 99;  //compile-time constants
	private final int i4 = rand.nextInt(20);  //not compile-time constants，创建多个finalData对象时i4值会变化，但INT_5不变
	static final int INT_5 = rand.nextInt(20); //run-time constants
	
	private Value v1 = new Value(11);
	private final Value v2 = new Value(22);
	private static final Value v3 = new Value(33);
	public static void main(String[] args) {
		FinalData fd1 = new FinalData("fd1");
//		fd1.valueOne++;  //The final field FinalData.valueOne cannot be assigned
		fd1.v2.i++;  //v2是一个final的引用，代表其不能重新指向别的对象，但是指向的对象的值可被修改
//		fd1.v2 = new Value(55);  //不能再指向其它对象
		fd1.v1 = new Value(44);  //v1不是final，可以指向其它对象
	}
}

class Value{
	int i;
	public Value(int i) {
		this.i = i;
	}
}
