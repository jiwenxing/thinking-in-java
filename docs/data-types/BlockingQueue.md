# BlockingQueue
---

## BlockingQueue 接口介绍

BlockingQueue 接口有很多实现，可以看到除了 JDK juc 包下的实现外，还有一些第三方的实现。这说明 BlockingQueue 接口是用于并发编程的场景。在并发编程里队列 queue 需要处理多生产者和多消费者并发的场景，而我们选择不同的队列实现往往性能差异会很大，因此有必要梳理一下各个 BlockingQueue 的实现原理及其适用场景。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/728b2925-da37-43c4-80d5-b17ddfc87d17)


## Blocking vs Non-Blocking Queue

BlockingQueue 阻塞队列提供了一种线程同步的机制，即生产者在添加元素的时候需要等待队列有空闲的容量，同理消费者在去处消费元素的时候也需要等待直到有可用的元素。而在非同步队列里遇到这种情况要么抛异常、要么只是返回 null 或 false。

为了实现这个功能，BlockingQueue interface 扩展增加了这两个接口的功能: put and take，其功能和普通队列的 add and remove 相似都是增加或取出元素。但是通过阻塞等待实现了生产和消费线程之间的协作。

下面就简单介绍一下几种常见的 BlockingQueue 的实现

### ArrayBlockingQueue

java.util.concurrent.ArrayBlockingQueue 是一个线程安全的、基于数组、有界的、阻塞的、FIFO 队列。试图向已满队列中放入元素会导致操作受阻塞；试图从空队列中提取元素将导致类似阻塞。此类基于 java.util.concurrent.locks.ReentrantLock 来实现线程安全，所以提供了 ReentrantLock 所能支持的公平性选择。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/57ecb776-869b-4621-bb76-c18f1d4195f4)

ArrayBlockingQueue 是一个阻塞式的有界队列，继承自 AbstractBlockingQueue，间接的实现了 Queue 接口和 Collection 接口。底层以数组的形式保存数据(实际上可看作一个循环数组)。常用的操作包括 add，offer，put，remove，poll，take，peek。我们先来熟悉一下 ArrayBlockingQueue 中的这几个重要的方法。

- add(E e)：把 e 加到 BlockingQueue 里，即如果 BlockingQueue 可以容纳，则返回 true，否则报异常 
- offer(E e)：表示如果可能的话，将 e 加到 BlockingQueue 里，即如果 BlockingQueue 可以容纳，则返回 true，否则返回 false 
- put(E e)：把 e 加到 BlockingQueue 里，如果 BlockQueue 没有空间，则调用此方法的线程被阻断直到 BlockingQueue 里面有空间再继续
- poll(time)：取走 BlockingQueue 里排在首位的对象，若不能立即取出，则可以等 time 参数规定的时间,取不到时返回 null 
- take()：取走 BlockingQueue 里排在首位的对象，若 BlockingQueue 为空，阻断进入等待状态直到 Blocking 有新的对象被加入为止 
- remainingCapacity()：剩余可用的大小。等于初始容量减去当前的 size

ArrayBlockingQueue 队列以数组为载体，配合可重入锁实现生产线程和消费线程共享数据，ArrayBlockingQueue 作为共享池，实现了并发条件下的添加及取出等方法。


作为线程安全的类，ArrayBlockingQueue 的所有公开方法的逻辑都是在加锁的前提下进行的。这里以put方法为例。

通过 put 方法添加元素时，线程会一直等待，直到有空闲空间可以放入元素。

```java
public void put(E e) throws InterruptedException {
    checkNotNull(e); // 不允许存空值，JUC下线程安全的容器都不允许存空值。

    // 在JUC的很多类里，都会看到这种写法：把类的属性赋值给方法内的一个变量。
    // 这是因为类的属性是存放在堆里的，方法内的变量是存放在方法栈上的，访问方法栈比访问堆要快。
    // 在这里，this.lock属性要访问两次，通过赋值给方法的局部变量，就节省了一次堆的访问。
    // 其他的类属性只访问一次就不需要这样处理了。优化无处不在！！
    final ReentrantLock lock = this.lock;

    lock.lockInterruptibly();  // 加锁
    try {
      // 放在循环里是避免虚假唤醒
      // 容器满的时候持续等待
        while (count == items.length)
             // await 方法会导致当前线程释放锁，等待其他线程唤醒，唤醒后重新获取锁
            notFull.await();

        insert(e);
    } finally {
        lock.unlock();  // 释放锁
    }
}
```

