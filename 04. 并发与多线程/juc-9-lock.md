# ReentrantLock 及 AQS
---

Lock 接口是 juc 提供的一套类似于 synchronized 的用于控制并发线程访问共享资源的工具。ReentrantLock 是 Lock 接口的默认实现，是一种独占锁。相对 synchronized 而言，ReentrantLock 提供了更多的操作方式以及更细粒度的加锁方式。

synchronized 是基于 JVM 实现的，内置锁，Java 中的每一个对象都可以作为锁。Lock 是基于在语言层面实现的锁，Lock 锁可以被中断，支持定时锁。Lock 可以提高多个线程进行读操作的效率。一般对于数据结构设计或者框架的设计都倾向于使用 Lock，但是随着 JDK 对 synchronized 的不断优化，两者的性能将会逐渐接近。


## Lock 体系

下图是 juc 包下 Lock 目录中所有的对象，

![](https://jverson.oss-cn-beijing.aliyuncs.com/aa93a2e41f616a3232b9e1aa27b48686.jpg)

整个 Lock 框架及其依赖关系如下图所示，其中 AbstractQueuedSynchronizer（AQS）及 LockSupport 等类是其核心，需要重点学习和理解。

![](https://jverson.oss-cn-beijing.aliyuncs.com/383019f54ccf087c317b480b315314ea.jpg)

## ReentrantLock

先来看比较核心的 ReentrantLock 类，ReentrantLock 是 Lock 接口的默认实现，是一种独占锁。具有以下主要特性：

- 可重入。ReentrantLock 是可重入锁，因为它会记录之前获得锁线程对象，保存在 exclusiveOwenerThread 变量中，当一个线程要获取锁时，会先判断当前线程是不是已经获取锁的线程。synchronized 也是可重入锁。

- 可中断。ReentrantLock 是可中断锁，它提供了 lockInterruptibly 这种可中断的加锁方式，可以有效的避免线程之间因为互相持续占有资源而导致阻塞。synchronized 无法实现可中断。

- 公平锁与非公平锁可选。ReentrantLock **默认是非公平锁**，但是也可以通过构造方法选择非公平锁。公平锁是指当多个线程尝试获取同一个锁时，获取锁的顺序按照到达的时间顺序排序。

其构造函数如下，可以看到其主要依靠 sync 类实现，并且默认为非公平锁：

```Java
/**
 * Creates an instance of {@code ReentrantLock}.
 * This is equivalent to using {@code ReentrantLock(false)}.
 */
public ReentrantLock() {sync = new NonfairSync();
}

/**
 * Creates an instance of {@code ReentrantLock} with the
 * given fairness policy.
 *
 * @param fair {@code true} if this lock should use a fair ordering policy
 */
public ReentrantLock(boolean fair) {sync = fair ? new FairSync() : new NonfairSync();}

abstract static class Sync extends AbstractQueuedSynchronizer {//...}
```

Sync 是 ReentrantLock 中定义的一个抽象类，FairSync 及 NonfairSync 都继承自 Sync。Sync 则继承了 AQS 抽象类。AQS，AbstractQueuedSynchronizer，即队列同步器。它是构建锁或者其他同步组件的基础框架（如 ReentrantLock、ReentrantReadWriteLock、Semaphore）等。

AQS 最核心的数据结构是一个 volatile int state 和 一个 FIFO 线程等待对列。state 代表共享资源的数量，如果是互斥访问，一般设置为 1，而如果是共享访问，可以设置为 N（N 为可共享线程的个数）；而线程等待队列是一个双向链表，无法立即获得锁而进入阻塞状态的线程会加入队列的尾部。当然对 state 以及队列的操作都是采用了 volatile + CAS + 自旋的操作方式，采用的是乐观锁的概念。

**AQS 是并发组件包 JUC(Java Util Concurrency) 的核心，包括 ReentrantLock 在内的很多并发组件如 Condition、BlockingQueue 以及线程池里使用的worker等都是基于其实现的，下一篇将会重点介绍一下 AQS 的原理及其应用。**


## 代码中使用示例

```Java
public class OrderService {
    private static ReentrantLock reentrantLock = new ReentrantLock(true);
    public void createOrder() {
        // 比如我们同一时间，只允许一个线程创建订单
        reentrantLock.lock();
        // 通常，lock 之后紧跟着 try 语句，然后在 finally 中确保释放锁
        try {
            // 这块代码同一时间只能有一个线程进来(获取到锁的线程)，
            // 其他的线程在lock()方法上阻塞，等待获取到锁，再进来
            // 执行代码...
        } finally {
            // 需要显式的释放锁，否则会造成死锁
            reentrantLock.unlock();
        }
    }
}

```


## ReetrantReadWriteLock

ReetrantReadWriteLock 是一种读写锁，其实现了 ReadWriteLock 接口，该接口中有两个方法：获取读锁、获取写锁。它将文件的读写操作分开，分成 2 个锁来分配给线程，从而使得多个线程可以同时进行读操作。

需要注意的是读写锁的规则：

1. 如果有一个线程已经占用了读锁，则此时其他线程如果要申请写锁，则申请写锁的线程会一直等待释放读锁。
2. 如果有一个线程已经占用了写锁，则此时其他线程如果申请写锁或者读锁，则申请的线程会一直等待释放写锁。

```Java
public interface ReadWriteLock {Lock readLock();       // 获取读锁  
    Lock writeLock();      // 获取写锁} 
```

将读写锁分开在特定的一些场景中相较于 synchronized 这种排它锁能够显著的提升程序的并发性能，下面接口源码上的注释很清晰的解释了这些场景，主要依赖于读写的频率以及每次读写所占用的时间，比如读多写少的情况就是适用读写锁的典型场景，当然实际的性能提升情况还需要经过测试得出。

> Whether or not a read-write lock will improve performance over the use of a mutual exclusion lock depends on the frequency that the data is
read compared to being modified, the duration of the read and write
operations, and the contention for the data - that is, the number of
threads that will try to read or write the data at the same time.
For example, a collection that is initially populated with data and
thereafter infrequently modified, while being frequently searched
(such as a directory of some kind) is an ideal candidate for the use of
a read-write lock. However, if updates become frequent then the data
spends most of its time being exclusively locked and there is little, if any
increase in concurrency. Further, if the read operations are too short
the overhead of the read-write lock implementation (which is inherently
more complex than a mutual exclusion lock) can dominate the execution
cost, particularly as many read-write lock implementations still serialize
all threads through a small section of code. Ultimately, only profiling
and measurement will establish whether the use of a read-write lock is
suitable for your application.


## ReentrantLock vs synchronized

两种锁的区别如下：

- Lock 是一个接口，是基于在语言层面实现的锁；而 synchronized 是 Java 中的关键字，synchronized 是基于 JVM 实现的，内置锁，Java 中的每一个对象都可以作为锁。；
- synchronized 在发生异常时，会自动释放线程占有的锁，因此不会导致死锁现象发生；而 Lock 在发生异常时，如果没有主动通过 unLock() 去释放锁，则很可能造成死锁现象，因此使用 Lock 时需要在 finally 块中释放锁；
- Lock 可以让等待锁的线程响应中断，而 synchronized 却不行，使用 synchronized 时，等待的线程会一直等待下去，不能够响应中断；
- 通过 Lock 可以知道有没有成功获取锁，而 synchronized 却无法办到。
- Lock 可以提高多个线程进行读操作的效率。（可以通过 readwritelock 实现读写分离）
- 性能上来说，在资源竞争不激烈的情形下，Lock 性能稍微比 synchronized 差点（编译程序通常会尽可能的进行优化 synchronized）。但是当同步非常激烈的时候，synchronized 的性能一下子能下降好几十倍。而 ReentrantLock 确还能维持常态。

## 总结

伴随着 JDK 对内置锁 synchronized 的不断优化，选择 ReentrantLock 和 synchronized 时效率不应该是主要原因了，而是应该考虑锁是否需要公平性，是否需要可中断，可共享等来作为选择依据。

## 参考

- [并发编程的锁机制：synchronized和lock](https://juejin.im/post/5a43ad786fb9a0450909cb5f#heading-17)