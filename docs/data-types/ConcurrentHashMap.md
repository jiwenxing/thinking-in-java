# ConcurrentHashMap
---

ConcurrentHashMap 底层存储数据的结构与 HashMap 是一样的，都是数组 + 链表（或红黑树）的结构。在日常开发中，最常用到的键值对存储结构的是 HashMap，但是 HashMap 是非线程安全的，在高并发的场景下进行 put 操作的时候有可能进入死循环从而使服务器的 cpu 使用率达到 100%。

在 jdk1.5 以前，可以通过 HashTable 替代 HashMap 来保证线程安全， HashTable 是通过在每个需要同步的方法中都添加了 synchronized 关键字实现的，这样在高并发的情景下效率比较低。

jdk1.5 后推出了 ConcurrentHashMap 来替代效率低下的 HashTable，其使用了分段锁的技术，在整个数组中被分为多个 segment，在多个线程操作时，它不用做额外的同步的情况下默认同时允许 16 个线程读和写这个 Map 容器。因为不像 `HashTable` 和 `SynchronizedMap` 需要锁整个 Map，要操作哪一段才上锁那段数据。每次 get、put、remove 操作时只锁住目标元素所在的 segment，其它 segment 依然是可以并发操作的。但是如果出现机端的情况，所有的数据都集中在一个 segment 中的话，依然相当于锁住了全表，不过总体来说相较于 HashTable 效率还是有了很大的提升。

在 JDK1.8 中 ConcurrentHashMap 摒弃了 segment 的思想，转而使用 cas+synchronized 组合的方式来实现并发下的线程安全的，和分段锁相比效率又有了比较大的提升。要深刻理解 JDK1.8 中 ConcurrentHashMap 的实现原理，需要先熟悉 Java 内存模型，volatile 关键字和 CAS 算法等基础知识。

## ConcurrentHashMap 成员变量

- **sizeCtl** 主要是用来控制数组的初始化和扩容的，默认值为 0，可以概括一下 4 种状态：
  - sizeCtr=0：默认值；
  - sizeCtr=-1：表示 Map 正在初始化中；
  - sizeCtr=-N：表示正在有 N-1 个线程进行扩容操作；
  - sizeCtr>0: 未初始化则表示初始化 Map 的大小，已初始化则表示下次进行扩容操作的阈值；

- **table** 用于存储链表或红黑数的数组，初始值为 null，在第一次进行 put 操作的时候进行初始化，默认值为 16；

- **nextTable** 在扩容时新生成的数组，其大小为当前 table 的 2 倍，用于存放 table 转移过来的值；

- **Node** 该类存储数据的核心，以 key-value 形式来存储；

- **ForwardingNode** 这是一个特殊 Node 节点，仅在进行扩容时用作占位符，表示当前位置已被移动或者为 null，该 node 节点的 hash 值为 - 1；

```Java
/**
 * Table initialization and resizing control.  When negative, the
 * table is being initialized or resized: -1 for initialization,
 * else -(1 + the number of active resizing threads).  Otherwise,
 * when table is null, holds the initial table size to use upon
 * creation, or 0 for default. After initialization, holds the
 * next element count value upon which to resize the table.
 */
private transient volatile int sizeCtl;

/**
 * The array of bins. Lazily initialized upon first insertion.
 * Size is always a power of two. Accessed directly by iterators.
 */
transient volatile Node<K,V>[] table;

/**
 * The next table to use; non-null only while resizing.
 */
private transient volatile Node<K,V>[] nextTable;

/**
 * Key-value entry.  This class is never exported out as a
 * user-mutable Map.Entry (i.e., one supporting setValue; see
 * MapEntry below), but can be used for read-only traversals used
 * in bulk tasks.  Subclasses of Node with a negative hash field
 * are special, and contain null keys and values (but are never
 * exported).  Otherwise, keys and vals are never null.
 */
static class Node<K,V> implements Map.Entry<K,V> {//......}
```

## put 操作过程

源码如下：

```Java
public V put(K key, V value) {return putVal(key, value, false);
}

/** Implementation for put and putIfAbsent */
final V putVal(K key, V value, boolean onlyIfAbsent) {
	//  注意：key 和 value 都不能为 null
    if (key == null || value == null) throw new NullPointerException();
    // 计算 key 的 hash 值
    int hash = spread(key.hashCode());
    // 用于记录数组位置上存放的 node 的节点数量
    // 在 put 完成后会对这个参数判断是否需要转换成红黑树或链表
    int binCount = 0;
    for (Node<K,V>[] tab = table;;) {Node<K,V> f; int n, i, fh;
        if (tab == null || (n = tab.length) == 0) 
        // 第一次 put 操作，对数组进行初始化，实现懒加载
            tab = initTable();
        else if ((f = tabAt(tab, i = (n - 1) & hash)) == null) { 
        // 获取插入元素所在的数组的下标的位置，该位置为空的话就直接使用 cas 放进去
            if (casTabAt(tab, i, null,
                         new Node<K,V>(hash, key, value, null)))
                break;                   // no lock when adding to empty bin
        }
        else if ((fh = f.hash) == MOVED)
        	// 正在进行扩容操作，让当前线程也帮助该位置上的扩容，并发扩容提高扩容的速度
            tab = helpTransfer(tab, f);
        else {
            V oldVal = null;
            synchronized (f) {if (tabAt(tab, i) == f) {if (fh>= 0) {
                        binCount = 1;
                        for (Node<K,V> e = f;; ++binCount) {
                            K ek;
                            if (e.hash == hash &&
                                ((ek = e.key) == key ||
                                 (ek != null && key.equals(ek)))) {
                                oldVal = e.val;
                                if (!onlyIfAbsent)
                                    e.val = value;
                                break;
                            }
                            Node<K,V> pred = e;
                            if ((e = e.next) == null) {pred.next = new Node<K,V>(hash, key,
                                                          value, null);
                                break;
                            }
                        }
                    }
                    else if (f instanceof TreeBin) {Node<K,V> p;
                        binCount = 2;
                        if ((p = ((TreeBin<K,V>)f).putTreeVal(hash, key,
                                                       value)) != null) {
                            oldVal = p.val;
                            if (!onlyIfAbsent)
                                p.val = value;
                        }
                    }
                }
            }
            if (binCount != 0) {if (binCount>= TREEIFY_THRESHOLD)
                    treeifyBin(tab, i);
                if (oldVal != null)
                    return oldVal;
                break;
            }
        }
    }
    addCount(1L, binCount);
    return null;
}
```

