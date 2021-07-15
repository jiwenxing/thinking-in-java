# ThreadLocal 介绍
---

## 使用举例

ThreadLocal 实例通常总是以静态字段初始化如下，为什么，看了下面的源码解读就知道了

`static ThreadLocal<User> threadLocalUser = new ThreadLocal<>();`

例如一个 web 服务，每个用户请求进来都是通过线程池去运行下面一个任务处理请求，我们可以通过一个 threadLocalUser 变量实现 user 参数在该线程整个生命周期的传递

```Java
void processUser(user) {
    try {
        threadLocalUser.set(user);
        step1();
        step2();
    } finally {
        threadLocalUser.remove();  // 线程结束的时候要记得 remove，要不然线程池会导致threadLocalUser 变量错乱
    }
}
```

## 源码解读

ThreadLocal 源码里有一个简单的例子，每个线程都可以通过一个 ThreadLocal 类型的 threadId 变量获取到一个唯一标识的 id。

```Java
public class ThreadLocalTest {
    private static final AtomicInteger nextId = new AtomicInteger(1);
    private static final ThreadLocal<Integer> threadId = ThreadLocal.withInitial(() -> nextId.getAndIncrement());
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> System.out.println(Thread.currentThread().getName() + ": " + threadId.get()));
        }
        executorService.shutdown();
    }
}
```

我们的直觉是如果要实现这样一个功能应该是在 ThreadLocal 里维护一个 thread 为 key 的 Map<thread, value> 结构。而 Java 里的这个实现可能和我们的直觉有所出入，ThreadLocal 仅仅是一个代理工具类，内部并不持有任何与线程相关的数据，所有和线程相关的数据都存储在 Thread 里面，这样的设计容易理解。而从数据的亲缘性上来讲，ThreadLocalMap 属于 Thread 也更加合理。

我们再来简单看一下源码，可以看到 ThreadLocalMap 本身就是 Thread 类的成员变量，也就是我们在某个线程里对 ThreadLoacal 变量进行操作的时候其实操作的是当前线程 Thread 实例的 ThreadLocal 变量，即 Thread.currentThread().threadLocals。每个Thread 维护一个 ThreadLocalMap 映射表，这个映射表的 key 是 ThreadLocal 实例本身，value 是真正需要存储的 Object。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/03d8d311-0d64-4c14-86a5-f66afa9f6755)

JDK 之所以这么设计还有一个更加深层次的原因，那就是不容易产生内存泄露。在我们的设计方案中，ThreadLocal 持有的 Map 会持有 Thread 对象的引用，这就意味着，只要ThreadLocal 对象存在，那么 Map 中的 Thread 对象就永远不会被回收。ThreadLocal 的生命周期往往都比线程要长，所以这种设计方案很容易导致内存泄露。而 Java 的实现中Thread 持有 ThreadLocalMap，而且 ThreadLocalMap 里对 ThreadLocal 的引用还是弱引用（WeakReference），所以只要 Thread 对象可以被回收，那么 ThreadLocalMap 就能被回收。Java 的这种实现方案虽然看上去复杂一些，但是更加安全。

从源码可以看到 ThreadLocalMap 结构类似于普通的 Map，其中的 Entry 结构对应的 key 是 threadlocal 变量封装的一个弱引用，value 即我们要设置的值，如果我们想在里面存储多个 kv 集合，就多创建几个 threadlocal 变量即可。

```Java
public void set(T value) {
        Thread t = Thread.currentThread(); // 当前线程
        ThreadLocalMap map = getMap(t); // 取得当前线程的 ThreadLocalMap 对象，其实就是 Thread 类的成员变量 threadLocals，换句话说每个线程的 ThreadLocalMap 其实就是保存在 Thread 的成员变量里
        if (map != null)
            map.set(this, value); // ThreadLocalMap 内部的 key 是 ThreadLocal 对象，value 就是要设置的值
        else
            createMap(t, value);
    }
    ThreadLocalMap getMap(Thread t) {
        return t.threadLocals;
    }
    void createMap(Thread t, T firstValue) {
        t.threadLocals = new ThreadLocalMap(this, firstValue); // 初始化当前 Thread 的 threadLocals 成员变量
    }

    static class ThreadLocalMap { // threadlocal 的内部类
        /**
         * The entries in this hash map extend WeakReference, using
         * its main ref field as the key (which is always a
         * ThreadLocal object).  Note that null keys (i.e. entry.get()
         * == null) mean that the key is no longer referenced, so the
         * entry can be expunged from table.  Such entries are referred to
         * as "stale entries" in the code that follows.
         */
        static class Entry extends WeakReference<ThreadLocal<?>> { // ThreadLocalMap 里保存的 Entry 对象定义
            /** The value associated with this ThreadLocal. */
            Object value;
            Entry(ThreadLocal<?> k, Object v) {
                super(k); // 会把 key 封装成一个弱引用
                value = v;
            }
        }
      //...
    }

    public T get() {
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t); // 获取当前 Thread 的 ThreadLocalMap 成员变量
        if (map != null) {
            ThreadLocalMap.Entry e = map.getEntry(this); // 以当前 threadLoacal 对象为 key 获取对应的 Entry（注意这里为啥不直接取 value？）
            if (e != null) {
                @SuppressWarnings("unchecked")
                T result = (T)e.value; // 取 Entry 中的 value
                return result;
            }
        }
        return setInitialValue();
    }

    private T setInitialValue() {
        T value = initialValue();
        Thread t = Thread.currentThread();
        ThreadLocalMap map = getMap(t);
        if (map != null)
            map.set(this, value);
        else
            createMap(t, value);
        return value;
    }
```

