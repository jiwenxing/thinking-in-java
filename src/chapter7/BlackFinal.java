/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter7;

/**
 * @author jverson
 * 展示空白final的使用方法
 */
public class BlackFinal {
	private final int i = 0;  //initialize final
	private final int j;  //black final
	private final Poppet p;  //black final reference
	public BlackFinal(int x) {
		j = x;
		p = new Poppet(j);
	}
}
class Poppet{
	private int i;
	public Poppet(int i) {
		this.i = i;
	}
}