另外注意由于其基于数组结构，因此队列需要的内存是预分配好的，这有利于增加吞吐量，当然也会一定程度上造成内存浪费。而 LinkedBlockingQueue needs to allocate and deallocate nodes every time an item is added or removed from the queue. For this reason, an ArrayBlockingQueue can be a better alternative if the queue grows fast and shrinks fast.

### LinkedBlockingQueue

LinkedBlockingQueue 基于 LinkedList 实现，如果没有指定队列大小则默认为 Integer.MAX_VALUE，也可以在初始化的时候通过构造函数指定队列大小。因此 LinkedBlockingQueue 可以是有界的也可以是无界的（optionally-bounded blocking queue）。

需要注意的是 LinkedBlockingQueue 的 put 和 take 操作使用了不同的锁。可以使用

### PriorityBlockingQueue

优先级队列，运行按照自定义的优先级对队列进行消费。其数据结构也是数组类型，不过逻辑结构是通过一个小顶堆或者完全二叉树来实现的优先级排序。

### DelayQueue

延时队列，只能消费已经过期的元素。其本质还是一个 PriorityBlockingQueue 优先级队列，只不多优先级是根据过期时间来决定的。

### LinkedTransferQueue

LinkedTransferQueue 是一个由链表结构组成的无界阻塞队列，它实现了 TransferQueue 接口。TransferQueue 接口继承了 BlockingQueue，主要扩展了两个方法 tryTransfer、transfer。

```Java
public interface TransferQueue<E> extends BlockingQueue<E> {
    // 如果可能，立即将元素转移给等待的消费者。 
    // 更确切地说，如果存在消费者已经等待接收它（在 take 或 timed poll（long，TimeUnit）poll）中，则立即传送指定的元素，否则返回 false。
    boolean tryTransfer(E e);

    // 将元素转移给消费者，如果需要的话等待。 
    // 更准确地说，如果存在一个消费者已经等待接收它（在 take 或timed poll（long，TimeUnit）poll）中，则立即传送指定的元素，否则等待直到元素由消费者接收。
    void transfer(E e) throws InterruptedException;

    // 上面方法的基础上设置超时时间
    boolean tryTransfer(E e, long timeout, TimeUnit unit) throws InterruptedException;

    // 如果至少有一位消费者在等待，则返回 true
    boolean hasWaitingConsumer();

    // 返回等待消费者人数的估计值
    int getWaitingConsumerCount();
}


public boolean tryTransfer(E e) {
    return xfer(e, true, NOW, 0) == null;
}


public void transfer(E e) throws InterruptedException {
    if (xfer(e, true, SYNC, 0) != null) {
        Thread.interrupted(); // failure possible only due to interrupt
        throw new InterruptedException();
    }
}

// 核心 xfer 方法详解
private E xfer(E e, boolean haveData, int how, long nanos) {
    if (haveData && (e == null))
        throw new NullPointerException();
    Node s = null;                        // the node to append, if needed

    retry:
    for (;;) {                            // restart on append race
        // 从  head 开始
        for (Node h = head, p = h; p != null;) { // find & match first node
            // head 的类型。
            boolean isData = p.isData;
            // head 的数据
            Object item = p.item;
            // item != null 有 2 种情况,一是 put 操作, 二是 take 的 itme 被修改了(匹配成功)
            // (itme != null) == isData 要么表示 p 是一个 put 操作, 要么表示 p 是一个还没匹配成功的 take 操作
            if (item != p && (item != null) == isData) { 
                // 如果当前操作和 head 操作相同，就没有匹配上，结束循环，进入下面的 if 块。
                if (isData == haveData)   // can't match
                    break;
                // 如果操作不同,匹配成功, 尝试替换 item 成功,
                if (p.casItem(item, e)) { // match
                    // 更新 head
                    for (Node q = p; q != h;) {
                        Node n = q.next;  // update by 2 unless singleton
                        if (head == h && casHead(h, n == null ? q : n)) {
                            h.forgetNext();
                            break;
                        }                 // advance and retry
                        if ((h = head)   == null ||
                            (q = h.next) == null || !q.isMatched())
                            break;        // unless slack < 2
                    }
                    // 唤醒原 head 线程.
                    LockSupport.unpark(p.waiter);
                    return LinkedTransferQueue.<E>cast(item);
                }
            }
            // 找下一个
            Node n = p.next;
            p = (p != n) ? n : (h = head); // Use head if p offlist
        }
        // 如果这个操作不是立刻就返回的类型    
        if (how != NOW) {                 // No matches available
            // 且是第一次进入这里
            if (s == null)
                // 创建一个 node
                s = new Node(e, haveData);
            // 尝试将 node 追加对队列尾部，并返回他的上一个节点。
            Node pred = tryAppend(s, haveData);
            // 如果返回的是 null, 表示不能追加到 tail 节点,因为 tail 节点的模式和当前模式相反.
            if (pred == null)
                // 重来
                continue retry;           // lost race vs opposite mode
            // 如果不是异步操作(即立刻返回结果)
            if (how != ASYNC)
                // 阻塞等待匹配值
                return awaitMatch(s, pred, e, (how == TIMED), nanos);
        }
        return e; // not waiting
    }
}
```

