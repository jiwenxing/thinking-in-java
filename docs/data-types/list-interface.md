# List 接口： ArrayList & LinkedList & Vector & Stack
---

List 的主要实现有 ArrayList、LinkedList、Vector、Stack 等，各有特点及其使用场景，下面进行简单介绍。


## ArrayList vs LinkedList

ArrayList 不保证线程安全，底层是 Object 数组，因此在指定位置插入删除元素的复杂度是 O(n)，但和普通数组一样具有随机访问（通过下标）的特性。另外其默认初始容量是 10。

LinkedList 也不保证线程安全，底层则是双向链表的结构，新增删除元素很快，但是不支持随机访问。

ArrayList 和 LinkedList 经常会放在一起比较，它们也有各自的特点和适用场景，下面我简单看一下 Collections 中的 binarySearch 方法实现

```Java
public static <T>
int binarySearch(List<? extends Comparable<? super T>> list, T key) {if (list instanceof RandomAccess || list.size()<BINARYSEARCH_THRESHOLD)
        return Collections.indexedBinarySearch(list, key);
    else
        return Collections.iteratorBinarySearch(list, key);
}
```

可以看到里面通过判断 list 是否为 RandomAccess 的实例分别调用了不同的二分查找方法，这两个方法有什么区别呢？

```Java
private static <T>
int indexedBinarySearch(List<? extends Comparable<? super T>> list, T key) {
    int low = 0;
    int high = list.size()-1;

    while (low <= high) {int mid = (low + high) >>> 1;
        Comparable<? super T> midVal = list.get(mid);
        int cmp = midVal.compareTo(key);

        if (cmp < 0)
            low = mid + 1;
        else if (cmp> 0)
            high = mid - 1;
        else
            return mid; // key found
    }
    return -(low + 1);  // key not found
}

private static <T>
int iteratorBinarySearch(List<? extends Comparable<? super T>> list, T key)
{
    int low = 0;
    int high = list.size()-1;
    ListIterator<? extends Comparable<? super T>> i = list.listIterator();

    while (low <= high) {int mid = (low + high) >>> 1;
        Comparable<? super T> midVal = get(i, mid);
        int cmp = midVal.compareTo(key);

        if (cmp < 0)
            low = mid + 1;
        else if (cmp> 0)
            high = mid - 1;
        else
            return mid; // key found
    }
    return -(low + 1);  // key not found
}

/**
 * Gets the ith element from the given list by repositioning the specified
 * list listIterator.
 */
private static <T> T get(ListIterator<? extends T> i, int index) {
    T obj = null;
    int pos = i.nextIndex();
    if (pos <= index) {do {obj = i.next();
        } while (pos++ < index);
    } else {do {obj = i.previous();
        } while (--pos> index);
    }
    return obj;
}
```

可以看到都是二分查找，唯一不同的就是在获取 mid 元素的方式上不同，indexedBinarySearch 方法中直接通过 list.get(mid) 获取，而 iteratorBinarySearch 中调用了 get(i, mid) 方法，可以看到该方法通过 listIterator 来查找元素。我们查看源码可以发现 ArrayList 实现了 RandomAccess 接口， 而 LinkedList 没有实现。这就好理解了，因为前者底层的数组结构天然支持随机读取。另外注意 RandomAccess 接口只是标识，并不是说 ArrayList 实现 RandomAccess 接口才具有快速随机访问功能的！

还有一点要注意的是我们平时应该对 ArrayList（实现了 RandomAccess 接口的 list） 用 for 循环；而未实现 RandomAccess 接口的 list，优先选择 iterator 遍历，也就是我们平时用的 foreach 方式。

## Vector vs ArrayList vs Stack

Vector 其实就是线程安全版的 ArrayList，使用上主要看是否对线程安全有要求，具体的区别有以下几点：

 线程安全：两者底层结构一样都是数组，Vector 类的大部分方法都是同步的（可能出现线程安全的方法都加了 synchronized），多个线程可以安全地访问一个 Vector 对象，而 Arraylist 不是同步的，在不需要保证线程安全时建议使用 Arraylist。

 扩容机制：两者都是动态扩容的，只是 ArrayList 元素超出时扩展当前 size 的 50%，而 Vector 则是直接 double。

 性能：显然不加锁的 ArrayList 要比 Vector 好很多，但是需要线程安全的场景只能选择 Vector

