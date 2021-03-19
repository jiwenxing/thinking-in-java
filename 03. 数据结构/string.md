# String 相关知识点
---

## 一些结论

- String 不可以被继承，因为是Final Class

## String 的 `==`、`equals` 以及 `hashcode`    

先来看一段代码：

```java
public static void main(String[] args) {
	String a = "hello";
	String b = "hello";
	String c = new String("hello");
	String d = new String("hello");
	System.out.println(a.hashCode() == b.hashCode()); //true， String重写了hashcode方法，只跟String的value[]有关
	System.out.println(a.hashCode() == c.hashCode()); //true 同上
	System.out.println(a.equals(b)); //true，String也重写了equals方法，只跟value[]有关
	System.out.println(a.equals(c)); //true，同上
	System.out.println(a == b); //true,因为a和b都在常量池中，
	System.out.println(a == c); //false，a在常量池中，c在堆中
	System.out.println(c == d); //false，c和d都在堆中，但是为不同的对象
	
	String hello = "Hello", lo = "lo";
	System.out.println((hello == ("Hel"+"lo"))); //true,"Hel"+"lo"在编译时进行计算，被当做常量
	System.out.println((hello == ("Hel"+lo))); //false，在运行时通过连接计算出的字符串是新创建的，因此是堆中创建的新对象
	System.out.println(hello == ("Hel"+lo).intern()); //true，通过使用字符串的intern()方法来指向字符串池中的对象
}
```


再来看看 String 重写的 hashcode 及 equals 方法

```java    
public int hashCode() {
    int h = hash;
    if (h == 0 && value.length > 0) {
        char val[] = value;

        for (int i = 0; i < value.length; i++) {
            h = 31 * h + val[i];
        }
        hash = h;
    }
    return h;
}   

public boolean equals(Object anObject) {
    if (this == anObject) {
        return true;
    }
    if (anObject instanceof String) {
        String anotherString = (String)anObject;
        int n = value.length;
        if (n == anotherString.value.length) {
            char v1[] = value;
            char v2[] = anotherString.value;
            int i = 0;
            while (n-- != 0) {
                if (v1[i] != v2[i])
                    return false;
                i++;
            }
            return true;
        }
    }
    return false;
}    
```

结论：

- String 的 hashCode 只和 value 有关
- equals 比较的也只是 value（指向同一个对象时 value 也必然相同，换句话就是`a==b ||（a.length=b.length && {a[i]=b[i]}）`）
- a==b 比较的是 ref
- hashSet 中比较是否重复的依据是`a.hasCode()=b.hasCode() && a.equals(b)`
- 因此两个不同 ref 的 String 对象在 hashSet 中会被认为是同一个元素
- **两个 String 的 hashCode 相同并不代表着 equals 比较时会相等**，**即不同的 String 可能会产生相同的 hash 值**，例如`Aa`和`BB`的 hashcode 都是2112

## 字符串常量池

JVM 为了提高性能和减少内存开销，在实例化字符串常量的时候进行了一些优化。为了减少在JVM中创建的字符串的数量，字符串类维护了一个字符串池，每当代码创建字符串常量时，JVM 会首先检查字符串常量池。如果字符串已经存在池中，就返回池中的实例引用。如果字符串不在池中，就会实例化一个字符串并放到池中。**Java 能够进行这样的优化是因为字符串是不可变的，可以不用担心数据冲突进行共享。** 

另外需要注意**通过 new 操作符创建的字符串对象不指向字符串池中的任何对象，但是可以通过使用字符串的 intern() 方法来指向其中的某一个。**


## hashcode 方法的作用

Java 的 Object 类中定义了`public native int hashCode()`方法，也就是说每一个 java 类都会继承这个方法（有的类会重写此方法），那这个方法到底有什么用，为什么这么重要？

**在 Java 中，hashCode 方法的主要作用是为了配合基于散列的集合一起正常运行，这样的散列集合包括HashSet、HashMap 以及 HashTable。**

