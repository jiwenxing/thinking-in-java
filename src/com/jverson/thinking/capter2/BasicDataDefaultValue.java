package com.jverson.thinking.capter2;
/**
 * 
 * 如果类的成员变量是基本数据类型，即使没有初始化，java也会确保其获得一个默认值
 * 但这个默认值可能不是你想要的，因此最好明确的对变量进行初始化。
 * 注意这里说的是类成员变量，如果局部变量没有初始化，java会在编译时直接报错（sum方法中a和b必须初始化）
 * 
 * @author jverson
 *
 */
public class BasicDataDefaultValue {

	int i;
	double d;
	float f;
	
	static int sum(){
		int a = 0;
		int b = 0;
		return a+b;
	}
	
	public static void main(String[] args) {
		BasicDataDefaultValue basicDataDefaultValue = new BasicDataDefaultValue();
		System.out.println(basicDataDefaultValue.i);  //0
		System.out.println(basicDataDefaultValue.d);  //0.0
		System.out.println(basicDataDefaultValue.f);  //0.0
		
		System.out.println(sum());
	}
	
}
