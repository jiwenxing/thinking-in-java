/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter7;

/**
 * @author jverson
 * 从输出可以看到继承类的初始化顺序
 */
class Insect{
	private int i = 9;
	protected int j;
	public Insect() {
		System.out.println("i = " + i + ", j = " + j);
		j=39;
	}
	private static int x1 = printInt("static Inset.x1 initialezed");
	static int printInt(String s){
		System.out.println(s);
		return 47;
	}
}

public class Beetle extends Insect{
    private int k = printInt("Beetle.k initialized");
	public Beetle() {
		System.out.println("k = " + k);
		System.out.println("j = " + j);
	}
	private static int x2 = printInt("static Beetle.x2 initialized");
	public static void main(String[] args) {
		Beetle beetle = new Beetle();
	}
}
