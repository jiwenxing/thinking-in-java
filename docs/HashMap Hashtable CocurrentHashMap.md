# HashMap Hashtable CocurrentHashMap
---

首先来看看 JDK 中 Map 接口的继承树，它常用的实现类有 `HashMap`、`HashTable` 及 `ConcurrentHashMap`等。这里重点介绍一下这三个常用实现之间的区别，同时也会梳理一下其它实现的底层数据结构及适用的场景，例如 `TreeMap`、`LinkedHashMap`等。

![](http://pgdgu8c3d.bkt.clouddn.com/7cbf202b26e96c7a6ea2906044cc2b60.jpg)



### HashMap & HashTable

上一篇详细讲解了 HashMap 的实现原理，先来看看两者的区别：

1. 相对于 HashMap，我们都知道 HashTable 是线程安全的，它的方法是同步了的，可以直接用在多线程环境中，这是最大的区别，但在只有一个线程访问的情况下，HashMap 效率要高于 HashTable
2. Hashtable 和 HashMap 都实现了 Map 接口，但是 Hashtable 的实现是基于 Dictionary 抽象类，而 HashMap 则是继承自 AbstractMap，这就表明两者在实现上将会有不少区别。
3. Hashtable 中 key 和 value 都不允许为 null，而 HashMap 中 key 和 value 都允许为 null（key 只能有一个为 null，而value 则可以有多个为 null）。因此在 HashMap 中不能由 get() 方法来判断是否存在某个键，而应该用 containsKey()方法来判断。
4. HashMap 的迭代器 (Iterator) 是 fail-fast 迭代器，而 Hashtable 的 enumerator 迭代器不是。
5. HashTable 是 JDK1.0 就引入的，比较古老，由于其效率问题，JDK1.5 引入了同样线程安全但性能更高的 ConcurrentHashMap，因此 HashTable 已基本被遗弃，更多的是出现在面试题中。这一点从 HashTable 的官方注释上就能看出来。

> Java Collections Framework.  Unlike the new collection implementations, {@code Hashtable} is synchronized.  If a thread-safe implementation is not needed, it is recommended to use{@link HashMap} in place of {@code Hashtable}.  If a thread-safe highly-concurrent implementation is desired, then it is recommended to use {@link java.util.concurrent.ConcurrentHashMap} in place of {@code Hashtable}.

**注意**：上一篇讲过 ConcurrentHashMap 并不能提供强一致性，它的 get，clear，iterator 都是弱一致性的，例如 put 操作将一个元素加入到底层数据结构后，get 可能在非常短的时间内还获取不到这个元素。ConcurrentHashMap 的弱一致性主要是为了提升效率，是一致性与效率之间的一种权衡。要成为强一致性，就得到处使用锁，甚至是全局锁，这就与使用 Hashtable 和同步的 HashMap 一样了。

### ConcurrentHashMap vs Hashtable vs Synchronized Map

虽然三个集合类在多线程并发应用中都是线程安全的，但是他们有一个重大的差别，就是他们各自实现线程安全的方式不同。`Hashtable`是 jdk1 的一个遗留下来的类，它把所有方法都加上`synchronized`关键字来实现线程安全。所有的方法都同步这样造成多个线程访问效率特别低。`Synchronized Map`与`HashTable`差别不大，也是在并发中作类似的操作，可以通过使用`Collections.synchronizedMap()`来包装`Map`作为同步容器使用。

ConcurrentHashMap 在 JDK1.8 之前都是使用锁分段的原理，表现在多个线程操作上，它不用做额外的同步的情况下默认同时允许16个线程读和写这个 Map 容器。因为不像`HashTable`和`Synchronized Map`，`ConcurrentHashMap`不需要锁整个Map，相反它划分了多个段(segments)，要操作哪一段才上锁那段数据。

在 JDK1.8 中则主要是采用了 CAS（Compare And Swap） 算法实现线程安全的。具体的实现原理前面已经介绍过，不再赘述。




 ### 参考

- [Difference between ConcurrentHashMap, Hashtable and Synchronized Map in Java](https://javarevisited.blogspot.com/2011/04/difference-between-concurrenthashmap.html)
- [JDK1.8逐字逐句带你理解ConcurrentHashMap](https://blog.csdn.net/u012403290/article/details/67636469)

 

 

 