# HashMap
---

HashMap 是基于哈希表的 Map 接口的非同步实现。此实现提供所有可选的映射操作，并允许使用 null 值和 null 键。此类不保证映射的顺序，特别是它不保证该顺序恒久不变（resize 的时候顺序可能会变）。

需要注意的是：**Hashmap 不是同步的，如果多个线程同时访问一个 HashMap，而其中至少一个线程从结构上（指添加或者删除一个或多个映射关系的任何操作）修改了，则必须保持外部同步，以防止对映射进行意外的非同步访问**。



## HashMap 的数据结构

HashMap 实际上是一个“链表散列”的数据结构，即数组和链表的结合体。从下图能看出，HashMap 底层就是一个数组结构，数组中的每一项又是一个链表（JDK8中链表长度超过8将变为红黑树结构）。当新建一个 HashMap 的时候，就会初始化数组的参数（JDK8 中在第一次使用时才会初始化 table）。

![](http://ochyazsr6.bkt.clouddn.com/fe8eae73037e2c8806b6769afe014f09.jpg)

下面来看看 HashMap 的构造方法，可以关注以下几点：

```java
/**
  * Constructs an empty <tt>HashMap</tt> with the default initial capacity
  * (16) and the default load factor (0.75).
  */
public HashMap() {
    this.loadFactor = DEFAULT_LOAD_FACTOR; // all other fields defaulted
}

public HashMap(int initialCapacity, float loadFactor) {
        if (initialCapacity < 0)
            throw new IllegalArgumentException("Illegal initial capacity: " +
                                               initialCapacity);
        if (initialCapacity > MAXIMUM_CAPACITY)
            initialCapacity = MAXIMUM_CAPACITY;
        if (loadFactor <= 0 || Float.isNaN(loadFactor))
            throw new IllegalArgumentException("Illegal load factor: " +
                                               loadFactor);
        this.loadFactor = loadFactor;
        this.threshold = tableSizeFor(initialCapacity);
}
```

1. 真正的带参构造函数有两个入参：初始容量（initialCapacity，默认 16）、负载因子（loadFactor，默认 0.75），当不设置参数创建一个 HashMap 的时候，会使用默认的值，通过这两个值可以计算出一个 threshold（resize 的临界值）。这些值将会决定 HashMap 在什么时候进行 resize（rehash），后面会详细讲。
2. 构造函数只是设置了一些属性，好像并没有创建什么实例，可是按照常理应该会去创建一个默认初始容量大小的 table 数组才对。在之前的 JDK 版本中构造函数确实会有下面这么一段代码，但是在 JDK8 的源码中，数组的初始化变成了 lazy-init，会在第一次 put 操作时根据设置的参数进行创建。

```java
// 早期版本 JDK 会在构造函数中直接初始化 table 数组
int capacity = 1;
while (capacity < initialCapacity)
    capacity <<= 1;
this.loadFactor = loadFactor;
threshold = (int)Math.min(capacity * loadFactor, MAXIMUM_CAPACITY + 1);
table = new Entry[capacity];

// JDK8 中则是初次使用时初始化
/**
  * The table, initialized on first use, and resized as
  * necessary. When allocated, length is always a power of two.
  * (We also tolerate length zero in some operations to allow
  * bootstrapping mechanics that are currently not needed.)
  */
transient Node<K,V>[] table;
```

3. table 数组中的 Node 元素（也叫 Entry），其实是一个 inner static class，其中包含了 key 和 value，也就是键值对，另外还包含了一个 next 的 Node 指针。也就是说 **Node 就是数组中的元素，每个 Node 其实就是一个 key-value 对，它持有一个指向下一个元素的引用，这就构成了链表**。

```java
/**
     * Basic hash bin node, used for most entries.  (See below for
     * TreeNode subclass, and in LinkedHashMap for its Entry subclass.)
     */
static class Node<K,V> implements Map.Entry<K,V> {
    final int hash;
    final K key;
    V value;
    Node<K,V> next;
    // ...
}
```



基本参数总结：

1. initialCapacity：初始容量。指的是 HashMap 集合初始化的时候自身的容量。可以在构造方法中指定；如果不指定的话，总容量默认值是 16 。需要注意的是初始容量必须是 2 的幂次方。
2. size：当前 HashMap 中已经存储着的键值对数量，即 `HashMap.size()` 。
3. loadFactor：加载因子。所谓的加载因子就是 HashMap (当前的容量/总容量) 到达一定值的时候，HashMap 会实施扩容。加载因子也可以通过构造方法中指定，默认的值是 0.75 。举个例子，假设有一个 HashMap 的初始容量为 16 ，那么扩容的阀值就是 0.75 * 16 = 12 。也就是说，在你打算存入第 13 个值的时候，HashMap 会先执行扩容。
4. threshold：扩容阀值。即 扩容阀值 = HashMap 总容量 * 加载因子。当前 HashMap 的容量大于或等于扩容阀值的时候就会去执行扩容。扩容的容量为当前 HashMap 总容量的两倍。比如，当前 HashMap 的总容量为 16 ，那么扩容之后为 32 。
5. table：Entry 数组。我们都知道 HashMap 内部存储 key/value 是通过 Entry 这个介质来实现的。而 table 就是 Entry 数组。
6. 在 Java 1.7 中，HashMap 的实现方法是数组 + 链表的形式。上面的 table 就是数组，而数组中的每个元素，都是链表的第一个结点。

## HashMap 的核心方法解读

当我们往 HashMap 中 put 元素的时候，先根据 key 的 hashCode 重新计算 hash 值，根据 hash 值得到这个元素在数组中的位置（即下标），如果数组该位置上已经存放有其他元素了，那么在这个位置上的元素将以链表的形式存放（如果这个位置上已经是树结构存储了，则添加到树结构中），新加入的放在链头，最先加入的放在链尾。如果数组该位置上没有元素，就直接将该元素放到此数组中的该位置上。同理从 HashMap 中 get 元素时，首先计算 key 的 hashCode，找到数组中对应位置的某一元素，然后通过 key 的 equals 方法在对应位置的链表中找到需要的元素。

```java
public V put(K key, V value) {
    return putVal(hash(key), key, value, false, true);
}

/**
     * Implements Map.put and related methods
     *
     * @param hash hash for key
     * @param key the key
     * @param value the value to put
     * @param onlyIfAbsent if true, don't change existing value
     * @param evict if false, the table is in creation mode.
     * @return previous value, or null if none
     */
final V putVal(int hash, K key, V value, boolean onlyIfAbsent,
               boolean evict) {
    Node<K,V>[] tab; Node<K,V> p; int n, i;
    if ((tab = table) == null || (n = tab.length) == 0)
        n = (tab = resize()).length;  //如果table数组还没初始化，则说明第一次调用，此处进行初始化
    if ((p = tab[i = (n - 1) & hash]) == null)
        tab[i] = newNode(hash, key, value, null);
    else {
        Node<K,V> e; K k;
        if (p.hash == hash &&
            ((k = p.key) == key || (key != null && key.equals(k))))
            e = p;
        else if (p instanceof TreeNode)
            e = ((TreeNode<K,V>)p).putTreeVal(this, tab, hash, key, value);
        else {
            for (int binCount = 0; ; ++binCount) {
                if ((e = p.next) == null) {
                    p.next = newNode(hash, key, value, null);
                    if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st
                        treeifyBin(tab, hash);
                    break;
                }
                if (e.hash == hash &&
                    ((k = e.key) == key || (key != null && key.equals(k))))
                    break;
                p = e;
            }
        }
        if (e != null) { // existing mapping for key
            V oldValue = e.value;
            if (!onlyIfAbsent || oldValue == null)
                e.value = value;
            afterNodeAccess(e);
            return oldValue;
        }
    }
    ++modCount;
    if (++size > threshold)
        resize();
    afterNodeInsertion(evict);
    return null;
}
```



**简单地说，HashMap 在底层将 key-value 当成一个整体进行处理，这个整体就是一个 Entry 对象。HashMap 底层采用一个 Entry[] 数组来保存所有的 key-value 对，当需要存储一个 Entry 对象时，会根据 hash 算法来决定其在数组中的存储位置，在根据 equals 方法决定其在该数组位置上的链表中的存储位置；当需要取出一个Entry 时，也会根据 hash 算法找到其在数组中的存储位置，再根据 equals 方法从该位置上的链表中取出该Entry。**



## HashMap 的 resize（rehash）

当 HashMap 中的元素越来越多的时候，hash 冲突的几率也就越来越高，因为数组的长度是固定的。所以为了提高查询的效率，就要对 HashMap 的数组进行扩容，数组扩容这个操作也会出现在 ArrayList 中，这是一个常用的操作，而在 HashMap 数组扩容之后，最消耗性能的点就出现了：原数组中的数据必须重新计算其在新数组中的位置，并放进去，这就是 resize。

那么 HashMap 什么时候进行 resize 呢？当 HashMap 中的元素个数超过【数组大小 `*loadFactor`】时，就会进行数组扩容，loadFactor的默认值为 0.75，这是一个折中的取值。也就是说，默认情况下，数组大小为 16，那么当 HashMap 中元素个数超过 `16*0.75=12` 的时候，就把数组的大小扩展为 `2*16=32`，即扩大一倍，然后重新计算每个元素在数组中的位置，而这是一个非常消耗性能的操作，所以如果我们已经预知 HashMap 中元素的个数，那么预设元素的个数能够有效的提高 HashMap 的性能。

## JDK8 使用红黑树的改进

JDK8 中对 HashMap 的源码进行了优化，在 JDK7 中，HashMap 处理“碰撞”的时候，都是采用链表来存储，当碰撞的结点很多时，查询时间是O(n)。 在 JDK8 中，HashMap 处理“碰撞”增加了红黑树这种数据结构，当碰撞结点较少时，采用链表存储，当满足一定条件时：**链表长度大于8个并且数组长度不低于64**，采用红黑树存储，此时查询时间是 O(logn) 。

注意 JDK8 链表转红黑树结构需要同同时满足以下两个条件才会发生：

- 链表长度（hash 冲突）超过 8 , putVal 方法中下面几行代码可以看到

```java
if (binCount >= TREEIFY_THRESHOLD - 1) // -1 for 1st，其中 TREEIFY_THRESHOLD=8
    treeifyBin(tab, hash);
break;
```



- 整个 table 数组的长度超过 64，下方 `treeifyBin` 方法可以看到

```java
/**
     * Replaces all linked nodes in bin at index for given hash unless
     * table is too small, in which case resizes instead.
     */
final void treeifyBin(Node<K,V>[] tab, int hash) {
    int n, index; Node<K,V> e;
    if (tab == null || (n = tab.length) < MIN_TREEIFY_CAPACITY) //注意这里
        resize();
    else if ((e = tab[index = (n - 1) & hash]) != null) {
        TreeNode<K,V> hd = null, tl = null;
        do {
            TreeNode<K,V> p = replacementTreeNode(e, null);
            if (tl == null)
                hd = p;
            else {
                p.prev = tl;
                tl.next = p;
            }
            tl = p;
        } while ((e = e.next) != null);
        if ((tab[index] = hd) != null)
            hd.treeify(tab);
    }
}
```



## Fail-Fast 机制

我们知道 java.util.HashMap 不是线程安全的，因此如果在使用迭代器的过程中有其他线程修改了 map，那么将抛出 ConcurrentModificationException，这就是所谓 fail-fast 策略。

这一策略在源码中的实现是通过 modCount 域，modCount 顾名思义就是修改次数，对 HashMap 内容（ ArrayList 同理）的修改都将增加这个值（源码中很多操作都有 modCount++ 这句），那么在迭代器初始化过程中会将这个值赋给迭代器的 expectedModCount。在迭代过程中，判断 modCount 跟 expectedModCount 是否相等，如果不相等就表示已经有其他线程修改了 Map。

```java
/**
     * The number of times this HashMap has been structurally modified
     * Structural modifications are those that change the number of mappings in
     * the HashMap or otherwise modify its internal structure (e.g.,
     * rehash).  This field is used to make iterators on Collection-views of
     * the HashMap fail-fast.  (See ConcurrentModificationException).
     */
transient int modCount;
```

fail-fast 机制，是一种错误检测机制。它只能被用来检测错误，因为 JDK 并不保证 fail-fast 机制一定会发生。若在多线程环境下使用 fail-fast 机制的集合，建议使用 java.util.concurrent 包下的类取代 java.util 包下的类。

### 一些常见问题

- 能否让HashMap实现线程安全，如何做？

> ```java
> Collections.synchronizeMap(hashMap);
> ```

- HashMap 线程不安全具体会体现在那些场景？

> 1. 多个线程同时使用 put 方法添加元素，且正好两个 put 的 key 发生了碰撞(hash值一样)，那么根据 HashMap的实现，这两个 key 会添加到数组的同一个位置，这样最终就会发生其中一个线程的 put 的数据被覆盖。
> 2. 多个线程同时检测到元素个数超过 threshold 进行 resize，都在重新计算元素位置以及复制数据，但是最终只有一个线程扩容后的数组会赋给 table，也就是说其他线程 put 的数据会丢失。

- 为什么 String, Interger 这样的 wrapper 类适合作为键？

> 原因类似，因为 String 是最常用的，以 String 为例来解释。String 是不可变的，也是final的，内部重写了equals() 和 hashCode() 方法。
>
> 1. 不可变性是必要的，如果键值在放入时和获取时返回不同的 hashcode 的话，那么就不能从 HashMap 中找到你想要的对象。不可变性还有其他的优点如线程安全。如果你可以仅仅通过将某个 field 声明成 final 就能保证 hashCode 是不变的，那么请这么做吧。
> 2. 因为获取对象的时候要用到 equals() 和 hashCode() 方法，那么键对象正确的重写这两个方法是非常重要的。如果两个不相等的对象返回不同的 hashcode 的话，那么碰撞的几率就会小些，这样就能提高 HashMap 的性能。

- 可以使用自定义的对象作为键吗？

> 可以使用任何对象作为键，只要它遵守了 equals() 和 hashCode() 方法的定义规则，并且当 key 对象插入到 Map 中之后将不会再改变了。

- 可以使用 CocurrentHashMap 来代替 Hashtable 吗？

> **绝大部分情况**是可以的而且也应该这么做，ConcurrentHashMap 同步性能更好，因为它仅仅根据同步级别对map 的一部分进行上锁。但 ConcurrentHashMap 并不能提供强一致性，它的 get，clear，iterator 都是弱一致性的，例如 put 操作将一个元素加入到底层数据结构后，get 可能在非常短的时间内还获取不到这个元素。相较而言 HashTable 提供更强的线程安全性。因此在一致性要求非常高的场景下依然有必要使用 HashTable。
>
> ConcurrentHashMap 的弱一致性主要是为了提升效率，是一致性与效率之间的一种权衡。要成为强一致性，就得到处使用锁，甚至是全局锁，这就与使用 Hashtable 和同步的 HashMap 一样了。
>
> 关于这个问题的深入探讨这可以参考[这里](https://my.oschina.net/hosee/blog/675423)



### 参考

- [ConcurrentHashMap能完全替代HashTable吗？](https://my.oschina.net/hosee/blog/675423)