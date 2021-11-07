/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter1_all;

import java.util.LinkedHashMap;

/**
 * @author Jverson
 *
 */
public class LinkedHashMapTest {

	public static void main(String[] args) {
		LinkedHashMap<Integer, Integer> lhMap = new LinkedHashMap<Integer, Integer>();
		for(int i=1; i<6; i++){
			lhMap.put(i*2, i);
		}
		System.out.println(lhMap); //{2=1, 4=2, 6=3, 8=4, 10=5}
		lhMap.put(3, 1);
		lhMap.get(4);
		lhMap.put(12, 6);
		System.out.println(lhMap); //{2=1, 4=2, 6=3, 8=4, 10=5, 3=1, 12=6} 完全按照插入顺序
		
        LinkedHashMap<Integer, Integer> lruMap = new LinkedHashMap<Integer, Integer>(20, 0.75f, true);
		for(int i=1; i<6; i++){
			lruMap.put(i*2, i);
		}
        System.out.println(lruMap); //{2=1, 4=2, 6=3, 8=4, 10=5}
        lruMap.put(3, 1);
        lruMap.get(4);
        lruMap.put(12, 6);
		System.out.println(lruMap); //{2=1, 6=3, 8=4, 10=5, 3=1, 4=2, 12=6} 符合LRU规则，每当我get或者put一个已存在的数据，就会把这个数据放到双向链表的尾部，put一个新的数据也会放到双向链表的尾部。
	}
	
	
	
	
}
