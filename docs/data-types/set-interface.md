# Set 接口：HashSet & TreeSet & EnumSet & LinkedHashSet
---


首先需要知道 Set 的实现都是基于 Map 接口的实现类，因此在学习完 Map 接口后再来看 Set 就很明了。

## HashSet

一些核心源码如下：

```Java
public class HashSet<E>
    extends AbstractSet<E>
    implements Set<E>, Cloneable, java.io.Serializable
{
    private transient HashMap<E,Object> map;

    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * default initial capacity (16) and load factor (0.75).
     */
    public HashSet() {
        map = new HashMap<>();
    }

    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }

    public boolean remove(Object o) {
        return map.remove(o)==PRESENT;
    }

    // ... ...

}
```

可以明显的看到 HashSet 内部就是一个 HashMap，值为 HashMap 的 Key，而 Value 则是一个 final static 的 Object 常量。HashSet 有以下一些特点：

- HashSet 中不能有相同的元素，可以有一个 Null 元素，存入的元素是无序的。
- HashSet 保证唯一性的原理是 hashCode() 和 equals() 方法
- 添加、删除操作时间复杂度都是O(1)
- 非线程安全


## LinkedHashSet

部分核心源码如下，从其调用的 HashSet 中的 super() 构造方法可以看出底层使用了 LinkedHashMap 结构。由此可知 LinkedHashSet 通过继承 HashSet，底层使用 LinkedHashMap，以很简单明了的方式来实现了其自身的所有功能。

```Java
public class LinkedHashSet<E>
    extends HashSet<E>
    implements Set<E>, Cloneable, java.io.Serializable {

    public LinkedHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor, true);
    }

    public LinkedHashSet(int initialCapacity) {
        super(initialCapacity, .75f, true);
    }

    public LinkedHashSet() {
        super(16, .75f, true);
    }

}

// 上面调用的 HashSet 中的 super() 构造方法
HashSet(int initialCapacity, float loadFactor, boolean dummy) {
    map = new LinkedHashMap<>(initialCapacity, loadFactor);
}
```

LinkedHashSet 有以下一些特点：

- 没有重复元素，可以有一个 null 元素
- 底层会维护一个双向链表，从而可以实现维持 insertion-order，并且不会因为某个元素被重复插入而改变
- 链表保证了元素的有序即存储和取出一致，哈希表保证了元素的唯一性
- 非线程安全
- 添加、删除操作时间复杂度都是O(1)

## TreeSet

从下面的部分源码可以看到 TreeSet 是基于 TreeMap（实现了 NavigableMap 接口） 实现的，默认是自然排序，也可以指定排序规则。它的特点如下：

- TreeSet 中不能有相同元素，不可以有 Null 元素，根据元素的自然顺序进行排序。
- TreeSet 底层通过红黑树(一种自平衡二叉查找树)结构实现排序
- 添加、删除操作时间复杂度都是O(log(n))
- 非线程安全


```Java
public class TreeSet<E> extends AbstractSet<E>
    implements NavigableSet<E>, Cloneable, java.io.Serializable
{
    private transient NavigableMap<E,Object> m;
    // Dummy value to associate with an Object in the backing Map
    private static final Object PRESENT = new Object();

    TreeSet(NavigableMap<E,Object> m) {
        this.m = m;
    }

    /**
     * Constructs a new, empty tree set, sorted according to the
     * natural ordering of its elements.  All elements inserted into
     * the set must implement the {@link Comparable} interface.
     */
    public TreeSet() {
        this(new TreeMap<E,Object>());
    }

    /**
     * Constructs a new, empty tree set, sorted according to the specified
     * comparator. 
     *
     * @param comparator the comparator that will be used to order this set.
     *        If {@code null}, the {@linkplain Comparable natural
     *        ordering} of the elements will be used.
     */
    public TreeSet(Comparator<? super E> comparator) {
        this(new TreeMap<>(comparator));
    }
}
```

## 线程安全

值得注意的是 Set 接口的这几个实现类都是非线程安全的，并且 JDK 并没有提供其对应的线程安全版本的实现，因此在需要同步的场景中需要调用 Collections.synchronized 方法实现，源码的注释上都有说明。

```Java
Set s = Collections.synchronizedSet(new HashSet(...));
Set s = Collections.synchronizedSet(new LinkedHashSet(...));
SortedSet s = Collections.synchronizedSortedSet(new TreeSet(...));
```

## 红黑树

红黑树是一种自平衡排序二叉树，树中每个节点的值，都大于或等于在它的左子树中的所有节点的值，并且小于或等于在它的右子树中的所有节点的值，这确保红黑树运行时可以快速地在树中查找和定位的所需节点。JDK 提供的集合类 TreeMap 本身就是一个红黑树的实现。我们将在 TreeMap 章节深入讨论一下红黑树。


