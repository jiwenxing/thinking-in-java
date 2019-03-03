# TreeMap 和 LinkedHashMap
---

本篇主要介绍一下平时不常用的两个 Map 实现：TreeMap 和 LinkedHashMap。

### TreeMap

TreeMap 是一个有序的 key-value 集合，基于**红黑树**（Red-Black tree）的 NavigableMap 接口（该接口又继承 SortedMap 接口）实现。可以保证 log(n) 的时间复杂度进行 put、remove、get、containsKey 等操作。该映射根据其键的自然顺序进行排序，或者根据创建映射时提供的 Comparator 进行排序，具体取决于使用的构造方法: 

```java
// 继承结构
public class TreeMap<K,V>
    extends AbstractMap<K,V>
    implements NavigableMap<K,V>, Cloneable, java.io.Serializable

// 使用键的自然顺序构造一个新的、空的树映射。
public TreeMap() {
    comparator = null;
}

// 构造一个新的、空的树映射，该映射根据给定比较器进行排序。
public TreeMap(Comparator<? super K> comparator) {
    this.comparator = comparator;
}

// 构造一个与给定映射具有相同映射关系的新的树映射，该映射根据其键的自然顺序进行排序。
public TreeMap(Map<? extends K, ? extends V> m) {
    comparator = null;
    putAll(m);
}

// 构造一个与指定有序映射具有相同映射关系和相同排序顺序的新的树映射。
public TreeMap(SortedMap<K, ? extends V> m) {
    comparator = m.comparator();
    try {
        buildFromSorted(m.size(), m.entrySet().iterator(), null, null);
    } catch (java.io.IOException cannotHappen) {
    } catch (ClassNotFoundException cannotHappen) {
    }
}
```

可以看到 TreeMap 继承于 AbstractMap，而 AbstractMap 实现了 Map 接口中定义的方法，减少了其子类继承的复杂度；TreeMap 实现了 NavigableMap 接口，意味着拥有了更强的元素搜索能力；如下图对于 SortedMap 来说，该类是 TreeMap 体系中的父接口，也是区别于 HashMap 体系最关键的一个接口，最大的区别就是 SortedMap 接口中定义的第一个方法 `Comparator<? super K> comparator()`。 

