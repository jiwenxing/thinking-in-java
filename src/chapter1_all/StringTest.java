///**
// * thinking-in-java
// * this is test code for learning java
// */
//package chapter1_all;
//
//import org.apache.commons.lang3.StringUtils;
//
//import com.sun.org.apache.bcel.internal.generic.NEW;
//
//import sun.applet.AppletClassLoader;
//
///**
// * @author Jverson
// *
// */
//public class StringTest {
//
//	/**
//	 * @param args
//	 */
//	public static void main(String[] args) {
//		String a = "hello";
//		String b = "hello";
//		String c = new String("hello");
//		String d = new String("hello");
//		System.out.println(a.hashCode() == b.hashCode()); //true
//		System.out.println(a.hashCode() == c.hashCode()); //true
//		System.out.println(a.equals(b)); //true
//		System.out.println(a.equals(c)); //true
//		System.out.println(a == b); //true
//		System.out.println(a == c); //false
//		System.out.println(c == d); //false
//
//		String hello = "Hello", lo = "lo";
//		System.out.println((hello == ("Hel"+"lo"))); //true,"Hel"+"lo"在编译时进行计算，被当做常量
//		System.out.println((hello == ("Hel"+lo))); //false，在运行时通过连接计算出的字符串是新创建的，因此是不同的
//		System.out.println(hello == ("Hel"+lo).intern()); //true，通过使用字符串的intern()方法来指向字符串池中的对象
//
//		Integer int1 = 127;
//		Integer int2 = 127;
//		System.out.println(int1 == int2);
//
//		String A="Aa";
//		String B="BB";
//		int aa=A.hashCode();
//		int bb=B.hashCode();
//		System.out.println(aa+":"+bb);
//		Object object = new Object();
//		object.toString();
//
//		String aaa = "10";
//		System.out.println(Integer.valueOf(aaa));
//
//		String str = "abcd";
//		String repeated = StringUtils.repeat(str,3); //repeat 实现
//
//		int n = StringUtils.countMatches("11112222", "1");
//
//		StringBuffer sBuffer = new StringBuffer();
//
//		StringBuilder sBuilder = new StringBuilder();
//
//		ClassLoader classLoader;
//		ExtClassLoader loader;
//
//	}
//
//
//}