核心就是这个 xfer 方法，源码在上方，其逻辑如下图所示，可以看到相比较 SynchronousQueue 多了一个可以存储的队列，相比较 LinkedBlockingQueue 多了直接传递元素，少了用锁来同步。

![](https://jverson.oss-cn-beijing.aliyuncs.com/0159f7e907dd33845749a625c76a0fac.jpg)


我们知道 SynchronousQueue 内部无法存储元素，当要添加元素的时候，需要阻塞，不够完美，LinkedBolckingQueue 则内部使用了大量的锁，性能不高。而 LinkedTransferQueue 可以看作是 ConcurrentLinkedQueue、SynchronousQueue、LinkedBlockingQueue 的超集。它不仅仅综合了这几个类的功能，同时也提供了更高效的实现。

使用场景：当我们不想生产者过度生产消息时，TransferQueue 可能非常有用，可避免发生 OutOfMemory 错误。在这样的设计中，消费者的消费能力将决定生产者产生消息的速度。



### SynchronousQueue

SynchronousQueue 的特别之处在于它内部没有容器，一个生产线程，当它生产即 put 的时候，如果当前没有人想要消费(即当前没有线程执行 take)，此生产线程必须阻塞，等待一个消费线程调用 take 操作，take 操作将会唤醒该生产线程，同时消费线程会获取生产线程的产品（即数据传递），这样的一个过程称为一次配对过程(当然也可以先 take 后 put,原理是一样的)。是一种线程与线程间一对一传递消息的模型。


JDK 提供的 newCachedThreadPool 线程池就是用了 SynchronousQueue 做任务队列，而他的核心线程数为0，最大线程数为无限大。因为核心线程数为0，所以任务来时，只能新建线程（如果没有空闲的线程），因为 SynchronousQueue 队列没有容量，不能存放任务，有了工作线程之后通过 SynchronousQueue 队列获取任务。所以这个线程池不能用于执行耗时的任务，因为他的最大线程数为无限大，很可能会建很多的线程。

```Java
public static ExecutorService newCachedThreadPool() {
    return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                  60L, TimeUnit.SECONDS,
                                  new SynchronousQueue<Runnable>());
}
```


### ConcurrentLinkedQueue

基于 CAS 实现的无锁的并发队列。

## 队列的使用场景

队列的目的就是为生产者和消费者提供一个地方存放要交互的数据，帮助缓冲它们之间传递的消息。这意味着缓冲常常是满的（生产者比消费者快）或者空的（消费者比生产者快）。生产者和消费者能够步调一致的情况非常少见。

考虑应用场景中对队列边界的要求。ArrayBlockingQueue 是有明确的容量限制的，而 LinkedBlockingQueue 则取决于我们是否在创建时指定，SynchronousQueue 则干脆不能缓存任何元素。

从空间利用角度，数组结构的 ArrayBlockingQueue 要比 LinkedBlockingQueue 紧凑，因为其不需要创建所谓节点，但是其初始分配阶段就需要一段连续的空间，所以初始内存需求更大。

通用场景中，LinkedBlockingQueue 的吞吐量一般优于 ArrayBlockingQueue，因为它实现了更加细粒度的锁操作。ArrayBlockingQueue 实现比较简单，性能更好预测，属于表现稳定的“选手”。

如果需要实现的是两个线程之间接力性（handoff）的场景，可能会选择CountDownLatch，但是SynchronousQueue也是完美符合这种场景的，而且线程间协调和数据传输统一起来，代码更加规范。

可能令人意外的是，很多时候SynchronousQueue的性能表现，往往大大超过其他实现，尤其是在队列元素较小的场景。

池资源 - 诸如线程池、数据库连接池之类的池资源都会使用到队列。比如，当池子中没有空闲资源的时候，新的线程任务还去池子中请求资源该怎么办？通常有如下两种策略：第一种非阻塞式地拒绝请求，这种方式不涉及排队，因此用不着队列；第二种阻塞式地等待资源；如果需要公平地对待每一个请求，符合先进先出的特点，那么就是用队列。使用顺序队列和链式队列在这里有着不同的作用。

链式队列可以实现一个支持无限排队的无界队列，但是极有可能导致排队过多，请求被处理的时效非常长，所以，如果是针对响应时间要求高的系统，那么链式队列是不合适的；

顺序队列则是有界的，当请求入队使得队列满了之后，后续的请求都会被拒绝，比较适合对响应时间要求高的系统。需要注意的是，顺序队列的大小设置需要按照实际的业务场景和并发量进行考究，太大容易导致排队请求过多，太小容易导致系统资源无法充分利用。

## Disruptor 

Disruptor 是英国外汇交易公司 LMAX 开发的一个高性能队列，研发的初衷是解决内存队列的延迟问题。Disruptor 是一个开源的并发框架，并获得2011 Duke’s 程序框架创新奖，能够在无锁的情况下实现网络的 Queue 并发操作。

目前，包括 Apache Storm、Camel、Log4j2 在内的很多知名项目都应用了 Disruptor 以获取高性能。

> Disruptor提供了一种线程之间信息交换的方式。

队列的底层一般分成三种：数组、链表和堆。其中，堆一般情况下是为了实现带有优先级特性的队列，暂且不考虑。

我们就从数组和链表两种数据结构来看，基于数组线程安全的队列，比较典型的就是上面说的 ArrayBlockingQueue，它主要通过加锁的方式来保证线程安全；基于链表的线程安全队列分成 LinkedBlockingQueue 和 ConcurrentLinkedQueue 两大类，前者也通过锁的方式来实现线程安全，而后者以及上面表格中的 LinkedTransferQueue 都是通过原子变量 compare and swap（以下简称“CAS”）这种不加锁的方式来实现的。

通过不加锁的方式实现的队列都是无界的（无法保证队列的长度在确定的范围内）；而加锁的方式，可以实现有界队列。在稳定性要求特别高的系统中，为了防止生产者速度过快，导致内存溢出，只能选择有界队列；同时，为了减少 Java 的垃圾回收对系统性能的影响，会尽量选择array/heap 格式的数据结构。这样筛选下来，符合条件的队列就只有ArrayBlockingQueue。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/166c0787-cc08-459c-9b35-e5943582d0b7)