![](https://jverson.oss-cn-beijing.aliyuncs.com/07f47a3b60d9d4bdf3676185e520927f.jpg)

另外 TreeMap 并不是一个 synchronized 的实现，是非线程安全的，如果需要再多线程场景中使用，需要对更改 Map 结构的操作（新增、删除等，但是更新值不算）代码进行同步，或者简单的使用下面的代码将其 “wrapped” 成一个线程同步的实现。

```java
SortedMap m = Collections.synchronizedSortedMap(new TreeMap(...));
```



**红黑树**，其本质上依旧一颗*二叉查找树*，它满足了二叉查找树的特点，即左子树任意节点的值永远小于右子树任意节点的值。不过，二叉查找树还有一个致命的弱点，即左子树(右子树)可以为空，而插入的节点全部集中到了树的另一端，致使二叉查找树失去了平衡，二叉查找树搜索性能下降，从而失去了使用二分查找的意义。

为了维护树的平衡性，平衡二叉树（AVL树）出现了，它用左右子树的高度差来保持着树的平衡。而红黑树，则用的是节点的颜色来维持树的平衡。具体是怎么实现的会在数据结构的部分详细学习，这里不再展开讨论。  

### LinkedHashMap

LinkedHashMap 继承了 HashMap，所以 LinkedHashMap 其实也是散列表的结构，但是 “linked” 是它对 HashMap 功能的进一步增强，LinkedHashMap 用双向链表的结构，把所有存储在 HashMap 中的数据连接起来。通过这个双向链表可以额外的维护元素的存储顺序以及可以实现 **LRU 算法**。

```java
// 继承结构 
public class LinkedHashMap<K,V>
    extends HashMap<K,V>
    implements Map<K,V>
    
/**  创建一个空的指定容量和负载因子并以插入顺序排序的 Map 对象
     * Constructs an empty insertion-ordered <tt>LinkedHashMap</tt> instance
     * with the specified initial capacity and load factor.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
public LinkedHashMap(int initialCapacity, float loadFactor) {
    super(initialCapacity, loadFactor);
    accessOrder = false;  // 注意这个参数，是否以访问顺序排序，及是否实现 LRU，默认关闭
}

// 默认构造方法
public LinkedHashMap() {
    super();
    accessOrder = false;
}

// 将其他类型 Map 转换为 LinkedHashMap
public LinkedHashMap(Map<? extends K, ? extends V> m) {
    super();
    accessOrder = false;
    putMapEntries(m, false);
}

// 这个构造方法可以指定 ordering mode 是否为 accessOrder，如果是则变成 LRU 排序
/**
     * Constructs an empty <tt>LinkedHashMap</tt> instance with the
     * specified initial capacity, load factor and ordering mode.
     *
     * @param  initialCapacity the initial capacity
     * @param  loadFactor      the load factor
     * @param  accessOrder     the ordering mode - <tt>true</tt> for
     *         access-order, <tt>false</tt> for insertion-order
     * @throws IllegalArgumentException if the initial capacity is negative
     *         or the load factor is nonpositive
     */
public LinkedHashMap(int initialCapacity,
                     float loadFactor,
                     boolean accessOrder) {
    super(initialCapacity, loadFactor);
    this.accessOrder = accessOrder;
}
```

下面我们来测试一下普通的 LinkedHashMap 操作及 开启 LRU 规则的 LinkedHashMap

```java
public static void main(String[] args) {
    LinkedHashMap<Integer, Integer> lhMap = new LinkedHashMap<Integer, Integer>();
    for(int i=1; i<6; i++){
        lhMap.put(i*2, i);
    }
    System.out.println(lhMap); //{2=1, 4=2, 6=3, 8=4, 10=5}
    lhMap.put(3, 1);
    lhMap.get(4);
    lhMap.put(12, 6);
    // 上面对Map执行了一些put、get操作之后，发现元素只是按照put的顺序排列
    System.out.println(lhMap); //{2=1, 4=2, 6=3, 8=4, 10=5, 3=1, 12=6} 完全按照插入顺序

    LinkedHashMap<Integer, Integer> lruMap = new LinkedHashMap<Integer, Integer>(20, 0.75f, true);
    for(int i=1; i<6; i++){
        lruMap.put(i*2, i);
    }
    System.out.println(lruMap); //{2=1, 4=2, 6=3, 8=4, 10=5}
    lruMap.put(3, 1);
    lruMap.get(4);
    lruMap.put(12, 6);
    // 对开启LRU规则的LinkedHashMap，每当我get或者put一个已存在的数据，就会把这个数据放到双向链表的尾部，put一个新的数据也会放到双向链表的尾部。
    System.out.println(lruMap); //{2=1, 6=3, 8=4, 10=5, 3=1, 4=2, 12=6} 符合LRU规则
}
```

最后再进入到源码看看双向链表是怎么实现，可以看到其 Entry 定义继承了 HashMap 的 Node，并且新增了指向前后操作元素的指针 before 和 after，这样便可以实现双向链表，同时 LinkedHashMap 中也新增了双向链表的头尾节点 head 和 tail。

```java
/**
     * HashMap.Node subclass for normal LinkedHashMap entries.
     */
static class Entry<K,V> extends HashMap.Node<K,V> {
    //前后指针
    Entry<K,V> before, after;
    Entry(int hash, K key, V value, Node<K,V> next) {
        super(hash, key, value, next);
    }
}

/**
     * The head (eldest) of the doubly linked list.
     */
transient LinkedHashMap.Entry<K,V> head;//双向链表头节点（最老）

/**
     * The tail (youngest) of the doubly linked list.
     */
transient LinkedHashMap.Entry<K,V> tail;//双向列表尾节点（最新）
```
