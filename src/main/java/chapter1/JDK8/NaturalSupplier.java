/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter1.JDK8;

import java.util.function.Supplier;
import java.util.stream.Stream;

public class NaturalSupplier implements Supplier<Long> {

	long value = 0;
	
	@Override
	public Long get() {
		this.value = value + 1;
		return this.value;
	}

	public static void main(String[] args) {
		Stream<Long> natural = Stream.generate(new NaturalSupplier()); //generate的入参是一个supplier接口的实现
		natural.map(x -> x*x).limit(10).forEach(System.out::println);
	}
	
}