log4j2 的异步日志是通过队列来处理的，关于队列，Log4j2 支持生成以下四种队列：

- ArrayBlockingQueue -- 默认的队列，通过 java 原生的 ArrayBlockingQueue 实现。

- DisruptorBlockingQueue -- disruptor 包实现的高性能队列。

- JCToolsBlockingQueue -- JCTools 实现的无锁队列。

- LinkedTransferQueue -- 通过 java7 以上原生支持的 LinkedTransferQueue 实现。

默认的是 ArrayBlockingQueue，最为推荐的是 disruptor 包实现的高性能队列 DisruptorBlockingQueue，他是英国外汇交易公司 LMAX 开源的、用于替代并发线程间数据交换的环形队列的、基本无锁的开源线程间通信框架。究竟 DisruptorBlockingQueue 的高性能队列是如何实现的呢

参考以下文章

系列文章：http://ifeve.com/disruptor/；英文原文：https://lmax-exchange.github.io/disruptor/disruptor.html

美团技术博客：https://tech.meituan.com/2016/11/18/disruptor.html

wiki：https://km.sankuai.com/page/14682418

https://my.oschina.net/u/3647019/blog/4927017

https://github.com/LMAX-Exchange/disruptor

http://ifeve.com/disruptor/

Linux 环形缓存

https://km.sankuai.com/page/657349677

[A Guide to Concurrent Queues in Java](https://www.baeldung.com/java-concurrent-queues)

