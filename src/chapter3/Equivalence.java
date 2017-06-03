package chapter3;

/**
 * 
 * 这里注意equals方法默认比较对象的引用，一些常见的类库都重写了equals方法
 * 例如下方的Integer对象，其重写的equals方法如下：
   public boolean equals(Object obj) {
        if (obj instanceof Integer) {
            return value == ((Integer)obj).intValue();
        }
        return false;
    }
 * 但当比较自定义的对象时，一定要注意重写一下equals方法，重写equals还有一些注意事项，后面第七章会讲到
 * @author jverson
 *
 */

public class Equivalence {

	public static void main(String[] args) {
		Integer n1 = new Integer(12);
		Integer n2 = new Integer(12);
		System.out.println(n1==n2);  //false
		System.out.println(n1.equals(n2));  //true
		
		String s1 = "hello";
		String s2 = "hello";
		System.out.println(s1==s2); //true
		
		String s3 = new String("hello");
		String s4 = new String("hello");
		System.out.println(s3==s4); //false
	}
	
}
