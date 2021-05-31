# 并发工具类：CountDownLatch
---

## CountDownLatch 用法


CountDownLatch 与其他并发编程工具类，如 CyclicBarrier、Semaphore、ConcurrentHashMap 和 BlockingQueue 等在 java.util.concurrent包中与 JDK 1.5 一起被引入。CountDownLatch 能让一个java线程等待其他线程完成任务，比如 Application 的主线程等待，直到其他负责启动框架服务的服务线程完成所有服务的启动。



用法也很简单： 

1. 初始化一个 CountDownLatch(int counter)，counter 和要并发的线程数相同
2. 每个线程持有这个 CountDownLatch 引用，线程在 task 执行完之后执行 countdown() 通知该线程已完成
3. 调用线程需要在启动完各个线程之后立即调用  await() 进行等待
4. 正常所有线程执行完后会 countdown() 到 0，此时调用线程解除等待继续执行

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/21a327bb-743e-4e8d-a8cd-8477cf53f6ac)

## 使用场景

### Waiting for a Pool of Threads to Complete

最典型的场景就是调用线程（不一定是主线程，任何线程都可以）等待其它子线程全部执行完再继续执行。注意这里有个细节，在调用 await 时需要设置超时时间，如果业务需要还要判断是否都执行成功，用来防止某个子线程异常没有执行到 countDown 导致调用线程永久等待！

```Java
public class TempTest {

    public static void main(String[] args) throws InterruptedException {
        System.out.println(LocalTime.now() + ": " + Thread.currentThread().getName() + " start!");
        CountDownLatch countDownLatch = new CountDownLatch(5); 
        List<Thread> workers = Stream.generate(() -> new Thread(new Worker(countDownLatch)))
                .limit(5).collect(toList()); // 创建 n 个子线程，将 countDownLatch 引用传入子线程
        workers.forEach(Thread::start); // 启动子线程并发执行
        final boolean completed = countDownLatch.await(5, TimeUnit.SECONDS);// 重要！启动线程后主线程需要主动调用 await 等待子线程全部完成；另外这里需要设置超时时间，防止有子线程异常导致这里永远陷入等待！
        System.out.println(LocalTime.now() + ": " + Thread.currentThread().getName() + " end!");
    }

    static class Worker implements Runnable {
        private CountDownLatch countDownLatch;
        public Worker(CountDownLatch countDownLatch) {
            this.countDownLatch = countDownLatch;
        }
        @Override
        public void run() {
            doSomeWork();
            countDownLatch.countDown(); // 子线程执行结束记得执行 countDown
        }

        private void doSomeWork() {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(LocalTime.now() + ": " + Thread.currentThread().getName() + " done!");
        }
    }

}
```

执行结果如下

> 10:56:28.092: main start!    
10:56:29.103: Thread-1 done!    
10:56:29.103: Thread-2 done!    
10:56:29.103: Thread-0 done!    
10:56:29.103: Thread-3 done!    
10:56:29.103: Thread-4 done!    
10:56:29.103: main end!


### A Pool of Threads Waiting to Begin

还有一种场景就是模拟并发，很久之前在一篇文档里有记录通过模拟并发来复现线上问题，见 [Java模拟并发请求](https://jverson.com/2016/05/24/concurrent/)

简单讲就是我们先创建一个 count 为 1 的 countDownLatch，然后逐个启动子线程在其执行 task 任务之前先调用 await 等待，等所有的子线程都启动完成进入到 await 时，在主线程里调用 countDown() 打开门闩，这时所有子线程将同时解除等待真正并发执行。

```Java
@Test
public void test2() throws InterruptedException {
    System.out.println(LocalTime.now() + ": " + Thread.currentThread().getName() + " 程序开始执行!");
    final ExecutorService exec = Executors.newFixedThreadPool(5);
    CountDownLatch countDownLatch = new CountDownLatch(1); // 创建一个 count 为 1 的门闩锁
    List<Runnable> workers = Stream.generate(() -> new Worker2(countDownLatch)).limit(5).collect(toList());
    workers.forEach(exec::submit);
    System.out.println(LocalTime.now() + ": " + Thread.currentThread().getName() + " 所有线程已提交线程池!");
    countDownLatch.countDown();
    System.out.println(LocalTime.now() + ": " + Thread.currentThread().getName() + " 执行 countDown() 打开门闩!");
    exec.shutdown(); // 注意 shutdown 关闭线程池虽然会允许之前提交的线程执行完，但是不会阻塞等待，如果需要等待需要再调用 awaitTermination
    exec.awaitTermination(2000, TimeUnit.MILLISECONDS);
    System.out.println(LocalTime.now() + ": " + Thread.currentThread().getName() + " 程序执行完毕，关闭线程池!");
}

static class Worker2 implements Runnable {
    private CountDownLatch countDownLatch;
    public Worker2(CountDownLatch countDownLatch) {
        this.countDownLatch = countDownLatch;
    }
    @SneakyThrows
    @Override
    public void run() {
        countDownLatch.await(); // 执行task之前先进入等待
        doSomeWork();
    }
    private void doSomeWork() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(LocalTime.now() + ": " + Thread.currentThread().getName() + " done!");
    }
}
```

输出如下

> 12:22:52.753: main 程序开始执行!  
12:22:52.771: main 所有线程已提交线程池!  
12:22:52.771: main 执行 countDown() 打开门闩!  
12:22:53.773: pool-1-thread-1 done!  
12:22:53.773: pool-1-thread-2 done!  
12:22:53.773: pool-1-thread-5 done!  
12:22:53.773: pool-1-thread-3 done!  
12:22:53.773: pool-1-thread-4 done!  
12:22:53.774: main 程序执行完毕，关闭线程池!

# CountDownLatch 工作原理

CountDownLatch 类存在一个内部类 Sync，继承自 AbstractQueuedSynchronizer，其源代码如下。对 CountDownLatch 方法的调用基本都会转发到对 Sync 或 AQS 的方法的调用，所以，CountDownLatch 内部的实现基本就是基于 AQS，深入解读可以参考 [JUC工具类: CountDownLatch详解](https://www.pdai.tech/md/java/thread/java-thread-x-juc-tool-countdownlatch.html)，关于 AQS 可以参考之前的笔记 [AQS 源码及原理](https://jverson.com/thinking-in-java/juc/juc-10-AQS.html)

```Java
/**
     * Synchronization control For CountDownLatch.
     * Uses AQS state to represent count.
     */
    private static final class Sync extends AbstractQueuedSynchronizer {
        private static final long serialVersionUID = 4982264981922014374L;

        Sync(int count) {
            setState(count);
        }

        int getCount() {
            return getState();
        }

        protected int tryAcquireShared(int acquires) {
            return (getState() == 0) ? 1 : -1;
        }

        protected boolean tryReleaseShared(int releases) {
            // Decrement count; signal when transition to zero
            for (;;) {
                int c = getState();
                if (c == 0)
                    return false;
                int nextc = c-1;
                if (compareAndSetState(c, nextc))
                    return nextc == 0;
            }
        }
    }
```

## 参考

- [Guide to CountDownLatch in Java](https://www.baeldung.com/java-countdown-latch)



