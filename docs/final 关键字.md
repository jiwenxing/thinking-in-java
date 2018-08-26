java中final关键字可以用于修饰数据、参数、方法以及类，通常想表达的意思是“这是无法改变的”，但是不同的语境其含义仍有很多细微的差别。

## final 数据

final修饰基本数据类型时，表示其数值恒定不变；修饰一个对象引用时，一旦引用被初始化指向一个对象，则无法再将其更改为指向另一个对象，但是对象其自身还是可以被修改的。

```java
public class FinalData {
	private static Random rand = new Random(47);
	private String id;
	public FinalData(String id) {
		this.id = id;
	}
	private final int valueOne = 9;  //compile-time constants
	private  static final int VALUE_TWO = 99;  //compile-time constants
	private final int i4 = rand.nextInt(20);  //not compile-time constants，创建多个finalData对象时i4值会变化，但INT_5不变
	static final int INT_5 = rand.nextInt(20); //run-time constants
	
	private Value v1 = new Value(11);
	private final Value v2 = new Value(22);
	private static final Value v3 = new Value(33);
	public static void main(String[] args) {
		FinalData fd1 = new FinalData("fd1");
//		fd1.valueOne++;  //The final field FinalData.valueOne cannot be assigned
		fd1.v2.i++;  //v2是一个final的引用，代表其不能重新指向别的对象，但是指向的对象的值可被修改
//		fd1.v2 = new Value(55);  //不能再指向其它对象
		fd1.v1 = new Value(44);  //v1不是final，可以指向其它对象
	}
}

class Value{
	int i;
	public Value(int i) {
		this.i = i;
	}
}
```

注意final数据要确保在使用前必须被初始化，但是不一定要在定义的时候就初始化，可以使用空白final，然后在构造器中对其初始化，这样可以提供更大的灵活性，实现根据对象而有所不同但还保持恒定不变。如下：

```java
public class BlackFinal {
	private final int i = 0;  //initialize final
	private final int j;  //black final
	private final Poppet p;  //black final reference
	public BlackFinal(int x) {
		j = x;
		p = new Poppet(j);
	}
}
class Poppet{
	private int i;
	public Poppet(int i) {
		this.i = i;
	}
}
```

## final 参数

final 允许在参数列表中将参数声明为final，这将意味着在方法中无法修改参数的值或者修改参数引用指向的对象。

```java
public class FinalArguments {
	void with(final Gizmo gizmo){
//		gizmo = new Gizmo();  //illegal because gizmo is final
	}
	void f(final int i){
//		i++;  //illegal because i is final
	}
	int g(final int i){
		return i+1;  // no problem
	}
}
class Gizmo{
	public void spin(){}
}
```

## final 方法

final 修饰方法的目的是确保在继承中方法行为保持不变，并且不会被覆盖。注意**类中所有private方法都会隐式的指定为final的**。

## final 类
当某个类整体定义为final时表示该类禁止被继承，，就是说处于某种考虑你希望这个类永远不需要做任何变动。