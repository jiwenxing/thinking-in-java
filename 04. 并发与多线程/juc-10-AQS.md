# AQS 源码及原理
---

在学习 Java 并发包 java.util.concurrent 源码的时候， AQS（AbstractQueuedSynchronizer）抽象类作为 Java 并发包的基础工具类是必不可少的一课，它是实现 ReentrantLock、CountDownLatch、Semaphore、FutureTask 等类的基础。

## AQS 原理

先来看看 AQS 都有哪些属性

```Java
// 头结点，你直接把它当做 当前持有锁的线程 可能是最好理解的
private transient volatile Node head;
// 阻塞的尾节点，每个新的节点进来，都插入到最后，也就形成了一个隐视的链表
private transient volatile Node tail;
// 这个是最重要的，不过也是最简单的，代表当前锁的状态，0代表没有被占用，大于0代表有线程持有当前锁
// 之所以说大于0，而不是等于1，是因为锁可以重入嘛，每次重入都加上1
private volatile int state;
// 代表当前持有独占锁的线程，举个最重要的使用例子，因为锁可以重入
// reentrantLock.lock()可以嵌套调用多次，所以每次用这个来判断当前线程是否已经拥有了锁
// if (currentThread == getExclusiveOwnerThread()) {state++}
private transient Thread exclusiveOwnerThread; //继承自AbstractOwnableSynchronizer
```

AbstractQueuedSynchronizer 的等待队列即由上面两个 Node 节点作为头尾构造而成，队列中每个线程被包装成一个 node，形成一个双向链表，示意如下

![](https://jverson.oss-cn-beijing.aliyuncs.com/aeb0e92a76d5e5b869263516be0118e4.jpg)

其中每个 Node 包含 thread、waitStatus、pre、next 四个属性。

```Java
// 取值为1、-1、-2、-3，或者0
// 这么理解，暂时只需要知道如果这个值 大于0 代表此线程取消了等待，
// 也许就是说半天抢不到锁，不抢了，ReentrantLock是可以指定timeouot的
volatile int waitStatus;
// 前驱节点的引用
volatile Node prev;
// 后继节点的引用
volatile Node next;
// 这个就是线程本尊
volatile Thread thread;

```

上面是一些需要了解的基础，下面以 ReentrantLock 为例探究其原理，上一篇我们已经知道 ReentrantLock 是通过内部类 Sync 来实现锁的管理，Sync 构造方法支持创建公平锁和非公平锁，其继承了 AQS 抽象类。

```Java
abstract static class Sync extends AbstractQueuedSynchronizer {
}

public ReentrantLock(boolean fair) {
    sync = fair ? new FairSync() : new NonfairSync();
}
```

下面以公平锁为例来探究其原理

```Java
/**
 * Sync object for fair locks
 */
static final class FairSync extends Sync {
    private static final long serialVersionUID = -3000897897090466540L;

    //获取锁
    final void lock() {
        acquire(1); //该方法继承自 AQS，直接粘贴到下方
    }

//----------------- 该方法继承自 AQS 抽象类 begin -----------------
    // 如果 tryAcquire(arg) 返回true, 即获取到锁，也就结束了。
    // 否则，acquireQueued 方法会将线程压到队列中
    public final void acquire(int arg) { // 此时 arg == 1
        // 首先调用tryAcquire(1)试一试，成功方法结束，获锁成功。该方法由 FairSync 自己实现在下面
        if (!tryAcquire(arg) &&
            // tryAcquire(arg) 没有成功，这个时候需要把当前线程挂起，放到阻塞队列中。
            acquireQueued(addWaiter(Node.EXCLUSIVE), arg)) {
              selfInterrupt();
        }
    }
//----------------- 该方法继承自 AQS 抽象类 end -------------------    

    /**
     * Fair version of tryAcquire.  Don't grant access unless
     * recursive call or no waiters or is first.
     */
    protected final boolean tryAcquire(int acquires) {
        final Thread current = Thread.currentThread();
        int c = getState(); // 获取 AQS 中的 state
        if (c == 0) { // state == 0 表示此刻没有线程持有锁
            if (!hasQueuedPredecessors() &&  // 看看队列中还有没有等待的线程，虽然此时锁是可以用的，但是这是公平锁，得讲究先来后到
                compareAndSetState(0, acquires)) { //如果没有线程在等待，那就用CAS尝试一下，成功了就获取到锁了，如果失败表示刚刚几乎同时被别的线程抢走了
                setExclusiveOwnerThread(current); // 到这里就是获取到锁了，标记一下，告诉大家，现在是我占用了锁
                return true;
            }
        }
        // 如果锁已被别的线程持有，判断这个线程是不是自己，如果是自己那就是重入，state+1 即可
        else if (current == getExclusiveOwnerThread()) {
            int nextc = c + acquires;
            if (nextc < 0)
                throw new Error("Maximum lock count exceeded");
            setState(nextc);
            return true;
        }
        // 如果到这里，说明前面的 if 和 else if 都没有返回true，说明没有获取到锁
        return false;
    }
}
```


未完待续。。。


参考下面这篇文章将整个流程捋清楚！

## 参考

- [Java并发指南7：JUC的核心类AQS详解](https://blog.csdn.net/zhou920786312/article/details/83658013)
