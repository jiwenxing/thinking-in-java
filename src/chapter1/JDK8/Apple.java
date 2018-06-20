/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter1.JDK8;

import java.util.List;
import java.util.function.Predicate;

import com.google.common.collect.Lists;

public class Apple {

	/** 颜色 */
	private Color color;
	/** 重量 */
	private float weight;
	/** 产地 */
	private String origin;
	public Apple() {
	}
	public Apple(Color color, float weight, String origin) {
	this.color = color;
	this.weight = weight;
	this.origin = origin;
	}
	
	enum Color{
		RED, GREEN, YELLOW
	}

	public Color getColor() {
		return color;
	}
	public void setColor(Color color) {
		this.color = color;
	}
	public float getWeight() {
		return weight;
	}
	public void setWeight(float weight) {
		this.weight = weight;
	}
	public String getOrigin() {
		return origin;
	}
	public void setOrigin(String origin) {
		this.origin = origin;
	}
	@Override
	public String toString() {
		return "Apple [color=" + color + ", weight=" + weight + ", origin=" + origin + "]";
	}
	
	public static List<Apple> filter(List<Apple> apples, Predicate<Apple> condition) {
		List<Apple> filterApples = null;
	    for(Apple apple: apples)  {
	        if(condition.test(apple)) {
	        	if (filterApples==null) {
	        		filterApples = Lists.newArrayList();
				}
	        	filterApples.add(apple);
	        }
	    }
	    return filterApples;
	}
}
