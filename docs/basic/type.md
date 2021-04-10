# 泛型中的 Type 接口
---

在上一篇 [Java 泛型（Generics）的理解和使用](https://jverson.com/thinking-in-java/basic/generics.html) 中我提到了 Type 接口，这一篇中我们来深入了解一下

![](https://jverson.oss-cn-beijing.aliyuncs.com/967934a6e803828193172554ade5aca9.jpg)

## 接口定义

从接口定义的注释中我们可以看到 Type 是 Java 编程语言中【所有类型】的公共高级接口。它们包括原始类型、参数化类型(泛型)、数组类型、类型变量和基本类型。

注意区分 Type 与 Class 的区别，这里的 Class 是 Type 的一种（Class 实现了 Type 接口），而像数组、枚举等"类型"是相对于 Class 来说的。

```Java
package java.lang.reflect;

/**
 * Type is the common superinterface for all types in the Java
 * programming language. These include raw types, parameterized types,
 * array types, type variables and primitive types.
 *
 * @since 1.5
 */
public interface Type {
    /**
     * Returns a string describing this type, including information
     * about any type parameters.
     *
     * @implSpec The default implementation calls {@code toString}.
     *
     * @return a string describing this type
     * @since 1.8
     */
    default String getTypeName() {
        return toString();
    }
}

```

## Java中的所有类型

如果有人突然问你 Java 中都有哪些类型？估计很多人都会一脸懵逼，然后第一反应就是 Integer、Double 等等~ 也没错这些是数据类型。到了 JDK1.5 我们可以从另一个角度去回答这个问题。

我们知道，Type是JDK5开始引入的，其引入主要是为了泛型，没有泛型的之前，只有所谓的原始类型。此时，所有的原始类型都通过字节码文件类Class类进行抽象。Class类的一个具体对象就代表一个指定的原始类型。

泛型出现之后，也就扩充了数据类型。从只有原始类型扩充了参数化类型、类型变量类型、泛型数组类型，也就是Type的子接口。 
那为什么没有统一到Class下，而是增加一个Type呢？（Class也是种类的意思，Type是类型的意思） 
是为了程序的扩展性，最终引入了Type接口作为Class，ParameterizedType，GenericArrayType，TypeVariable和WildcardType这几种类型的总的父接口。这样实现了Type类型参数接受以上五种子类的实参或者返回值类型就是Type类型的参数。

- raw type：原始类型，对应Class 。这里的Class不仅仅指平常所指的类，还包括数组、接口、注解、枚举等结构。
- primitive types：基本类型，仍然对应 Class
- parameterized types：参数化类型，对应 ParameterizedType，带有类型参数的类型，即常说的泛型，如：List<T>、Map<Integer, String>、List<? extends Number>。
- type variables：类型变量，对应 TypeVariable<D>，如参数化类型中的E、K等类型变量，表示泛指任何类。
- array types：(泛型)数组类型，对应 GenericArrayType，比如List<T>[]，T[]这种。注意，这不是我们说的一般数组，而是表示一种【元素类型是参数化类型或者类型变量的】数组类型。

注意：WildcardType 代表通配符表达式，或泛型表达式，比如【?】【? super T】【? extends T】。虽然WildcardType是Type的一个子接口，但并不是Java类型中的一种。

通过一段测试代码来了解一下

```Java
@Test
public void test2() throws NoSuchMethodException, SecurityException {
    Method method = TempTest.class.getMethod("testType", List.class, List.class, List.class, List.class, List.class, Map.class);
    Type[] types = method.getGenericParameterTypes();//按照声明顺序返回 Type 对象的数组
    for (Type type : types) {
        ParameterizedType pType = (ParameterizedType) type;//最外层都是ParameterizedType
        Type[] types2 = pType.getActualTypeArguments();//返回表示此类型【实际类型参数】的 Type 对象的数组
        for (int i = 0; i < types2.length; i++) {
            Type type2 = types2[i];
            System.out.println(i + "  类型【" + type2 + "】\t类型接口【" + type2.getClass().getInterfaces()[0].getSimpleName() + "】");
        }
    }
}

public <T> void testType(List<String> a1, List<ArrayList<String>> a2, List<T> a3, //
                         List<? extends Number> a4, List<ArrayList<String>[]> a5, Map<String, Integer> a6) {
}

// 输出如下

0  类型【class java.lang.String】	类型接口【Serializable】
0  类型【java.util.ArrayList<java.lang.String>】	类型接口【ParameterizedType】
0  类型【T】	类型接口【TypeVariable】
0  类型【? extends java.lang.Number】	类型接口【WildcardType】
0  类型【java.util.ArrayList<java.lang.String>[]】	类型接口【GenericArrayType】
0  类型【class java.lang.String】	类型接口【Serializable】
1  类型【class java.lang.Integer】	类型接口【Serializable】
```

## ParameterizedType 泛型/参数化类型

这里重点介绍一下 Type 四个子接口中比较常用的 ParameterizedType 接口。ParameterizedType 表示参数化类型，带有类型参数的类型，**即常说的泛型**，如：List<T>、Map<Integer, String>、List<? extends Number>。接口定义了三个方法如下

```Java
public interface ParameterizedType extends Type {
    /**
     * Returns an array of {@code Type} objects representing the actual type
     * arguments to this type.
     */
    Type[] getActualTypeArguments(); // 简单来讲就是获得参数化类型中<>里的类型参数的类型。

    /**
     * Returns the {@code Type} object representing the class or interface
     * that declared this type.
     */
    Type getRawType(); // 简单来说就是：返回最外层<>前面那个类型，例如Map<K ,V>，返回的是Map类型。

    /**
     * Returns a {@code Type} object representing the type that this type
     * is a member of.  For example, if this type is {@code O<T>.I<S>},
     * return a representation of {@code O<T>}.
     *
     * <p>If this type is a top-level type, {@code null} is returned.
     */
    Type getOwnerType();
}
```

下面我们自己代码测一下加深理解

```Java
public class TempTest {

    List<String> list1;
    List list2;
    Map<String,Long> map1;
    Map map2;
    Map.Entry<Long,Short> map3;


    @Test
    public void test3(){
        Field[] fields = TempTest.class.getDeclaredFields();
        for(Field f:fields){
            //是否是ParameterizedType
            System.out.print(f.getName()+":"+(f.getGenericType() instanceof ParameterizedType));
            System.out.println();
        }
    }

    // 输出：list1:true list2:false map1:true map2:false map3:true。从打印结果看来,具有<>符号的变量是参数化类型

    @Test
    public void test4() {
        Field[] fields = TempTest.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getGenericType() instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) f.getGenericType();
                System.out.print("变量：" + pType.getTypeName() + "     ");
                Type[] types = pType.getActualTypeArguments();
                for (Type t : types) {
                    System.out.print("类型：" + t.getTypeName() + " ");
                }
            }
            System.out.println();
        }
    }

    /** 输出如下，getActualTypeArguments() 返回了一个Type数组,数组里是参数化类型的参数
    变量：java.util.List<java.lang.String>     类型：java.lang.String 
    变量：java.util.Map<java.lang.String, java.lang.Long>     类型：java.lang.String 类型：java.lang.Long 
	变量：java.util.Map$Entry<java.lang.Long, java.lang.Short>     类型：java.lang.Long 类型：java.lang.Short 
	*/


	@Test
    public void test5() {
        Field[] fields = TempTest.class.getDeclaredFields();
        for (Field f : fields) {
            if(f.getGenericType() instanceof ParameterizedType){
                ParameterizedType pType = (ParameterizedType) f.getGenericType();
                System.out.print("变量："+f.getName());
                System.out.print(" RawType："+pType.getRawType().getTypeName());
            }
            System.out.println();
        }
    }
    /**
     * 输出如下，getRawType 其实也就是变量的类型
     * 变量：list1 RawType：java.util.List
     * 变量：map1 RawType：java.util.Map
     * 变量：map3 RawType：java.util.Map$Entry
     */

    @Test
    public void test6() {
        Field[] fields = TempTest.class.getDeclaredFields();
        for (Field f : fields) {
            if (f.getGenericType() instanceof ParameterizedType) {
                ParameterizedType pType = (ParameterizedType) f.getGenericType();
                System.out.print("变量：" + f.getName());
                Type t = pType.getOwnerType();
                if (t == null) {
                    System.out.print("OwnerType: Null");
                } else {
                    System.out.print("OwnerType：" + t.getTypeName());
                }
                System.out.println();
            }
        }
    }

    /**
     * 输出如下，可知类似 Map.Entry<Long, Short> 这种 O<T>.I<S> 类型的变量,调用 getOwnerType() 会返回 O<T>，大概表示内部类的持有类
     * 变量：list1OwnerType: Null
     * 变量：map1OwnerType: Null
     * 变量：map3OwnerType：java.util.Map
     */    

}
```

## 参考

- [Type 接口【重要】](https://www.cnblogs.com/baiqiantao/p/7460580.html)
- [java.lang.reflect.Type接口详解](https://juejin.cn/post/6844904177257168910)
