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


后面我们会结合 ArrayBlockingQueue 讲解使用 ReentrantLock 来实现同样的功能。

## 小结

- wait和notify用于多线程协调运行：

- 在synchronized内部可以调用wait()使线程进入等待状态；即必须在已获得的锁对象上调用wait()方法；

- 在synchronized内部可以调用notify()或notifyAll()唤醒其他等待线程；即必须在已获得的锁对象上调用notify()或notifyAll()方法；

- 已唤醒的线程还需要重新获得锁后才能继续执行。


## 参考

- [使用 wait 和 notify](https://www.liaoxuefeng.com/wiki/1252599548343744/1306580911915042)
- [Java 并发编程：线程间的协作(wait/notify/sleep/yield/join)](https://www.cnblogs.com/paddix/p/5381958.html)