## ThreadLocal 与内存泄露

Java 的 ThreadLocal 实现应该称得上深思熟虑了，不过即便如此深思熟虑，还是不能百分百地让程序员避免内存泄露，例如在线程池中使用 ThreadLocal，如果不谨慎就可能导致内存泄露。

在线程池中使用 ThreadLocal 为什么可能导致内存泄露呢？原因就出在线程池中线程的存活时间太长，往往都是和程序同生共死的，这就意味着 Thread 持有的 ThreadLocalMap 一直都不会被回收，再加上 ThreadLocalMap 中的 Entry 对 ThreadLocal 是弱引用（WeakReference），所以只要 ThreadLocal 结束了自己的生命周期是可以被回收掉的。但是 Entry 中的 Value 却是被 Entry 强引用的，所以即便 Value 的生命周期结束了，Value 也是无法被回收的，从而导致内存泄露。

那在线程池中，我们该如何正确使用 ThreadLocal 呢？其实很简单，既然JVM不能做到自动释放对Value的强引用，那我们手动释放就可以了。就像示例代码中在 finally 里执行 ThreadLocal 的 remove 操作

## InheritableThreadLocal 与继承性

通过 ThreadLocal 创建的线程变量，其子线程是无法继承的。也就是说你在线程中通过ThreadLocal 创建了线程变量V，而后该线程创建了子线程，你在子线程中是无法通过ThreadLocal来访问父线程的线程变量V的。

如果你需要子线程继承父线程的线程变量，那该怎么办呢？其实很简单，Java提供了InheritableThreadLocal 来支持这种特性，InheritableThreadLocal 是 ThreadLocal子类，所以用法和 ThreadLocal 相同，这里就不多介绍了。

不过，我完全不建议你在线程池中使用 InheritableThreadLocal，不仅仅是因为它具有 ThreadLocal 相同的缺点——可能导致内存泄露，更重要的原因是：线程池中线程的创建是动态的，很容易导致继承关系错乱，如果你的业务逻辑依赖 InheritableThreadLocal，那么很可能导致业务逻辑计算错误，而这个错误往往比内存泄露更要命。如果项目中一定要用建议直接使用阿里开源的封装好的 [transmittable-thread-local](https://github.com/alibaba/transmittable-thread-local)

## 在线程池中使用导致上下文丢失问题（避坑）

如果我们初始化线程池的时候指定如果线程池满，则新提交的任务转为串行执行，即在调用线程中执行（ThreadPoolExecutor.CallerRunsPolicy），那我们之前的写法就会有问题了，串行执行结束的时候还在 finally 里调 remove(); 就会将主线程的上下文也清理，即使后面线程池继续并行工作，传给子线程的上下文也已经是null了，而且这样的问题很难在预发测试的时候发现。

## 并行流中线程上下文丢失问题（避坑）

如果 ThreadLocal 碰到并行流，也会有很多有意思的事情发生，并行流的设计比较特殊，因为并行流底层的实现也是一个 ForkJoin 线程池，父线程也有可能参与到并行流线程池的调度，那如果上面方法被父线程执行，那么父线程的上下文就会在 finally 里被清理。导致后续拷贝到子线程的上下文都为null，同样产生丢失上下文的问题。

```Java
dataList.parallelStream().forEach(entry -> {
            try {
                threadlocal.set(session);
                // 业务处理
                doIt();
            } catch (Exception e) {
                // log it
            } finally {
                threadlocal.remove();
            }
        });
```

## 总结

线程本地存储模式本质上是一种避免共享的方案，由于没有共享，所以自然也就没有并发问题。如果你需要在并发场景中使用一个线程不安全的工具类，最简单的方案就是避免共享。避免共享有两种方案，一种方案是将这个工具类作为局部变量使用，另外一种方案就是线程本地存储模式。这两种方案，局部变量方案的缺点是在高并发场景下会频繁创建对象，而线程本地存储方案，每个线程只需要创建一个工具类的实例，所以不存在频繁创建对象的问题。

线程本地存储模式是解决并发问题的常用方案，所以 Java SDK 也提供了相应的实现：ThreadLocal。通过上面我们的分析，你应该能体会到Java SDK的实现已经是深思熟虑了，不过即便如此，仍不能尽善尽美，例如在线程池中使用ThreadLocal仍可能导致内存泄漏，所以使用ThreadLocal还是需要你打起精神，足够谨慎。
