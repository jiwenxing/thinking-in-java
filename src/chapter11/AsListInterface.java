/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * @author jverson
 * 添加元素及aslist用法
 */
public class AsListInterface {

	public static void main(String[] args) {
		/**
		 * asList：This method acts as bridge between array-based and collection-based APIs
		 * ArrayList构造函数接受一个实现Collection接口的类型作为参数，使用asList将一个数组转化为大小固定的List
		 */
		Collection<Integer> collection = new ArrayList<>(Arrays.asList(1,2,3,4,5));
		
		/**
		 * 几种不同的添加元素方法
		 * 注意Collections类包含了很多非常有用的操作集合类的静态方法
		 */
		Integer[] moreInts = {9,8,7,6};
		collection.addAll(Arrays.asList(moreInts));
		System.out.println(collection);
		Collections.addAll(collection, 11, 12);
		System.out.println(collection);
		Collections.addAll(collection, moreInts);
		System.out.println(collection);
		
		/**
		 * Arrays.asList得到的list其底层数据结构仍然是数组，因此不能调整尺寸，也就是说不能添加和删除元素
		 * 如果进行尺寸改变的操作便会抛出“java.lang.UnsupportedOperationException”
		 */
		List<Integer> list = Arrays.asList(1, 2, 3);
		list.add(99);
		System.out.println(list);
		
		/**
		 * 包括Map在内的所有容器类型都可以直接打印输出，而不像数组必须使用Arrays.toString()方法
		 */
		Map<String, String> map = new HashMap<String, String>();
		map.put("cat", "ketty");
		map.put("dog", "alaska");
		System.out.println(map);
		map.toString();
	}
	
}
