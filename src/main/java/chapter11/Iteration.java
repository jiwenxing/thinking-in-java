/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/**
 * @author jverson
 * 迭代器
 */
public class Iteration {
	
	public static void main(String[] args) {
		List<String> list = new ArrayList<String>(Arrays.asList("wo","shi","zhong","guo","ren"));
		
		//使用迭代器遍历容器
		Iterator<String> it = list.iterator();
		while(it.hasNext()){
			System.out.println(it.next());
		}
		
		//使用foreach遍历容器
		for (String string : list) {
			System.out.println(string);
		}
		
		//使用迭代器删除某个特定元素
		it = list.iterator(); //重新将迭代器放回至第一个元素，要不然经过前面的循环it.hasNext()直接就是false
		while(it.hasNext()){
			if (it.next().equals("zhong")) {
				it.remove();  //可以直接将列表中的元素删除，但是调用remove方法之前必须先调用next方法
			}
		}
		System.out.println(list); //print [wo, shi, guo, ren]
		
//		list = new ArrayList<String>(Arrays.asList("wo","shi","zhong","guo","ren"));
		for (String string : list) {
			if (string.equals("wo")) {
				list.remove(string);
			}
		}
		System.out.println(list);
	}

}
