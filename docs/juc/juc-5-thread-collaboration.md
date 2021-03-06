# 线程协作之使用 wait/notify/notifyAll
---

Java 中常见的关于多线程协作的一些关键字和方法及其作用如下：

![](https://jverson.oss-cn-beijing.aliyuncs.com/935997150ff402d172055664c9122ed3.jpg)

这里用一个示例演示一下 wait、notify、notifyAll 的用法。


## 线程状态

首先我们需要对线程的状态有个了解，Java 中线程中状态可分为五种：New（新建状态），Runnable（就绪状态），Running（运行状态），Blocked（阻塞状态），Dead（死亡状态）。

![](https://jverson.oss-cn-beijing.aliyuncs.com/9a07ccf84afe8dc48770f2fd1a56d4aa.jpg)


- New：新建状态，当线程创建完成时为新建状态，即new Thread(...)，还没有调用start方法时，线程处于新建状态。

- Runnable：就绪状态，当调用线程的的start方法后，线程进入就绪状态，等待CPU资源。处于就绪状态的线程由Java运行时系统的线程调度程序(thread scheduler)来调度。

- Running：运行状态，就绪状态的线程获取到CPU执行权以后进入运行状态，开始执行run方法。

- Blocked：阻塞状态，线程没有执行完，由于某种原因（如，I/O操作等）让出CPU执行权，自身进入阻塞状态。

- Dead：死亡状态，线程执行完成或者执行过程中出现异常，线程就会进入死亡状态。

而线程的协作就涉及到线程在这几种状态之间的切换。


## wait/notify/notifyAll 方法的使用

![](https://jverson.oss-cn-beijing.aliyuncs.com/a6d040d4396be1eda3f49622db24a4c8.jpg)

wait 方法是一个 native 方法，一共有如上三个重载方法。它的作用就是阻塞当前线程等待 notify/notifyAll 方法的唤醒，或等待超时后自动唤醒，或者被其它线程 interrupts。一定要注意**wait notify 和 notifyAll 方法的使用必须在同步代码范围内（即 Synchronized 修饰的方法内），否则就会抛出 IllegalMonitorStateException 异常**

执行下面这段代码就会抛出 `Exception in thread "Thread-0" java.lang.IllegalMonitorStateException` 的异常。看一下对 IllegalMonitorStateException 的解释

> IllegalMonitorStateException Thrown to indicate that a thread has attempted to wait on an object's monitor or to notify other threads waiting on an object's monitor without owning the specified monitor.

大概就是：线程试图等待对象的监视器或者试图通知其他正在等待对象监视器的线程，但本身没有对应的监视器的所有权。因为 wait 方法是一个 native 方法，其底层是通过一个叫做监视器锁的对象来完成的。而这里调用 wait 方式时没有获取到 monitor 对象的所有权，那如何获取 monitor 对象所有权？Java 中只能通过 Synchronized 关键字来完成，修改上述代码，增加Synchronized关键字：

```Java
public class WaitTest {

    public /* synchronized */ void testWait() {   // 增加 synchronized 关键字修饰编程一个同步方法后才能使用 wait
        System.out.println("Start-----");
        try {
            wait(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("End-------");
    }

    public static void main(String[] args) {
        final WaitTest test = new WaitTest();
        new Thread(new Runnable() {
            @Override
            public void run() {
                test.testWait();
            }
        }).start();
    }
}
```

有了对 wait 方法原理的理解，notify 方法和 notifyAll 方法就很容易理解了。既然 wait 方式是通过对象的 monitor 对象来实现的，所以只要在同一对象上去调用 notify/notifyAll 方法，就可以唤醒对应对象 monitor 上等待的线程了。notify 和 notifyAll 的区别在于前者只能唤醒 monitor 上的一个线程，而 notifyAll 则唤醒所有的线程。一般用 notifyAll 避免 notify 在我们程序不严谨的情况下导致有线程永远不能得到唤醒。

![](https://jverson.oss-cn-beijing.aliyuncs.com/e2a885d0e701c2b6bf9a68d982c6fb29.jpg)

另外还需要注意一个通过 wait 方法阻塞的线程，必须同时满足以下两个条件才能被真正执行，下面会有一个示例

- 线程需要被唤醒（超时唤醒或调用 notify/notifyll）。
- 线程唤醒后需要竞争到锁（monitor）。这一点很重要！

## 代码示例

我们实现一个简单的任务队列，有 producer 线程不断生产任务，也有消费线程不断的在消费，有点类似于 ArrayBlockingQueue（使用 ReentrantLock 实现）。

可以看到程序里启动了几个消费线程，刚开始没有任务的时候都会阻塞在 getTask 方法里，该方法在没有任务可消费的情况下回 wait 阻塞并释放对象锁。一旦生产线程 addThread 添加了新任务进去就会调用 notifyAll() 唤醒那些阻塞的线程。注意此时所有被唤醒的消费线程都需要重新获取锁才能往下执行，可以知道在 notifyAll 方法刚刚调用的时候锁是被 addTask 线程持有的，当 addTask 线程执行完毕释放锁之后此时 taskQueue 里也有新的 task 了，这时所有被唤醒的消费线程只有一个能重新获取到对象锁从而获取到最新的 task，而其它线程在 while 判断里 tasks.isEmpty() 仍然为 true，又进入到 wait 状态！


```Java
public class TaskQueueDemo {

    public static void main(String[] args) throws InterruptedException {
        var q = new TaskQueue();
        var consumethreads = Stream.generate(() -> new Thread(() ->{
            for (;;){ // getTask 阻塞获取任务进行执行，执行完毕后重新获取下一个任务
                try {
                    System.out.println(Thread.currentThread().getName() + " consume task: " + q.getTask());
                    Thread.sleep(1000); // 模拟 task 执行时间
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    return;
                }
            }
        })).limit(3).collect(Collectors.toList());
        consumethreads.forEach(Thread::start);

        var addThread = new Thread(() -> {
            for (int i=0; i<10; i++) {
                // 放入task:
                String s = "t-" + i;
                System.out.println("add task: " + s);
                q.addTask(s);
                try { Thread.sleep(100); } catch(InterruptedException e) {}
            }
            System.out.println("all task added!");
        });
        addThread.start();
        addThread.join(); // 等待添加任务的线程执行完
        Thread.sleep(5000); // 再等待一会等消费线程把任务都消费完
        System.out.println("current task queue is " + q.tasks);
        consumethreads.forEach(Thread::interrupt); // 结束所有线程主线程才能退出
    }

    static class TaskQueue {
        Queue<String> tasks = Lists.newLinkedList();
        public synchronized void addTask(String task) {
            tasks.add(task);
            this.notifyAll(); // notifyAll 比 notify 更安全，notify 只唤醒一个线程，如果有多个线程在等待没有考虑周全的话将导致其余线程永久等待下去
        }
        public synchronized String getTask() throws InterruptedException {
            // 注意这里必须用 while 而不是 if，因为被唤醒后需要重新获取锁
            // 例如有 3 个线程同时被唤醒，此时只会有一个线程重新拿到锁判断 tasks.isEmpty() 为 false 执行 tasks.remove()，而其它两个线程再拿到锁的时候又变成 empty 重新进入等待
            while (tasks.isEmpty()) {
                this.wait();
            }
            return tasks.remove();
        }
    }
}
```

输出示例如下:

```bash
add task: t-0
Thread-0 consume task: t-0
add task: t-1
Thread-2 consume task: t-1
add task: t-2
Thread-1 consume task: t-2
add task: t-3
add task: t-4
add task: t-5
add task: t-6
add task: t-7
add task: t-8
add task: t-9
all task added!
Thread-0 consume task: t-3
Thread-2 consume task: t-4
Thread-1 consume task: t-5
Thread-0 consume task: t-6
Thread-2 consume task: t-7
Thread-1 consume task: t-8
Thread-0 consume task: t-9
current task queue is []
java.lang.InterruptedException
```

## ArrayBlockingQueue 基于 ReentrantLock 的实现

上面代码的示例其实是利用 wait/notify 实现了 BlockingQueue（不同于 wait/notify 依赖 synchronized，基于 ReentrantLock 实现） 的功能，现在我们直接使用 BlockingQueue 来实现和上面相同的功能

```Java
public class BlockingQueueDemo {

    public static void main(String[] args) throws InterruptedException {
        BlockingQueue<Integer> taskQueue = new ArrayBlockingQueue<Integer>(5);

        // 创建消费线程阻塞从队列消费任务
        var consumers = Stream.generate(() -> new Thread(() -> {
            try {
                while (true) { // 持续消费
                    Integer task = taskQueue.take(); // 没有任务时阻塞等待
                    System.out.println(Thread.currentThread().getName() + " excute task: " + task);
//                    System.out.println(Thread.currentThread().getName() + " current taskQueue = " + taskQueue);
                    Thread.sleep(100); // 模拟 task 执行时间
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        })).limit(3).collect(Collectors.toList());
        consumers.forEach(Thread::start); // 启动所有消费线程

        // 创建一个生产者线程
        var producer = new Thread(() -> {
            for (int i = 0; i < 10; i++) {
                if (taskQueue.offer(i)) {
                    System.out.println(Thread.currentThread().getName() + " success offer task: " + i);
                    System.out.println(Thread.currentThread().getName() + " current taskQueue = " + taskQueue);
                } else {
                    System.out.println(Thread.currentThread().getName() + " fail to offer task: " + i);
                    System.out.println(Thread.currentThread().getName() + " current taskQueue = " + taskQueue);
                }
                try { Thread.sleep(10); } catch (InterruptedException e) {}
            }
        });
        producer.start();
        producer.join(); // 等待生产任务完成
        Thread.sleep(2000); // 等待所有任务消费完
        consumers.forEach(Thread::interrupt); // 程序执行完毕，结束消费线程
    }

}
```

输出如下

```Java
Thread-3 success offer task: 0
Thread-0 excute task: 0
Thread-3 current taskQueue = []
Thread-3 success offer task: 1
Thread-1 excute task: 1
Thread-3 current taskQueue = []
Thread-3 success offer task: 2
Thread-2 excute task: 2
Thread-3 current taskQueue = [] // 可以看到前三个 task 刚生产就被消费，因为我们提起启动了三个消费线程已经在等待了
Thread-3 success offer task: 3
Thread-3 current taskQueue = [3] // 任务开始积压
Thread-3 success offer task: 4
Thread-3 current taskQueue = [3, 4]
Thread-3 success offer task: 5
Thread-3 current taskQueue = [3, 4, 5]
Thread-3 success offer task: 6
Thread-3 current taskQueue = [3, 4, 5, 6]
Thread-3 success offer task: 7
Thread-3 current taskQueue = [3, 4, 5, 6, 7]
Thread-3 fail to offer task: 8  // 队列已满，添加任务 8 失败
Thread-3 current taskQueue = [3, 4, 5, 6, 7]
Thread-0 excute task: 3
Thread-3 success offer task: 9 // 消费掉一个以后，任务 9 又可以插入到队列了
Thread-3 current taskQueue = [4, 5, 6, 7, 9]
Thread-1 excute task: 4
Thread-2 excute task: 5
Thread-0 excute task: 6
Thread-1 excute task: 7
Thread-2 excute task: 9
java.lang.InterruptedException
```

这里的核心就是我们使用了 juc 包下的 ArrayBlockingQueue 替代了上面我们自己实现的 TaskQueue，我们看看 ArrayBlockingQueue 内部实现有什么不同，可以看到内部基于 ReentrantLock 代替了 synchronized，Condition 的 await 和 signal 分别实现了 wait 和 notify 相同的功能。详细的原理可以先参考以下这篇文章：[ArrayBlockingQueue 详细源码解析](https://juejin.cn/post/6844903989788540941)。

```Java

/** 存储数据的数组 The queued items */
final Object[] items;

/** 获取数据的索引，用于下次 take, poll, peek or remove 等方法 items index for next take, poll, peek or remove */
int takeIndex;

/** 添加元素的索引， 用于下次 put, offer, or add 方法 items index for next put, offer, or add */
int putIndex;

/** 队列元素的个数 Number of elements in the queue */
int count;

/*
 * Concurrency control uses the classic two-condition algorithm
 * found in any textbook.
 */

/** 控制并发访问的锁 Main lock guarding all access */
final ReentrantLock lock;

/** 非空条件对象，用于通知 take 方法中在等待获取数据的线程，队列中已有数据，可以执行获取操作 Condition for waiting takes */
private final Condition notEmpty;

/** 未满条件对象，用于通知 put 方法中在等待添加数据的线程，队列未满，可以执行添加操作 Condition for waiting puts */
private final Condition notFull;

/**
 * Inserts the specified element at the tail of this queue if it is
 * possible to do so immediately without exceeding the queue's capacity,
 * returning {@code true} upon success and {@code false} if this queue
 * is full.  This method is generally preferable to method {@link #add},
 * which can fail to insert an element only by throwing an exception.
 *
 * @throws NullPointerException if the specified element is null
 */
public boolean offer(E e) {
    Objects.requireNonNull(e);
    final ReentrantLock lock = this.lock;
    lock.lock();
    try {
        if (count == items.length)
            return false;
        else {
            enqueue(e);
            return true;
        }
    } finally {
        lock.unlock();
    }
}

/**
 * Inserts element at current put position, advances, and signals.
 * Call only when holding lock.
 */
private void enqueue(E e) {
    // assert lock.isHeldByCurrentThread();
    // assert lock.getHoldCount() == 1;
    // assert items[putIndex] == null;
    final Object[] items = this.items;
    items[putIndex] = e;
    if (++putIndex == items.length) putIndex = 0; //putIndex 进行自增，当达到数组长度的时候，putIndex 重头再来，即设置为0
    count++;
    notEmpty.signal(); //添加完数据后，说明数组中有数据了，所以可以唤醒 notEmpty 条件对象等待队列(链表)中第一个可用线程去 take 数据
}

public E take() throws InterruptedException {
    final ReentrantLock lock = this.lock;
    lock.lockInterruptibly();
    try {
        while (count == 0)
            notEmpty.await();
        return dequeue();
    } finally {
        lock.unlock();
    }
}

/**
 * Extracts element at current take position, advances, and signals.
 * Call only when holding lock.
 */
private E dequeue() {
    // assert lock.isHeldByCurrentThread();
    // assert lock.getHoldCount() == 1;
    // assert items[takeIndex] != null;
    final Object[] items = this.items;
    @SuppressWarnings("unchecked")
    E e = (E) items[takeIndex];
    items[takeIndex] = null;
    if (++takeIndex == items.length) takeIndex = 0; // takeIndex 向前前进一位，如果前进后位置超过了数组的长度，则将其设置为0；
    count--;
    if (itrs != null)
        itrs.elementDequeued();
    notFull.signal(); // 提取完数据后，说明数组中有空位，所以可以唤醒 notFull 条件对象的等待队列(链表)中的第一个可用线程去 put 数据
    return e;
}
```

详细原理可以参考这篇文章：[ArrayBlockingQueue 详细源码解析](https://juejin.cn/post/6844903989788540941)，回头有空再好好整理一下这部分内容。



## 小结

- wait 和 notify 用于多线程协调运行：

- 在 synchronized 内部可以调用 wait() 使线程进入等待状态；即必须在已获得的锁对象上调用wait()方法；

- 在 synchronized 内部可以调用 notify() 或 notifyAll() 唤醒其他等待线程；即必须在已获得的锁对象上调用 notify() 或 notifyAll() 方法；

- 已唤醒的线程还需要重新获得锁后才能继续执行。


## 参考

- [使用 wait 和 notify](https://www.liaoxuefeng.com/wiki/1252599548343744/1306580911915042)
- [Java 并发编程：线程间的协作(wait/notify/sleep/yield/join)](https://www.cnblogs.com/paddix/p/5381958.html)