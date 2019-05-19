# Java 创建线程的几种方式
---

上一篇我们了解到线程和进程一样分为五个阶段：创建、就绪、运行、阻塞、终止。本篇介绍一下在 Java 中如何创建线程。创建线程主要有以下四种方式：

1. 继承 Thread 类
2. 实现 Runnable 接口
3. 实现 Callable 接口结合 FutureTask 创建带执行结果的线程
4. 线程池，利用线程池 ExecutorService、Callable、Future 来实现


## 继承 Thread 类     

```java
public class ThreadTest extends Thread {
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        ThreadTest threadTest = new ThreadTest();
        threadTest.start();
    }
}
```

## 实现 Runnable 接口  

```java
public class RunnableTest implements Runnable{
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
    }
    public static void main(String[] args) {
        RunnableTest target = new RunnableTest();
        Thread t1 = new Thread(target, "t1");
        Thread t2 = new Thread(target, "t2");
        t1.start();
        t2.start();
    }
}
```

- 

细心的你会发现 Thread 其实实现了 Runnable 接口，也就是说实现了其中的 run 方法，而这个 **run 方法即线程要执行的方法体**。从下面 Thread 源码的 run 方法实现来看，如果 Thread 类是通过 Runnable 对象来构造的，则会执行 Runnable 对象中实现的 run 方法；如果是 Thread 子类则会直接重写这个 run 方法（Subclasses of Thread should override this method.）。

值得注意的是，程序创建的 Runnable 对象只是线程的 target(线程执行体)，而多个线程可以共享同一个 target，所以多个线程可以共享同一个线程类的实例变量。

Thread 类部分源码：

```java
public class Thread implements Runnable {
	
    //...

	private int priority;
	/* What will be run. */
    private Runnable target;
    /* Whether or not the thread is a daemon thread. */
    private boolean daemon = false;

    /**
     * If this thread was constructed using a separate
     * <code>Runnable</code> run object, then that
     * <code>Runnable</code> object's <code>run</code> method is called;
     * otherwise, this method does nothing and returns.
     * <p>
     * Subclasses of <code>Thread</code> should override this method.
     *
     * @see     #start()
     * @see     #stop()
     * @see     #Thread(ThreadGroup, Runnable, String)
     */
	@Override
	public void run() {
	    if (target != null) {
	        target.run();
	    }
	}

    /**
     *
     * @param  target
     *         the object whose {@code run} method is invoked when this thread
     *         is started. If {@code null}, this thread's run method is invoked.
     * @param  name
     *         the name of the new thread
     */
     public Thread(Runnable target, String name) {
         init(null, target, name, 0);
     }

    //...

}
```

## Callable & Future

关于 Callable 和 Runnable 的对比，Java Doc 中注释写的很清楚，两者都是函数式接口，最大的区别是 Callable 可以获取线程执行结果，另外 Callable 中定义的执行体方法名是 call 而 Runnable 中是 run。

>  The {@code Callable} interface is similar to {@link
 java.lang.Runnable}, in that both are designed for classes whose
 instances are potentially executed by another thread.  A
 {@code Runnable}, however, does not return a result and cannot
 throw a checked exception.

但是注意 Callable 接口不能直接作为 Thread 的 target 来构建线程，从上面 Thread 类源码可以看到 target 应是一个 Runnable 对象。JDK5 定义了一个叫做 Future<V> 的接口来表示线程执行结果，里面定义了 cancel（取消运行）、isCancelled、get（获取线程执行结果）等一些方法。然后还提供了一个 RunnableFuture<V> 的接口同时继承了 Runnable、Future<V>。


```java
/**
 * A {@link Future} that is {@link Runnable}. Successful execution of
 * the {@code run} method causes completion of the {@code Future}
 * and allows access to its results.
 * @see FutureTask
 * @see Executor
 * @since 1.6
 * @author Doug Lea
 * @param <V> The result type returned by this Future's {@code get} method
 */
public interface RunnableFuture<V> extends Runnable, Future<V> {
    /**
     * Sets this Future to the result of its computation
     * unless it has been cancelled.
     */
    void run();
}
```

于是我们可以用 RunnableFuture<V> 这个接口的实现作为 target 去构建一个线程，并且能够得到其运行结果。当然肯定不需要自己去实现，那样太麻烦了，JDK 为我们提供了一个默认实现 `FutureTask<V>`，这个类实现了 Runnable 接口中定义的 run 方法，也实现了 Future 中定义的获取线程结果等一些方法，它有一个 Callable 的成员变量，有两个带参构造方法，我们需要用一个 Callable 对象去构造 FutureTask 对象，也可以用一个 Runnable 对象去构造 FutureTask，但其在内部会将 Runnable 对象转为 Callable 对象。而其 run 方法的实现中则是调用了 Callable 成员变量的 call 方法来执行线程逻辑。下面看看其部分源码：