举个例子：当向集合中插入对象时，如何判别在集合中是否已经存在该对象了？（注意：集合中不允许重复的元素存在）。

很容易想到的方法是调用 equals 方法来逐个进行比较，这个方法确实可行，但是如果集合中已经存在一万条数据或者更多的数据，如果采用 equals 方法去逐一比较，效率必然是一个问题。

JDK 中实现的方法是：当集合要添加新的对象时，先调用这个对象的 hashCode 方法，得到对应的 hashcode 值，实际上在 HashMap 的具体实现中会用一个 table 保存已经存进去的对象的 hashcode 值（hashcode 值一般都会被缓存），如果table 中没有该 hashcode 值，它就可以直接存进去，不用再进行任何比较了；如果存在该 hashcode 值， 就调用它的equals 方法与新元素进行比较，相同的话就不存了，不相同就散列其它的地址。这样一来实际调用 equals 方法的次数就大大降低了。

总结一句话就是：**可以根据 hashcode 值判断两个对象是否不等，这样效率更高！**


## 可以直接根据 hashcode 值判断两个对象是否相等吗？

不可以！前面也演示了不同的对象可能会生成相同的 hashcode 值。虽然不能根据hashcode值判断两个对象是否相等，但是可以直接根据 hashcode 值判断两个对象不等，如果两个对象的 hashcode 值不等，则必定是两个不同的对象。 

* 两个对象调用 equals 方法得到的结果为 true，则两个对象的 hashcode 值必定相等；
* 如果 equals 方法得到的结果为 false，则两个对象的 hashcode 值不一定不同
* 如果两个对象的 hashcode 值不等，则 equals 方法得到的结果必定为 false；
* 如果两个对象的 hashcode 值相等，则 equals 方法得到的结果未知。


## 在重写 equals 方法的同时，必须重写 hashCode 方法。为什么这么说？

重写 equals 方法后必须重写 hashCode 是为了让 Java 中所有使用到 Hash 算法的数据结构能够正常运行。

举个例子：假如 a 和 b 是两个对象，你重写了 equals() 方法，a.equals(b)结果为true。

然后创建一个 HashMap，执行 map.put(a,c); map 中插入了一条数据，键是 a 值是 c，调用 map.get(a) 可以返回对象 c，但是调用 map.get(b) 却不能返回对象 c， 但是 a 和 b 两个对象是相等的，相等的对象却得不到相同的结果，就不符合逻辑了。因为 HashMap 是根据键对象的 HashCode 来进行快速查找的，所以你必须保证 a 和 b 这两个相同对象的 HashCode 也相同，因此你需要重写 hashCode() 方法。另外，如果你要用到 HashSet，在这个例子中 a 和 b 可以同时插入到 HashSet 中，然而这两个对象在逻辑上有时相等的，这不符合 HashSet 的定义。

另外这也是hashcode方法的要求，在Object的hashcode方法注释中明确做了如下说明：
> If two objects are equal according to the {@code equals(Object)}
     *     method, then calling the {@code hashCode} method on each of
     *     the two objects must produce the same integer result.


## hashcode 是对象的内存地址吗

首先String就不是，因为它重写了hashcode方法。这个问题问的应该是object中的hashcode的native方法。因为不同JVM的实现不一样，可能有JVM这样实现，但大多时候并不是这样，只能说可能存储地址有一定关联。

## String.itern()的基本原理和作用

String.intern()是一个Native方法，底层调用C++的 StringTable::intern 方法，源码注释：当调用 intern 方法时，如果常量池中已经该字符串，则返回池中的字符串；否则将此字符串添加到常量池中，并返回字符串的引用。

所以明面上，它有两大好处，一是重复的字符串，会用同一个引用代替；二是字符串比较，不再需要逐个字符的equals()比较，而用==对比引用是否相同即可。


