///**
// * thinking-in-java
// * this is test code for learning java
// */
//package chapter1.JDK8;
//
//import java.util.List;
//
//
//@FunctionalInterface
//public interface AppleFilter {
//
//	boolean accept(Apple apple);
//
//	default List<Apple> filterApplesByAppleFilter(List<Apple> apples, AppleFilter filter) {
//		List<Apple> filterApples = Lists.newArrayList();
//		for (final Apple apple : apples) {
//			if (filter.accept(apple)) {
//			    filterApples.add(apple);
//			}
//		}
//		return filterApples;
//	}
//}
