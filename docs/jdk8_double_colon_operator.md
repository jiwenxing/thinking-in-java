# JDK8 中双引号操作符及其使用方法
---

通过前面的了解，我们习惯使用 Lambdas 表达式可以让代码变得很简洁，比如我们创建一个比较器 Comparator

```java
Comparator c = (Computer c1, Computer c2) -> c1.getAge().compareTo(c2.getAge());
```

进一步使用类型推断

```java
Comparator c = (c1, c2) -> c1.getAge().compareTo(c2.getAge());
```

再进一步，使用双引号表达式让代码变得更加易读和简洁

```java
Comparator c = Comparator.comparing(Computer::getAge);
```


其实所谓的双引号表达式 `double colon operator ( :: )` 是 Lambdas 表达式中调用方法的一种缩写形式，即直接通过方法名调用方法，是一种可读性更好的写法。

## How Does It Work?

其实双引号表达式就是创建一个方法引用（嗯？经常创建对象引用，没听说过方法也可以创建引用），双引号前面是对象引用，后面方法名称。例如：

```java
Computer::getAge;
```

我们可以创建一个方法引用指向这个方法，然后在需要使用的时候讲方法应用于参数即可

```java
Function<Computer, Integer> getAge = Computer::getAge;
Integer computerAge = getAge.apply(c1);
```

## Method References

方法引用有很多种，它们的语法如下，需要注意的是，静态方法引用和类型上的实例方法引用拥有一样的语法。编译器会根据实际情况做出决定。

一般我们不需要指定方法引用中的参数类型，因为编译器往往可以推导出结果，但如果需要我们也可以显式在 :: 分隔符之前提供参数类型信息。

- 静态方法引用：ClassName::methodName    
```java
Function<String, String> upperfier = String::toUpperCase;
```

- 实例上的实例方法引用：instanceReference::methodName    
```java
Set<String> knownNames = ...
Predicate<String> isKnown = knownNames::contains;
```

- 超类上的实例方法引用：super::methodName    
```java
public Double calculateValue(Double initialValue) {
    return initialValue/1.50;
}
//method in MacbookPro subclass
@Override
public Double calculateValue(Double initialValue){
    Function<Double, Double> function = super::calculateValue;
    Double pcValue = function.apply(initialValue);
    return pcValue + (initialValue/10) ;
}
```

- 类型上的实例方法引用：ClassName::methodName    
```java
Computer c1 = new Computer(2015, "white", 100);
Computer c2 = new MacbookPro(2009, "black", 100);
List inventory = Arrays.asList(c1, c2);
inventory.forEach(Computer::turnOnPc);
```

- 构造方法引用：Class::new    
```java
SocketImplFactory factory = MySocketImpl::new;
```

- 数组构造方法引用：TypeName[]::new    
```java
Function <Integer, Computer[]> computerCreator = Computer[]::new;
Computer[] computerArray = computerCreator.apply(5);
```

## Conclusion

其实，JVM 本身并不支持指向方法引用，过去不支持，现在也不支持。Java 8 对方法引用的支持知识编译器层面的支持，虚拟机执行引擎并不了解方法引用。编译器遇到方法引用的时候，会像上面那样自动推断出程序员的意图，将方法引用还原成 接口实现对象，或者更形象地说，就是把方法引用设法包装成一个接口实现对象，这样虚拟机就可以无差别地执行字节码文件而不需要管什么是方法引用了。

需要注意的是，方法引用是用来简化接口实现代码的，并且凡是能够用方法引用来简化的接口，都有这样的特征：有且只有一个待实现的方法，及函数式接口。换句话说方法引用只能和函数式接口结合使用！

## References

[The Double Colon Operator in Java 8](https://www.baeldung.com/java-8-double-colon-operator)