在 Java 中 Stack 类表示后进先出（LIFO）的对象堆栈。栈是一种非常常见的数据结构，它采用典型的先进后出的操作方式完成的。每一个栈都包含一个栈顶，每次出栈是将栈顶的数据取出，

Stack 继承自 Vector 并通过新增五个操作对进行了扩展，其它代码与 Vector 完全一致，因此也是线程安全的。扩展的这五个操作如下：

- empty()：测试堆栈是否为空。
- peek()：查看堆栈顶部的对象，但不从堆栈中移除它。
- pop()：移除堆栈顶部的对象，并作为此函数的值返回该对象。
- push(E item)：把项压入堆栈顶部。
- search(Object o)：返回对象在堆栈中的位置，以 1 为基数。

很容易想到 Stack 的性能会和 Vector 一样较差，如果在不需要同步的场景中使用并不是很合适，那 Stack 有没有非同步的实现呢？通常可以利用 Deque 或者 Deque 的实现类来替换 Stack，比如：

```Java
Deque<Integer> stack = new ArrayDeque<Integer>();
```

ArrayDeque 不是线程安全的，而且它不支持null元素。ArrayDeque 可以用来作为栈或者队列，用作 Stack 的时候，它比 Stack 快；用作 Queue 的时候，它比 LinkedList（LinkedList 本身实现了 Deque 接口，因为可以作为 Queue 来使用）快；后面会专门介绍一下 Queue 系列。


最后总结一下三者的关系和区别：

- Vector 与 ArrayList 基本是一致的，不同的是 Vector 是线程安全的，会在可能出现线程安全的方法前面加上 synchronized 关键字；

- Vector：随机访问速度快，插入和移除性能较差(数组的特点)；支持 null 元素；有顺序；元素可以重复；线程安全；

- Stack：后进先出，实现了一些栈基本操作的方法（其实并不是只能后进先出，因为继承自 Vector，可以有很多操作，从某种意义上来讲，不是一个栈）；




## Collections.synchronized

另外使用中我们也可以通过 `Collections.synchronizedList(new ArrayList(...)` 将 ArrayList 转换为一个线程安全的 SynchronizedRandomAccessList 或 SynchronizedList 对象。同理还有 SynchronizedMap、synchronizedSet 等方法。

既然有线程安全的类（例如 Vector）可以使用，JDK 为什么还要提供另一种方式呢？这里以 synchronizedList 为例大概介绍一下其原理和区别。

- 首先 Vector 是 java.util 包中的一个类，SynchronizedList 是 java.util.Collections 中的一个静态内部类。
- 我们知道 Vector 和 Arraylist 都是 List 的子类，底层数据结构一样，只不过 Vector 内部都是同步方法。而 SynchronizedList 里面实现的方法几乎都是使用同步代码块包上 List 的方法。
- 还有就是上面介绍的扩容机制有区别，调用 add() 方法时如果需要扩容，一个是扩 50% 一个是扩一倍
- SynchronizedList 中实现的类并没有都使用 synchronized 同步代码块。其中有 listIterator 和 listIterator(int index) 并没有做同步处理。但是 Vector 却对该方法加了方法锁。 所以说，在使用 SynchronizedList 进行遍历的时候要手动加锁。这在其源码注释中能看到    
```Java
 * It is imperative that the user manually synchronize on the returned
 * list when iterating over it:
 * <pre>
 *  List list = Collections.synchronizedList(new ArrayList());
 *      ...
 *  synchronized (list) {*      Iterator i = list.iterator(); // Must be in synchronized block
 *      while (i.hasNext())
 *          foo(i.next());
 *  }
 * </pre>
 * Failure to follow this advice may result in non-deterministic behavior.
```
- 还有一个很重要的区别就是，如果我们想要一个线程安全的链表类型的 list，即 LinkedList 的线程安全类，而 JDK 提供的 Vector 底层是动态数组，这时候就只能使用 `Collections.synchronizedList(new LinkedList(...)` 得到 SynchronizedList 来实现。

在平时使用的选择上，其实没有太大区别，个人更倾向于使用 `Collections.synchronizedList` 的方式，因为它有很好的扩展和兼容功能，可以将所有的 List 的子类转成线程安全的类。就是注意 **遍历时要手动进行同步处理**，最后由于 synchronizedList 内部是同步代码块，使用的时候可以指定锁定的对象。

## 参考

- [SynchronizedList 和 Vector 的区别](http://www.hollischuang.com/archives/498)