- 判断传进来的 key 和 value 是否为空，在 ConcurrentHashMap 中 key 和 value 都不允许为空，而 HashMap 是可以的，这一点值得注意一下；
- 对 key 进行重 hash 计算，获得 hash 值
- 如果当前的数组为空，说明这是第一插入数据，则会对 table 进行初始化；
- 插入数据，这里分为 3 中情况：
  - 插入位置为空，直接将数据放入 table 的第一个位置中；
  - 插入位置不为空，并且是一个 ForwardingNode 节点，说明该位置上的链表或红黑树正在进行扩容，然后让当前线程加进去并发扩容，提高效率；
  - 插入位置不为空，也不是 ForwardingNode 节点，若为链表则从第一节点开始组个往下遍历，如果有 key 的 hashCode 相等并且值也相等，那么就将该节点的数据替换掉，否则将数据加入到链表末段；若为红黑树，则按红黑树的规则放进相应的位置；
- 数据插入成功后，判断当前位置上的节点的数量，如果节点数据大于转换红黑树阈值（默认为 8），则将链表转换成红黑树，提高 get 操作的速度；
- 数据量 + 1，并判断当前 table 是否需要扩容；

## 为什么要求 table 的长度一定是 2 的 n 次方

我们将一个值放进 Map 的时候需要通过其 hash 值对数组长度取模得到其位置，即计算 `hash%length`，但是计算机中直接求余效率不如位移运算，因此源码中做了优化 `hash&(length-1)`，而 `hash%length==hash&(length-1)` 的前提是 length 是 2 的 n 次方；

我们来看看 ConcurrentHashMap 中计算 hash 的源码，从代码和注释可以看到 jdk1.8 计算 hash 的方法是先获取到 key 的 hashCode，然后对 hashCode 进行高 16 位和低 16 位异或运算，然后再与 0x7fffffff 进行与运算。高低位异或运算可以保证 hashCode 的每一位都可以参与运算，从而使运算的结果更加均匀的分布在不同的区域，在计算 table 位置时可以减少冲突，提高效率，我们知道 Map 在 put 操作时大部分性能都耗费在解决 hash 冲突上面。得出运算结果后再和 0x7fffffff 与运算，其目的是保证每次运算结果都是一个正数。然后 put 的时候通过执行 `(f = tabAt(tab, i = (n - 1) & hash)) == null` 来判断对应位置元素是否已存在。

```Java
int hash = spread(key.hashCode());

/**
 * Spreads (XORs) higher bits of hash to lower and also forces top
 * bit to 0. Because the table uses power-of-two masking, sets of
 * hashes that vary only in bits above the current mask will
 * always collide. (Among known examples are sets of Float keys
 * holding consecutive whole numbers in small tables.)  So we
 * apply a transform that spreads the impact of higher bits
 * downward. There is a tradeoff between speed, utility, and
 * quality of bit-spreading. Because many common sets of hashes
 * are already reasonably distributed (so don't benefit from
 * spreading), and because we use trees to handle large sets of
 * collisions in bins, we just XOR some shifted bits in the
 * cheapest possible way to reduce systematic lossage, as well as
 * to incorporate impact of the highest bits that would otherwise
 * never be used in index calculations because of table bounds.
 */
static final int spread(int h) {return (h ^ (h>>> 16)) & HASH_BITS;
}
```

## 为什么 JDK7 中 HashMap 在并发时可能会出现死循环

这个现象貌似还不少人在生产环境遇到过，现象是线上正常运行的应用莫名其妙的会突然出现 CPU100%，重启后问题消失，隔一段时间又会反复出现。可以通过 jps & jstack 查看线程栈情况发现问题。这主要是发生在多线程下 HashMap 扩容的时候，需要把原 HashMap 里的数据一个一个的复制到新的 HashMap 里，在这个过程中 hash 冲突的链表在复制到新结构中时其顺序会反转，并发时就可能会出现环形链表。具体原理参考这篇文章 [JAVA HASHMAP的死循环](https://coolshell.cn/articles/9606.html)

但是注意 JDK8 已经解决了这个问题，它使用 head 和 tail 两个引用来保证链表的顺序和之前一样来避免此问题。但尽管不会死循环的 BUG，但 HashMap 还是非线程安全类，仍然会产生数据丢失等问题。因此在多线程场景下还是要使用线程安全的类 ConcurrentHashMap。



