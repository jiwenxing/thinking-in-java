/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter1.JDK8;

import java.util.List;

import com.google.common.collect.Lists;

@FunctionalInterface
public interface AppleFilter {

	boolean accept(Apple apple);
	
	public static List<Apple> filterApplesByAppleFilter(List<Apple> apples, AppleFilter filter) {
		List<Apple> filterApples = Lists.newArrayList();
		for (final Apple apple : apples) {
			if (filter.accept(apple)) {
			    filterApples.add(apple);
			}
		}
		return filterApples;
	}
}
