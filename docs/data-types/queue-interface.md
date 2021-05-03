# Queue 接口及其实现类介绍
---

通过前面的介绍我们知道，Java 中最基础的 Collection 接口有 List、Queue 和 Set。其中 List 的主要实现有 ArrayList、LinkedList、Vector、Stack 等，ArrayList 和 LinkedList 底层数据结构不同，Vector 是线程安全的 ArrayList（每个方法都加了 synchronized），Stack 则直接继承了 Vector 并增加了几个方式如 push、pop、peek 等，因此 Vector 和 Stack 底层都是数组结构；Set 我们知道是基于 Map 接口的实现和 HashMap 数据结构基本一致。今天我们来聊聊 Quene！

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/098b0e63-a9de-43ce-91e1-a2cb7b99793d)


## Queue 介绍

Quene 是先进先出的队列，其实就是一个有序 List，和 List 的区别是它限制了一些操作，List可以在任意位置添加和删除元素，而 Queue 只有两个操作：1. 把元素添加到队列末尾；2. 从队列头部取出元素。因此我们也可以把 List 当做一个 Queue 来使用。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/30d92cd8-9cf9-41db-b8ff-d7760a6586ca)

可以看到 Queue 有很多实现，这些实现都有一个共同的特点：添加、删除和获取队列元素总是有两个方法，在添加或获取元素失败时，这两个方法的行为是不同的。用一个表格总结如下：

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/2ccac6be-796d-4dfb-af0d-9b80ddba34e8)

## PriorityQueue

PriorityQueue 实现了一个优先队列：从队首获取元素时，总是获取优先级最高的元素。换句话说我们可以自定义出队的优先级，打破先进先出的规则。这里的关键就是构造函数里传入一个 Comparator

```Java
Queue<User> q = new PriorityQueue<>(new UserComparator());

// Comparator 实现举例
class UserComparator implements Comparator<User> {
    public int compare(User u1, User u2) {
        if (u1.number.charAt(0) == u2.number.charAt(0)) {
            // 如果两人的号都是A开头或者都是V开头,比较号的大小:
            return u1.number.compareTo(u2.number);
        }
        if (u1.number.charAt(0) == 'V') {
            // u1的号码是V开头,优先级高:
            return -1;
        } else {
            return 1;
        }
    }
}
```

另外注意 PriorityQueue 是 小根堆的数据结构，也是一棵完全二叉树，而其存储结构是一个数组。逻辑结构层次遍历的结果刚好就是这个数组如下图。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/3ecba23d-4203-41d2-b137-3dd156178892)

因此如果直接打印 PriorityQueue.toString 输出的是数组的顺序，如果想按照顺序打印队列得按照消费的方式不断 poll 进行遍历。

```Java
@Test
public void testPriorityQuene() {
    PriorityQueue<Integer> pq = new PriorityQueue<>(5, Collections.reverseOrder());
    pq.offer(4);
    pq.offer(2);
    pq.offer(1);
    pq.offer(5);
    pq.offer(3);
    System.out.println(pq); // [5, 4, 1, 2, 3]  直接输入的是其物理结构的数组
    while (pq.size() > 0) {  // 5 4 3 2 1  poll 才能按照预期的顺序出队列
        System.out.println(pq.poll());
    }
}
```

这里涉及到堆的概念和理解，可以单开一节深入研究一下（主要是堆排序）

- 堆也是一颗树
- 堆最为主流的一种实现方式：二叉堆
- 二叉堆是一颗完全二叉树
- 可以用数组来存储二叉堆

关于 PriorityQueue 也有一些总结：

- PriorityQueue 是基于最大优先级堆实现的，根据比较器的情况可以是大根堆或者小根堆；
- PriorityQueue 不支持 null；
- PriorityQueue 不是线程安全的，多线程环境下可以使用 java.util.concurrent.PriorityBlockingQueue；
- 使用 iterator() 遍历时，不保证输出的序列是有序的，其实遍历的是存储数组。


## Deque（Double Ended Queue）

`public interface Deque<E> extends Queue<E>`

我们知道，Queue是队列，只能一头进，另一头出。而 Deque 允许两头都进，两头都出，这种队列叫双端队列 Deque（Double Ended Queue）。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/f992b551-0b2d-411d-8b01-c1ac6e13ea1f)

可以看到 Queue 提供的 add()/offer() 方法在 Deque 中也可以使用，但是使用Deque，最好不要调用 offer()，而是显式调用 offerLast()：

另外 Deque 是一个接口，它的实现类有 ArrayDeque 和 LinkedList（之前介绍过其底层是一个双向链表）。我们发现 LinkedList 真是一个全能选手，它即是 List，又是 Queue，还是 Deque。因此可以直接将 LinkedList 当做 Deque 使用

```Java
// 不推荐的写法:
LinkedList<String> d1 = new LinkedList<>();
d1.offerLast("z");
// 推荐的写法：
Deque<String> d2 = new LinkedList<>();
d2.offerLast("z");
```

## BlockingQueue

可以发现其它的 Queue 都在 java util 包里，而 BlockingQueue 在 concurrent 包里，这说明其是一个主要用于多线程的队列。其实现主要有以下这些（包括一些第三方的实现），详细内容将在并发的章节里深入学习。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/dbd3c50c-1d73-495d-b404-9fbe324e75ce)

