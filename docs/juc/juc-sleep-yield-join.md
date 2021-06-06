# 线程协作之 sleep/yield/join
--- 

在[线程协作之使用 wait/notify/notifyAll](https://jverson.com/thinking-in-java/juc/juc-5-thread-collaboration.html)中我们讲解了 wait 和 notify 方法的使用和原理，现在我们再来看另外一组线程间协作的方法 sleep/yield/join。他们最明显区别是：这几个方法都位于 Thread 类中，而 wait/notify/notifyAll 三个方法都位于 Object 类中。现在我们逐个分析 sleep/yield/join 方法


## sleep

sleep 方法的作用是让当前线程暂停指定的时间（毫秒），sleep 方法是最简单的方法，在前面的例子中也经常用到，比较容易理解。唯一需要注意的是其与 wait 方法的区别。

- wait 方法依赖于同步，而 sleep 方法可以直接调用。
- 更深层次的区别在于 sleep 方法只是暂时让出 CPU 的执行权，并不释放锁。而 wait 方法则需要释放锁。

```Java
public class SleepWaitTest {

    public synchronized void testSleep() {
        System.out.println(Thread.currentThread().getName() + " start!");
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " end!");
    }

    public synchronized void testWait() {
        System.out.println(Thread.currentThread().getName() + " start!");
        try {
            wait(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(Thread.currentThread().getName() + " end!");
    }

    public static void main(String[] args) throws InterruptedException {
        SleepWaitTest sleepWaitTest = new SleepWaitTest();
        System.out.println("--- test sleep ---");
        Stream.generate(() -> new Thread(() -> {
            sleepWaitTest.testSleep();
        })).limit(3).collect(Collectors.toList()).forEach(Thread::start);

        Thread.sleep(5000);

        System.out.println("--- test wait ---");
        Stream.generate(() -> new Thread(() -> {
            sleepWaitTest.testWait();
        })).limit(3).collect(Collectors.toList()).forEach(Thread::start);

    }

}
```

输出如下，显然 sleep 的时候依然持有锁，因此其它线程无法进入到方法，只要一个线程执行完释放锁之后其它线程才能进入方法；而当调用 wait 方法后，当前线程会释放持有的 monitor 对象锁，因此，其他线程还可以进入到同步方法，线程被唤醒后，需要竞争锁，获取到锁之后再继续执行。

```
--- test sleep ---
Thread-0 start!
Thread-0 end!
Thread-2 start!
Thread-2 end!
Thread-1 start!
Thread-1 end!
--- test wait ---
Thread-3 start!
Thread-5 start!
Thread-4 start!
Thread-5 end!
Thread-4 end!
Thread-3 end!
```

## yield

yield 方法的作用是暂停当前线程，以便其他线程有机会执行，不过不能指定暂停的时间，并且也不能保证当前线程马上停止。yield 方法只是将 Running 状态转变为 Runnable 状态。我们还是通过一个例子来演示其使用：

```Java
public class YieldTest {

    public static void main(String[] args) {
        Stream.generate(() -> new Thread(() -> {
            Stream.iterate(1, n -> n +1).limit(5).forEach(i -> {
                try { Thread.sleep(500); } catch (InterruptedException e) {} // 减缓一下打印的速度便于观察
                System.out.println(Thread.currentThread().getName() + ": " + i);
                Thread.yield(); // 把 cpu 让给其它线程
            });
        })).limit(2).collect(Collectors.toList()).forEach(Thread::start);
    }

}


/**
 * A hint to the scheduler that the current thread is willing to yield
 * its current use of a processor. The scheduler is free to ignore this
 * hint.
 *
 * <p> Yield is a heuristic attempt to improve relative progression
 * between threads that would otherwise over-utilise a CPU. Its use
 * should be combined with detailed profiling and benchmarking to
 * ensure that it actually has the desired effect.
 *
 * <p> It is rarely appropriate to use this method. It may be useful
 * for debugging or testing purposes, where it may help to reproduce
 * bugs due to race conditions. It may also be useful when designing
 * concurrency control constructs such as the ones in the
 * {@link java.util.concurrent.locks} package.
 */
public static native void yield();
```

输出如下，我们通过 yield 方法来实现两个线程的交替执行。不过请注意，这种交替并不一定能得到保证，上面源码的注释中也对这个问题进行说明。


```
Thread-0: 1
Thread-1: 1
Thread-1: 2
Thread-0: 2
Thread-0: 3
Thread-1: 3
Thread-0: 4
Thread-1: 4
Thread-0: 5
Thread-1: 5
```

yield 仅做了解即可，由于其效果不好预测，生产环境很少使用。

- 调度器可能会忽略该方法。
- 使用的时候要仔细分析和测试，确保能达到预期的效果。
- 很少有场景要用到该方法，主要使用的地方是调试和测试。


## join

join 方法的作用是父线程等待子线程执行完成后再执行，换句话说就是将异步执行的线程合并为同步的线程。JDK 中提供三个版本的 join 方法，其实现与 wait 方法类似，join() 方法实际上执行的 join(0)，而 join(long millis, int nanos) 也与 wait(long millis, int nanos) 的实现方式一致，暂时对纳秒的支持也是不完整的。我们可以看下 join 方法的源码，这样更容易理解

```Java
    /**
     * Waits at most {@code millis} milliseconds for this thread to
     * die. A timeout of {@code 0} means to wait forever.
     *
     * <p> This implementation uses a loop of {@code this.wait} calls
     * conditioned on {@code this.isAlive}. As a thread terminates the
     * {@code this.notifyAll} method is invoked. It is recommended that
     * applications not use {@code wait}, {@code notify}, or
     * {@code notifyAll} on {@code Thread} instances.
     *
     * @param  millis
     *         the time to wait in milliseconds
     *
     * @throws  IllegalArgumentException
     *          if the value of {@code millis} is negative
     *
     * @throws  InterruptedException
     *          if any thread has interrupted the current thread. The
     *          <i>interrupted status</i> of the current thread is
     *          cleared when this exception is thrown.
     */
    public final synchronized void join(long millis)
    throws InterruptedException {
        long base = System.currentTimeMillis();
        long now = 0;

        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        if (millis == 0) {
            while (isAlive()) {
                wait(0);
            }
        } else {
            while (isAlive()) {
                long delay = millis - now;
                if (delay <= 0) {
                    break;
                }
                wait(delay);
                now = System.currentTimeMillis() - base;
            }
        }
    }

    /**
     * Waits at most {@code millis} milliseconds plus
     * {@code nanos} nanoseconds for this thread to die.
     * If both arguments are {@code 0}, it means to wait forever.
     *
     * <p> This implementation uses a loop of {@code this.wait} calls
     * conditioned on {@code this.isAlive}. As a thread terminates the
     * {@code this.notifyAll} method is invoked. It is recommended that
     * applications not use {@code wait}, {@code notify}, or
     * {@code notifyAll} on {@code Thread} instances.
     *
     * @param  millis
     *         the time to wait in milliseconds
     *
     * @param  nanos
     *         {@code 0-999999} additional nanoseconds to wait
     *
     * @throws  IllegalArgumentException
     *          if the value of {@code millis} is negative, or the value
     *          of {@code nanos} is not in the range {@code 0-999999}
     *
     * @throws  InterruptedException
     *          if any thread has interrupted the current thread. The
     *          <i>interrupted status</i> of the current thread is
     *          cleared when this exception is thrown.
     */
    public final synchronized void join(long millis, int nanos)
    throws InterruptedException {

        if (millis < 0) {
            throw new IllegalArgumentException("timeout value is negative");
        }

        if (nanos < 0 || nanos > 999999) {
            throw new IllegalArgumentException(
                                "nanosecond timeout value out of range");
        }

        if (nanos >= 500000 || (nanos != 0 && millis == 0)) {
            millis++;
        }

        join(millis);
    }

    /**
     * Waits for this thread to die.
     *
     * <p> An invocation of this method behaves in exactly the same
     * way as the invocation
     *
     * <blockquote>
     * {@linkplain #join(long) join}{@code (0)}
     * </blockquote>
     *
     * @throws  InterruptedException
     *          if any thread has interrupted the current thread. The
     *          <i>interrupted status</i> of the current thread is
     *          cleared when this exception is thrown.
     */
    public final void join() throws InterruptedException {
        join(0);
    }
```

重点关注一下 join(long millis) 方法的实现，可以看出 join 方法就是通过 wait 方法来将线程的阻塞，如果 join 的线程还在执行，则将当前线程阻塞起来，直到 join 的线程执行完成，当前线程才能执行。不过有一点需要注意，这里的 join 只调用了 wait 方法，却没有对应的 notify 方法，原因是 Thread 的 start 方法中做了相应的处理，所以当 join 的线程执行完成以后，会自动唤醒主线程继续往下执行。

```Java
public class SleepWaitTest {

    public static void main(String[] args) throws InterruptedException {
        SleepWaitTest sleepWaitTest = new SleepWaitTest();
        System.out.println("--- test join ---");
        Stream.generate(() -> new Thread(() -> {
	        System.out.println(Thread.currentThread().getName() + " start!");
	        try {
	            Thread.sleep(1000);
	        } catch (InterruptedException e) {
	            e.printStackTrace();
	        }
	        System.out.println(Thread.currentThread().getName() + " end!");
        })).limit(3).collect(Collectors.toList()).forEach(thread -> {
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

}
```

输出如下，可以看到我们在主线程里创建了三个子线程然后分别启动，并且线程启动后直接调用了 join。由上面的源码我们知道对子线程调用 join 会导致调用线程在子线程没完成的情况下进入到 wait 状态，直到子线程执行结束自动唤醒调用线程继续执行。所以这段代码三个子线程会顺序执行。

join 方法在多线程使用很多，总之只要记住一句话即可：join 表示等待此线程执行结束！Waits for this thread to die. 另外以下几个特性也很重要需要注意    

- When we invoke the join() method on a thread, the calling thread goes into a waiting state. It remains in a waiting state until the referenced thread terminates.

- The join() method may also return if the referenced thread was interrupted.

- if the referenced thread was already terminated or hasn't been started, the call to join() method returns immediately.

- when a thread t1 calls t2.join(), then all changes done by t2 are visible in t1 on return




```
--- test join ---
Thread-0 start!
Thread-0 end!
Thread-1 start!
Thread-1 end!
Thread-2 start!
Thread-2 end!
```


## 参考

- [The Thread.join() Method in Java](https://www.baeldung.com/java-thread-join)

- [Java 并发编程：线程间的协作(wait/notify/sleep/yield/join)](https://www.cnblogs.com/paddix/p/5381958.html)