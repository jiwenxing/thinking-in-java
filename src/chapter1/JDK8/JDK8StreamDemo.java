/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter1.JDK8;

import static java.util.stream.Collectors.summarizingDouble;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.Map;
import static java.util.stream.Collectors.*;
import java.util.stream.Stream;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import chapter1.JDK8.Apple.Color;

/**
 * @author jiwenxing
 *
 */
public class JDK8StreamDemo {

	public static void main(String[] args) {
		Apple[] appleArr = {
			new Apple(Color.GREEN, 2.2f, "baoji"), 
			new Apple(Color.GREEN, 1.2f, "shanxi"), 
			new Apple(Color.RED, 1.5f, "xinjiang"),
			new Apple(Color.YELLOW, 2.5f, "tianjin"),
			new Apple(Color.YELLOW, 0.5f, "shanxi")
		};
		List<Apple> apples = Arrays.asList(appleArr);
		
		List<String> strings = Arrays.asList("d", null, "a", "c", "f", "a", "x", "n");
		
		List<Integer> integers = Arrays.asList(5, 8, 2, 3, 1, 9, 2);
		
		Sets.newHashSet().stream();
		Lists.newArrayList().stream();
		
		/*创建流*/
		// 数组->流
		String[] strs = {"A", "B", "C", "D"};
		Stream<String> stream = Arrays.stream(strs);
		// 文件->流
		System.out.println("-------文件->流------");
		try {
			Stream<String> fileStream = Files.lines(Paths.get("d://a.txt"), Charsets.UTF_8);
			List<String> lines = fileStream.filter(s -> s!=null && s.startsWith("K_")).collect(toList());
			System.out.println(lines);
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		// iteartor  接受Function参数，0 为seed，n -> n + 2 为 Function 接口的抽象方法 R apply(T t) 的实现； 从0开始打印51个偶数
		Stream.iterate(0, n -> n + 2).limit(51).forEach(System.out::println);  
		// generate  接受Supplier参数，() -> "Hello Man!" 为 Supplier接口的抽象方法 T get()的实现; 重复打印10个"Hello Man!"
		Stream.generate(() -> "Hello Man!").limit(10).forEach(System.out::println);

		
		// 去重
		System.out.println("-------distinct------");
		strings.stream().filter(s -> s!=null).distinct().forEach(System.out::println);
		
		// 筛选(对流的元素过滤)
		System.out.println("-------filter------");
		apples.stream().filter(apple -> apple.getWeight()>2).forEach(System.out::println);
		
		// 排序(对流的元素排序)
		System.out.println("-------sort------");
		apples.stream().sorted((Apple a, Apple b) -> Float.valueOf(a.getWeight()).compareTo(b.getWeight())).forEach(System.out::println);;
		
		// 映射(将流的元素映射成另一个类型)
		System.out.println("-------map------");
		List<String> origins = apples.stream().map(apple -> apple.getOrigin()).collect(toList());
		System.out.println(origins);
		
		// 查找和匹配(将流的元素映射成另一个类型)
		System.out.println("-------查找任意元素匹配------");
		System.out.println(apples.stream().anyMatch(apple -> apple.getWeight()>2 && apple.getOrigin().equals("baoji")));
		System.out.println("-------查找所有元素匹配------");
		System.out.println(apples.stream().allMatch(apple -> apple.getWeight()>2));
		System.out.println("-------查找任意元素没有匹配------");
		System.out.println(apples.stream().noneMatch(apple -> apple.getWeight()>2));
		System.out.println("-------查找元素------");
		System.out.println(apples.stream().filter(apple -> apple.getWeight()>2).count());
		System.out.println(apples.stream().filter(apple -> apple.getWeight()>2).findFirst()); //返回满足条件第一个元素
		System.out.println(apples.stream().filter(apple -> apple.getWeight()>2).findAny()); //返回满足条件任一元素
		
		/*归约操作（求和，求最大值或最小值）*/
		System.out.println("-------计算所有苹果总重------");
		System.out.println(apples.stream().map(Apple::getWeight).reduce((n,m) -> n+m).get()); //reduce操作接收BinaryOperator类型参数，它继承了BiFunction，接受两个类型相同的参数
		
		//T reduce(T identity, BinaryOperator accumulator)
		int value = Stream.of(1, 2, 3, 4).reduce(100, (sum, item) -> sum + item); //110, 100即为计算初始值，每次相加计算值都会传递到下一次计算的第一个参数。
		//最大最小值
		System.out.println(apples.stream().max(Comparator.comparing(Apple::getWeight)).get());
		System.out.println(apples.stream().min(Comparator.comparing(Apple::getWeight)).get());
		System.out.println(apples.stream().count());
		
		DoubleSummaryStatistics dss = apples.stream().collect(summarizingDouble(Apple::getWeight));
		double sum = dss.getSum();          // 汇总
		double average = dss.getAverage();  // 求平均数
		long count = dss.getCount();        // 计算总数
		double max = dss.getMax();          // 最大值
		double min = dss.getMin();
		
		// 苹果按照产地分组，打印产自陕西的分组
		Map<String, List<Apple>> originGroup = apples.stream().collect(groupingBy(Apple::getOrigin));
		System.out.println(originGroup.get("shanxi"));
		
		// 苹果按照重量区间分组，打印大号的分组
		Map<String, List<Apple>> weightGroup = apples.stream().collect(groupingBy(apple -> {
			if (apple.getWeight()<1) {
				return "small";
			}else if (apple.getWeight()<2 && apple.getWeight()>1) {
				return "medium";
			}else {
				return "big";
			}
		})); 
		System.out.println("-----big----");
		System.out.println(weightGroup.get("big"));

		
	}
	
	
}
