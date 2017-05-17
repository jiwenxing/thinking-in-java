
package com.jverson.thinking.capter3;

/**
 * 
 * java中对float和double转为整型值时默认是截尾
 * 如果想达到四舍五入的效果，需要使用java.lang.Math的round方法
 * @author jverson
 *
 */
public class CastingNumbers {

	public static void main(String[] args) {
		double d1 = 0.7, d2 = 0.2;
		float f1 = 1.7f, f2 = 1.2f;
		//默认截尾
		System.out.println((int)d1); //0
		System.out.println((int)d2); //0
		System.out.println((int)f1); //1
		System.out.println((int)f2); //1
		//使用Math.round进行四舍五入
		System.out.println(Math.round(d1)); //1
		System.out.println(Math.round(d2)); //0
		System.out.println(Math.round(f1)); //2
		System.out.println(Math.round(f2)); //1
	}
	
	
	
}