```java
public class FutureTask<V> implements RunnableFuture<V> {

    private Callable<V> callable;

    public FutureTask(Callable<V> callable) {
        if (callable == null)
            throw new NullPointerException();
        this.callable = callable;
        this.state = NEW;       // ensure visibility of callable
    }

    public FutureTask(Runnable runnable, V result) {
        this.callable = Executors.callable(runnable, result); // 转为callable类型
        this.state = NEW;       // ensure visibility of callable
    }

    public V get() throws InterruptedException, ExecutionException {
        int s = state;
        if (s <= COMPLETING)
            s = awaitDone(false, 0L);
        return report(s);
    }

    public void run() {
        if (state != NEW ||
            !UNSAFE.compareAndSwapObject(this, runnerOffset,
                                         null, Thread.currentThread()))
            return;
        try {
            Callable<V> c = callable;
            if (c != null && state == NEW) {
                V result;
                boolean ran;
                try {
                    result = c.call(); //调用callable的call方法
                    ran = true;
                } catch (Throwable ex) {
                    result = null;
                    ran = false;
                    setException(ex);
                }
                if (ran)
                    set(result);
            }
        } finally {
            // runner must be non-null until state is settled to
            // prevent concurrent calls to run()
            runner = null;
            // state must be re-read after nulling runner to prevent
            // leaked interrupts
            int s = state;
            if (s >= INTERRUPTING)
                handlePossibleCancellationInterrupt(s);
        }
    }

}
```

看完源码明白了原理之后我们再来看一个 demo 就很容易理解了

- 首先定义一个 Callable 对象实现 call 方法（线程的主逻辑）
- 然后使用 Callable 对象构建一个 FutureTask 对象
- 再用 FutureTask 对象构建一个 Thread 对象并调用其 start 方法启动线程
- 最后调用 FutureTask 的 get 方法阻塞获取线程执行结果


```java
public class CallableTest implements Callable<Integer> {
    @Override
    public Integer call() {
        int sum = 0;
        System.out.println(Thread.currentThread().getName() + " 开始执行!");
        for (int i = 0; i <= 5; i++) {
            sum += i;
        }
        System.out.println(Thread.currentThread().getName() + " 执行完毕! sum=" + sum);
        return sum;
    }
    public static void main(String[] args) {
        CallableTest callableTest = new CallableTest();
        FutureTask<Integer> futureTask = new FutureTask<>(callableTest);
        Thread thread = new Thread(futureTask, "t1");
        thread.start();
        try {
            System.out.println(futureTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
```

由于 Runnable、Callable 接口都是函数式接口，因此上面的 demo 也可用 lambda 表达式改造如下

```java
public class CallableLambdaTest {
    public static void main(String[] args) {
        FutureTask<Integer> futureTask = new FutureTask<>(() -> {
            int sum = 0;
            for (int i = 0; i <= 5; i++) {
                sum += i;
            }
            return sum;
        });
        new Thread(futureTask, "t1").start();
        try {
            System.out.println(futureTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
```

或者

```java
public class CallableLambdaTest {
    public static void main(String[] args) {
        FutureTask<Integer> futureTask = new FutureTask<>(CallableLambdaTest::call);
        new Thread(futureTask, "t1").start();
        try {
            System.out.println("thread:" + Thread.currentThread().getName() + ", result:" + futureTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

    }
    private static Integer call() {
        System.out.println("thread: " + Thread.currentThread().getName() + " start!");
        int sum = 0;
        for (int i = 0; i <= 5; i++) {
            sum += i;
        }
        return sum;
    }
}
```

一般推荐使用 Runnable 和 Callable 接口的方式创建多线程，这样线程类还可以继承其他类，多个线程可以共享一个 target 对象，非常适合多个相同线程来处理同一份资源的情况，从而可以将CPU、代码和数据分开，体现了面向对象的编程思想。


## 线程池

线程池一般实现逻辑如下，这里先简单看一个示例

- 创建线程池（可以利用 JDK 的 Executors 的工厂方法）
- 创建 Callable 或 Runnable 任务，提交到线程池
- 通过返回的 Future#get 获取返回的结果


```java
public class ThreadPoolTest {
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; i++) {
            executorService.submit(() -> {
                System.out.println(Thread.currentThread().getName());
            });
        }
        executorService.shutdown();
    }
}
```

## 总结

实际应用中一般都是使用线程池来实现和管理多线程，主要是以下几点原因

- 线程是稀缺资源，不能频繁的创建。
- 解耦作用；线程的创建与执行完全分开，方便维护。
- 应当将其放入一个池子中，可以给其他任务进行复用。

阿里 Java 开发手册也有以下内容，这足以说明线程池在应用开发中重要作用，当然学习其它更底层更基础的方式有利于我们充分的理解多线程。

![](https://jverson.oss-cn-beijing.aliyuncs.com/5d77f4086ac4de6ac250b333188a1d91.jpg)

既然线程池这么重要，关于线程池的原理及使用包括在 Spring Boot 下的使用我将在下一篇单独详细讲解。


## 参考

- [Java创建线程的三种方式以及优劣对比](https://zhuanlan.zhihu.com/p/48415513)


