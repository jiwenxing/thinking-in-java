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
			/**
			 *  通过‘Class.forName’方法可以创建一个指定Class类的引用（Class也是类），其实就是调用类加载器去加载指定类的class文件
			 *  有了这个引用，便可以在运行时获取到类型信息
			 */
//			clazz = Class.forName("chapter14.RTTI.FancyToy"); 
			/**
			 * 也可以像下面这样通过类字面常量来生成对class对象的引用（推荐使用）
			 */
			clazz = FancyToy.class;
		} catch (Exception e) {
			e.printStackTrace();
		}
//		printInfo(clazz);
		for (Class cc : clazz.getInterfaces()) {
//			printInfo(cc);
		}
		Class up = clazz.getSuperclass();
//		printInfo(up);
		
		try {
			/**
			 * 1. 此时up只是一个Class引用，并不知道任何类型信息，调用‘newInstance’方法可以获得一个up实例对象的引用，因为还不知道类型信息
			 * 因此该引用只是一个object对象，但是该引用指向的是一个toy对象
			 * 2. 另外注意，如果已经有一个感兴趣类的对象，要想获取其Class对象的引用，只需要调用‘getClass’方法就好了
			 */
			Object ob = up.newInstance();
			printInfo(ob.getClass());
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

	}

}
