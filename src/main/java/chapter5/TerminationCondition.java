/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter5;

/**
 * @author jverson
 * 如果重写了finalize方法，则该对象在被回收之前会先执行finalize方法
 */
public class TerminationCondition {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		Book book = new Book(true);
		book.checkin();
		new Book(true); //该对象没有引用指向它，会被回收
		System.gc(); //System.gc()会显式直接触发Full GC，同时对老年代和新生代进行回收，一般在程序中很少使用
	}
	
}
