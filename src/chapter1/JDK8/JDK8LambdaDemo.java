///**
// * thinking-in-java
// * this is test code for learning java
// */
//package chapter1.JDK8;
//
//import java.util.Arrays;
//import java.util.List;
//import java.util.function.Consumer;
//import java.util.stream.Collectors;
//
//import chapter1.JDK8.Apple.Color;
//
///**
// * @author jverson
// */
//public class JDK8LambdaDemo {
//
//
//	public static void main(String[] args) {
//		Apple[] appleArr = {
//			new Apple(Color.GREEN, 2.2f, "baoji"),
//			new Apple(Color.GREEN, 1.2f, "shanxi"),
//			new Apple(Color.RED, 1.5f, "xinjiang"),
//			new Apple(Color.YELLOW, 2.5f, "tianjin")
//		};
//		List<Apple> apples = Arrays.asList(appleArr);
//
//		filterApplesByColorAndWeight(apples);
//
//		runableByLambda();
//
//		iterateListByLambda(apples);
//
//		filterApplesByPredicate(apples);
//
//	}
//
//
//
//	/**
//	 * @param apples
//	 */
//	private static void filterApplesByPredicate(List<Apple> apples) {
//		Apple.filter(apples, apple -> Color.GREEN.equals(apple.getColor()) && apple.getWeight()>2).forEach(System.out::println);;
//	}
//
//
//
//	/**
//	 * @param apples
//	 */
//	private static void iterateListByLambda(List<Apple> apples) {
//		//普通方式
//		for (Apple apple : apples) {
//			System.out.println(apple);
//		}
//
//		//文艺方式-lambda
//		apples.forEach(apple -> System.out.println(apple));
//
//		//更文艺方式-lambda & 方法引用
//		apples.forEach(System.out::println);
//	}
//
//
//
//	/**
//	 * lambda方式实现runnable
//	 */
//	private static void runableByLambda() {
//		//普通写法
//		new Thread(new Runnable() {
//			@Override
//			public void run() {
//				System.out.println("hahaha");
//			}
//		}).start();
//
//		//lambda 文艺写法
//		new Thread(() -> System.out.println("hehehe")).start();
//	}
//
//
//
//	private static void filterApplesByColorAndWeight(List<Apple> apples) {
//		//方式1：匿名内部类
//		List<Apple> filterApples = AppleFilter.filterApplesByAppleFilter(apples, new AppleFilter() {
//			@Override
//			public boolean accept(Apple apple) {
//				return Color.GREEN.equals(apple.getColor()) && apple.getWeight()>2;
//			}
//		});
//		filterApples.forEach(System.out::println);
//
//		//方式2：lambda表达式
//		int weightLimit = 2;
//		List<Apple> filterApples1 = AppleFilter.filterApplesByAppleFilter(apples, apple -> Color.GREEN.equals(apple.getColor()) && apple.getWeight()>weightLimit);
//		filterApples1.forEach(System.out::println);
//
//		//方式3：stream api方式
//		apples.stream().filter(apple -> Color.GREEN.equals(apple.getColor())).forEach(System.out::println);
//	}
//
//
//
//	//常规方法：筛选出绿色的苹果
//	public static List<Apple> filterGreenApples(List<Apple> apples){
//		/*写法1，常规遍历*/
//		/*List<Apple> filterApples = Lists.newArrayList();
//		for (Apple apple : apples) {
//			if (Color.GREEN.equals(apple.getColor())) {
//				filterApples.add(apple);
//			}
//		}*/
//
//		/*写法2，JDK8遍历*/
//		/*List<Apple> filterApples = Lists.newArrayList();
//		apples.forEach(apple -> {
//			if (Color.GREEN.equals(apple.getColor())) {
//				filterApples.add(apple);
//			}
//		});*/
//
//		/*写法2：使用JDK8 stream*/
//		List<Apple> filterApples = apples.stream().filter(apple -> Color.GREEN.equals(apple.getColor())).collect(Collectors.toList());
//
//
//		return filterApples;
//	}
//
//
//}
