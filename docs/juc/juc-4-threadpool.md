# Executor 框架与线程池
---

上一篇已经提到，我们需要尽量避免在应用中显式的创建线程，而是使用线程池来提供和管理线程资源，这样可以避免频繁的线程创建导致的资源浪费和性能损耗。总结一下使用线程池的好处：

- 降低资源消耗。通过重复利用已创建的线程降低线程创建和销毁造成的消耗
- 提高响应速度。当任务到达时，任务可以不需要的等到线程创建就能立即执行
- 提高线程的可管理性。线程是稀缺资源，如果无限制的创建，不仅会消耗系统资源，还会降低系统的稳定性，使用线程池可以进行统一的分配，调优和监控。

最常见的创建线程池的的方式如下：

- Executors.newCachedThreadPool()：无限线程池。
- Executors.newFixedThreadPool(nThreads)：创建固定大小的线程池。
- Executors.newSingleThreadExecutor()：创建单个线程的线程池。

随便查看 newSingleThreadExecutor 的源码如下可以看到其实是通过 ThreadPoolExecutor 构建，而 ThreadPoolExecutor 则实现了 ExecutorService 接口，ExecutorService 接口则继承了 Executor 接口。换句话说 Java 中的线程池是通过 Executor 框架来实现。下面我们就来详细研究一下这个框架。

```java
public static ExecutorService newSingleThreadExecutor() {
    return new FinalizableDelegatedExecutorService
        (new ThreadPoolExecutor(1, 1,
                                0L, TimeUnit.MILLISECONDS,
                                new LinkedBlockingQueue<Runnable>()));
}
```

## Java 异步执行框架 Eexecutor

先来看一下 JDK8 juc 包下的线程池类图：

![](https://jverson.oss-cn-beijing.aliyuncs.com/dcbb52314fdbb37bca326fda4bab6b16.jpg)

可以看到核心接口即 Executor、ExecutorService、ScheduledExecutorService 这三个。

### Executor

Executor 接口是 Java5 中引入的，在 java.util.cocurrent 包下，通过该框架来控制线程的启动、执行和关闭，可以简化并发编程的操作。

```Java
public interface Executor {
    void execute(Runnable command);
}
```

Executor 接口中只定义了一个方法 execute（Runnable command），该方法用来执行一个提交的 Runable 任务，任务即一个实现了 Runnable 接口的类。这个接口有两个作用

- 提供一种将”任务提交”与”任务如何运行及线程如何使用和调度”分离开来的机制
- 避免我们为每个任务这样显示的创建线程： new Thread(new(RunnableTask())).start()

### ExecutorService

但是 JDK 并没有提供 Executor 的直接实现供用户使用，而是使用 ExecutorService 接口扩展（继承）了一下 Executor 接口，提供了更丰富的实现多线程的方法，比如增加了 shutDown()，shutDownNow()，invokeAll()，invokeAny() 和 submit() 等方法。

**因此我们一般用 ExecutorService 接口来实现和管理多线程。**


### ScheduledExecutorService

ScheduledExecutorService 扩展了 ExecutorService 接口，增加了 schedule、scheduleAtFixedRate 及 scheduleWithFixedDelay 方法。使用这些方法可以在指定的延时后执行一个 Runnable 或者 Callable 任务，也可以按照指定时间间隔定期执行任务。

下面是使用 ScheduledExecutorService 接口的一个示例，beepForAnHour 方法可以实现在一小时内每隔 10 秒打印一下 “beep”。

```Java
import static java.util.concurrent.TimeUnit.*;
class BeeperControl {
    private final ScheduledExecutorService scheduler =
      Executors.newScheduledThreadPool(1);

    public void beepForAnHour() {
      final Runnable beeper = () -> System.out.println("beep");
      final ScheduledFuture<?> beeperHandle = scheduler.scheduleAtFixedRate(beeper, 10, 10, SECONDS);
      scheduler.schedule(() -> beeperHandle.cancel(true), 60 * 60, TimeUnit.SECONDS);
    }
}
```

## ThreadPoolExecutor 原理

本篇开始的地方介绍了 Executors 中的线程池工厂方法都是通过 ThreadPoolExecutor 类构建不同类型的线程池，因此重点看一下这个类的原理的源码。

```Java
/**
 * An ExecutorService that executes each submitted task using
 * one of possibly several pooled threads, normally configured
 * using  Executors factory methods.
 **/
public class ThreadPoolExecutor extends AbstractExecutorService {
	//...
	/**
     * Creates a new {@code ThreadPoolExecutor} with the given initial
     * parameters and default thread factory.
     *
     * @param corePoolSize the number of threads to keep in the pool, even
     *        if they are idle, unless {@code allowCoreThreadTimeOut} is set
     * @param maximumPoolSize the maximum number of threads to allow in the
     *        pool
     * @param keepAliveTime when the number of threads is greater than
     *        the core, this is the maximum time that excess idle threads
     *        will wait for new tasks before terminating.
     * @param unit the time unit for the {@code keepAliveTime} argument
     * @param workQueue the queue to use for holding tasks before they are
     *        executed.  This queue will hold only the {@code Runnable}
     *        tasks submitted by the {@code execute} method.
     * @param handler the handler to use when execution is blocked
     *        because the thread bounds and queue capacities are reached
     * @throws IllegalArgumentException if one of the following holds:<br>
     *         {@code corePoolSize < 0}<br>
     *         {@code keepAliveTime < 0}<br>
     *         {@code maximumPoolSize <= 0}<br>
     *         {@code maximumPoolSize < corePoolSize}
     * @throws NullPointerException if {@code workQueue}
     *         or {@code handler} is null
     */
    public ThreadPoolExecutor(int corePoolSize,
                              int maximumPoolSize,
                              long keepAliveTime,
                              TimeUnit unit,
                              BlockingQueue<Runnable> workQueue,
                              RejectedExecutionHandler handler) {
        this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
             Executors.defaultThreadFactory(), handler);
    }
    //...
}

//Provides default implementations of {@link ExecutorService} execution methods.
public abstract class AbstractExecutorService implements ExecutorService {
	//...
}
```

可以看到 JDK 首先提供了一个 ExecutorService 接口的默认实现抽象类 AbstractExecutorService，而 ThreadPoolExecutor 则继承默认实现使用线程池的方式重写了任务执行的方式。

通过上面 ThreadPoolExecutor 的构造方法源码可以看到其中几个核心的参数，详细介绍如下：

- corePoolSize 线程池的核心线程数，当线程空闲也不会回收的线程数量，当有新任务在execute()方法提交时，会执行以下判断：
  1. 如果运行的线程少于 corePoolSize，则创建新线程来处理任务，即使线程池中的其他线程是空闲的；
  2. 如果线程池中的线程数量大于等于 corePoolSize 且小于 maximumPoolSize，则只有当 workQueue 满时才创建新的线程去处理任务；
  3. 如果设置的 corePoolSize 和 maximumPoolSize 相同，则创建的线程池的大小是固定的，这时如果有新任务提交，若 workQueue 未满，则将请求放入 workQueue 中，等待有空闲的线程去从 workQueue 中取任务并处理；
  4. 如果运行的线程数量大于等于 maximumPoolSize，这时如果 workQueue 已经满了，则通过 handler 所指定的策略来处理任务；

- maximumPoolSize 线程池最大线程个数
- keepAliveTime 和 unit 则是超过核心线程数的线程空闲后的存活时间
- workQueue 用于存放任务的阻塞队列
- handler 当队列和最大线程池都满了之后的饱和策略，它是 RejectedExecutionHandler 类型的变量。如果阻塞队列满了并且没有空闲的线程，这时如果继续提交任务，就需要采取一种策略处理该任务。线程池提供了4种策略：
  1. AbortPolicy：直接抛出异常，这是默认策略；
  2. CallerRunsPolicy：用调用者所在的线程来执行任务；
  3. DiscardOldestPolicy：丢弃阻塞队列中靠最前的任务，并执行当前任务；
  4. DiscardPolicy：直接丢弃任务；

再来看看最核心的 excute 方法的实现逻辑：

```Java
public void execute(Runnable command) {
        if (command == null)
            throw new NullPointerException();
        /*
         * Proceed in 3 steps:
         *
         * 1. If fewer than corePoolSize threads are running, try to
         * start a new thread with the given command as its first
         * task.  The call to addWorker atomically checks runState and
         * workerCount, and so prevents false alarms that would add
         * threads when it shouldn't, by returning false.
         *
         * 2. If a task can be successfully queued, then we still need
         * to double-check whether we should have added a thread
         * (because existing ones died since last checking) or that
         * the pool shut down since entry into this method. So we
         * recheck state and if necessary roll back the enqueuing if
         * stopped, or start a new thread if there are none.
         *
         * 3. If we cannot queue task, then we try to add a new
         * thread.  If it fails, we know we are shut down or saturated
         * and so reject the task.
         */
        int c = ctl.get();
        if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            if (! isRunning(recheck) && remove(command))
                reject(command);
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        else if (!addWorker(command, false))
            reject(command);
    }
```

代码逻辑转化为流程图如下

![](https://jverson.oss-cn-beijing.aliyuncs.com/48c3c04d3c00aae6e9c145153451ab2a.jpg)

注意：在向线程池提交任务时，除了 execute 方法，还可以使用 submit 方法提交一个 Callable 实例，submit 方法会返回一个 Future 对象用于获取返回值。

另外类中定义了线程池的五种运行状态，这几种状态的转化关系如下图所示

![](https://jverson.oss-cn-beijing.aliyuncs.com/8ee3f193f22feb11419425a96b87b265.jpg)


## 如何配置线程池

注意线程池肯定不是越大越好，首先大量线程会占用可观的系统资源，频繁的线程上下文切换对 CPU 来说也是很大的消耗，一般的线程配置可以有以下一些参考准则：

- IO 密集型任务：由于线程并不是一直在运行，所以可以尽可能的多配置线程，比如 CPU 个数 * 2
- CPU 密集型任务（大量复杂的运算）应当分配较少的线程，比如 CPU 个数相当的大小


## Springboot 中使用线程池

在 Springboot 中一般是结合异步 @Async 注解及 Spring 的 TaskExecutor 来实现。首先在 Spring 中可以使用 @async 注解实现方法的异步调用，其原理就是动态代理标有 @async 注解的类，将原来的普通方法封装成一个 Runnable 或者 Callable 类型放在一个队列里等待线程池异步的领取任务执行。另外 @Async 注解允许我们指定其线程池。因此在 Springboot 中使用线程池变得很简单：

1. 定义线程池    
```Java
@Bean("synEsAsyncExecutor")
public TaskExecutor getSynEsAsyncExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(corePoolSize);// 设置核心线程数
    executor.setMaxPoolSize(maxPoolSize);// 设置最大线程数
    executor.setQueueCapacity(queueCapacity);// 设置队列容量
    executor.setKeepAliveSeconds(keepAliveSeconds);// 设置线程活跃时间（秒）
    executor.setThreadNamePrefix("task-synEs-");
    executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
    executor.initialize();
    return executor;
}
```

2. 开启 Spring 对异步的支持    
```Java
@Configuration
@EnableAsync
public class ThreadPoolConfig implements AsyncConfigurer{
    //...
}
```

3. 使用线程池，只需要在 Async 注解上配上线程池实例名称即可    
```Java
@Component("commentService")
@Async("synEsAsyncExecutor")
public class CommentServiceImpl implements CommentService{
    @Override
    public void hbaseToEsForJobService(UpdateRecordVo updateRecordVo) throws Exception {
        hbaseToEsById(updateRecordVo.getCommentId());
    }
}
```

4. 异常处理，实现 AsyncConfigurer 接口的 getAsyncUncaughtExceptionHandler 方法，此 handler 将被用于处理异常

完整的代码示例如下，其中定义了两个线程池分别用于不同的异步任务确保线程池隔离，避免两个任务之间出现线程资源竞争。同时还实现了异步方法异常的统一处理。

```Java
@Configuration
@EnableAsync
public class ThreadPoolConfig implements AsyncConfigurer{
	private static final Logger LOGGER = LoggerFactory.getLogger(ThreadPoolConfig.class);

	@Value("${task-executor.core_pool_size}")
    private int corePoolSize;
    @Value("${task-executor.max_pool_size}")
    private int maxPoolSize;
    @Value("${task-executor.queue-capacity}")
    private int queueCapacity;
    @Value("${task-executor.keep-alive-seconds}")
    private int keepAliveSeconds;

	@Autowired
	private AsyncExceptionHandlerService exceptionHandler;

	/**
	 * 聚合任务线程池
	 * @return
	 */
	@Bean("aggAsyncExecutor")
    public TaskExecutor getAggAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);// 设置核心线程数
        executor.setMaxPoolSize(maxPoolSize);// 设置最大线程数
        executor.setQueueCapacity(queueCapacity);// 设置队列容量
        executor.setKeepAliveSeconds(keepAliveSeconds);// 设置线程活跃时间（秒）
        executor.setThreadNamePrefix("task-agg-");// 设置默认线程名称
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

	/**
	 * es同步任务线程池
	 * @return
	 */
	@Bean("synEsAsyncExecutor")
    public TaskExecutor getSynEsAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);// 设置核心线程数
        executor.setMaxPoolSize(maxPoolSize);// 设置最大线程数
        executor.setQueueCapacity(queueCapacity);// 设置队列容量
        executor.setKeepAliveSeconds(keepAliveSeconds);// 设置线程活跃时间（秒）
        executor.setThreadNamePrefix("task-synEs-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }

	@Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
         return new MyAsyncExceptionHandler(exceptionHandler);  
    }

	/**
	 * 异常任务统一处理
	 */
	class MyAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {
		private AsyncExceptionHandlerService exceptionHandler;
		public MyAsyncExceptionHandler(AsyncExceptionHandlerService exceptionHandler) {
			this.exceptionHandler = exceptionHandler;
		}
        @Override  
        public void handleUncaughtException(Throwable throwable, Method method, Object... obj) {  
        	// 打印异常日志，添加ump监控
        	String methodName = method.getName();
        	LOGGER.error("task: {} execute error for record: {}! errMsg: {}",
        			methodName, JSON.toJSONString(obj), throwable.getMessage());
        	exceptionHandler.handle(methodName, obj[0]);
        }  
    }

}
```

注意这里使用的线程池是 `org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor` 类，该类实现了 `org.springframework.core.task.TaskExecutor` 接口（TaskExecutor 继承了 `java.util.concurrent.Executor`），是 Spring 提供的一种可以使用 JavaConfig 形式配置线程池，并且方便管理和监控线程池的一个类。换句话说 SpringFrameWork 的 ThreadPoolTaskExecutor 是辅助 JDK 的 ThreadPoolExecutor 的工具类，它将属性通过 JavaBeans 的命名规则提供出来，方便进行配置。

更通俗的将 JDK 中都是基于 Executor 接口，而 Spring 中则都是基于 TaskExecutor，但其实 TaskExecutor 继承了 Executor 接口。Spring 的 TaskExecutor 的常用实现类基本都是是基于 Executor 实现类的包装，使其更加方便使用，更好的融入 spring bean 生态。

当然了我们也可以通过 Executors 的工厂方法来创建使用 JDK 的线程池，不过在 Spring 中还是使用 Spring 封装过的 executor 更方便一些。

## 共用线程池导致死锁（避坑）

死锁一般是两个或多个线程互相持有对方所需的资源会造成死锁，而有的场景下线程池使用不当也会引发死锁，下面我们来看看很常见的一种情况，多个任务共用一个线程池，而且每个任务内也用到该线程池，线程池的线程数量等于或小于任务数，也会造成死锁。

```Java
public class DemoTest {
    private static ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 5,
            30L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(20));

    public static void main(String[] args) throws InterruptedException {
        for (int i = 0; i < 10; i++) {
            CompletableFuture.supplyAsync(DemoTest::work, executor);
            System.out.printf("executor i=%s queue=%s", i, executor.getQueue().size());
            System.out.println();
        }
        // 检查状态，会发现queue一段时间后，一直稳定在一个值，即线程池不再执行新任务
        while (executor.getQueue().size() > 0) {
            System.out.printf("checkInterval executor queue.size=%s", executor.getQueue().size());
            System.out.printf(", activeCount=%s", executor.getActiveCount());
            System.out.printf(", completedTaskCount=%s", executor.getCompletedTaskCount());
            System.out.printf(", taskCount=%s", executor.getTaskCount());
            System.out.println();
            Thread.sleep(1000);
        }
    }

    // 模拟一个异步调用，但同步返回的任务
    public static int work() {
        try {
            System.out.println(String.format("work %s thread begin, queue=%s", Thread.currentThread().getName(), executor.getQueue().size()));
            Integer result = CompletableFuture.supplyAsync(DemoTest::workInnerTask, executor).get();
            System.out.println(String.format("work %s thread done!", Thread.currentThread().getName()));
            return result;
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
        return -1;
    }

    private static int workInnerTask() {
        try {
            System.out.println(String.format("workInnerTask %s thread begin, queue=%s", Thread.currentThread().getName(), executor.getQueue().size()));
            Thread.sleep(2000);
            System.out.println(String.format("workInnerTask %s thread done!", Thread.currentThread().getName()));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return ThreadLocalRandom.current().nextInt();
    }

}
```

该代码在运行一段时间后就会发生死锁，核心线程都处于 Waiting 状态，队列大小不再变化，也就是说线程池完全处于阻塞状态无法处理队列里以及新加入的任务。

输出如下

```
executor i=0 queue=0
work pool-1-thread-1 thread begin, queue=0
executor i=1 queue=0
work pool-1-thread-2 thread begin, queue=0
executor i=2 queue=0
work pool-1-thread-3 thread begin, queue=0
executor i=3 queue=2
workInnerTask pool-1-thread-4 thread begin, queue=2
work pool-1-thread-5 thread begin, queue=2
executor i=4 queue=3
executor i=5 queue=5
executor i=6 queue=6
executor i=7 queue=7
executor i=8 queue=8
executor i=9 queue=9
checkInterval executor queue.size=9, activeCount=5, completedTaskCount=0, taskCount=14
checkInterval executor queue.size=9, activeCount=5, completedTaskCount=0, taskCount=14
workInnerTask pool-1-thread-4 thread done!
work pool-1-thread-1 thread done!
workInnerTask pool-1-thread-4 thread begin, queue=8
workInnerTask pool-1-thread-1 thread begin, queue=7
checkInterval executor queue.size=7, activeCount=5, completedTaskCount=2, taskCount=14
checkInterval executor queue.size=7, activeCount=5, completedTaskCount=2, taskCount=14
workInnerTask pool-1-thread-1 thread done!
workInnerTask pool-1-thread-4 thread done!
work pool-1-thread-1 thread begin, queue=6
work pool-1-thread-2 thread done!
work pool-1-thread-3 thread done!
workInnerTask pool-1-thread-4 thread begin, queue=5
work pool-1-thread-3 thread begin, queue=4
work pool-1-thread-2 thread begin, queue=5
checkInterval executor queue.size=6, activeCount=5, completedTaskCount=6, taskCount=17
checkInterval executor queue.size=6, activeCount=5, completedTaskCount=6, taskCount=17
workInnerTask pool-1-thread-4 thread done!
work pool-1-thread-4 thread begin, queue=5
work pool-1-thread-5 thread done!
work pool-1-thread-5 thread begin, queue=5
checkInterval executor queue.size=6, activeCount=5, completedTaskCount=8, taskCount=19
checkInterval executor queue.size=6, activeCount=5, completedTaskCount=8, taskCount=19
checkInterval executor queue.size=6, activeCount=5, completedTaskCount=8, taskCount=19
checkInterval executor queue.size=6, activeCount=5, completedTaskCount=8, taskCount=19
checkInterval executor queue.size=6, activeCount=5, completedTaskCount=8, taskCount=19
checkInterval executor queue.size=6, activeCount=5, completedTaskCount=8, taskCount=19
checkInterval executor queue.size=6, activeCount=5, completedTaskCount=8, taskCount=19
checkInterval executor queue.size=6, activeCount=5, completedTaskCount=8, taskCount=19
checkInterval executor queue.size=6, activeCount=5, completedTaskCount=8, taskCount=19
checkInterval executor queue.size=6, activeCount=5, completedTaskCount=8, taskCount=19
... ...
```

这时候通过 jstack 看看当前的线程状态如下，可以看到线程池的核心线程都处于 Waiting 状态，阻塞到了 `Integer result = CompletableFuture.supplyAsync(DemoTest::workInnerTask, executor).get();` 这一行。这是因为所有的核心线程都被 work 任务占了，workInnerTask 只能提交到缓冲队列等待有空闲线程去执行，但是 work 任务又依赖其中 workInnerTask 执行完成才能释放占有的线程，这样就陷入了互相等待的死锁状态。

显然解决方法有几种，第一种不要复用线程池，work 和 workInnerTask 使用互相独立的线程池；第二种 workInnerTask 不要阻塞直接返回 CompletableFuture，即全链路异步；

```
"Attach Listener" #16 daemon prio=9 os_prio=31 tid=0x00007fba50836800 nid=0x5d03 waiting on condition [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None

"pool-1-thread-5" #15 prio=5 os_prio=31 tid=0x00007fba4b2ea800 nid=0xa303 waiting on condition [0x0000700002716000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x000000076be3fef0> (a java.util.concurrent.CompletableFuture$Signaller)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.CompletableFuture$Signaller.block(CompletableFuture.java:1693)
	at java.util.concurrent.ForkJoinPool.managedBlock(ForkJoinPool.java:3323)
	at java.util.concurrent.CompletableFuture.waitingGet(CompletableFuture.java:1729)
	at java.util.concurrent.CompletableFuture.get(CompletableFuture.java:1895)
	at com.jverson.DemoTest.work(DemoTest.java:34)
	at com.jverson.DemoTest$$Lambda$1/1837760739.get(Unknown Source)
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1590)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
	- <0x000000076b8d3ee8> (a java.util.concurrent.ThreadPoolExecutor$Worker)

"pool-1-thread-4" #14 prio=5 os_prio=31 tid=0x00007fba4a341800 nid=0xa403 waiting on condition [0x0000700002613000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x000000076bcfa0e8> (a java.util.concurrent.CompletableFuture$Signaller)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.CompletableFuture$Signaller.block(CompletableFuture.java:1693)
	at java.util.concurrent.ForkJoinPool.managedBlock(ForkJoinPool.java:3323)
	at java.util.concurrent.CompletableFuture.waitingGet(CompletableFuture.java:1729)
	at java.util.concurrent.CompletableFuture.get(CompletableFuture.java:1895)
	at com.jverson.DemoTest.work(DemoTest.java:34)
	at com.jverson.DemoTest$$Lambda$1/1837760739.get(Unknown Source)
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1590)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
	- <0x000000076b9233b8> (a java.util.concurrent.ThreadPoolExecutor$Worker)

"pool-1-thread-3" #13 prio=5 os_prio=31 tid=0x00007fba4819b800 nid=0x5b03 waiting on condition [0x0000700002510000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x000000076bbb0900> (a java.util.concurrent.CompletableFuture$Signaller)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.CompletableFuture$Signaller.block(CompletableFuture.java:1693)
	at java.util.concurrent.ForkJoinPool.managedBlock(ForkJoinPool.java:3323)
	at java.util.concurrent.CompletableFuture.waitingGet(CompletableFuture.java:1729)
	at java.util.concurrent.CompletableFuture.get(CompletableFuture.java:1895)
	at com.jverson.DemoTest.work(DemoTest.java:34)
	at com.jverson.DemoTest$$Lambda$1/1837760739.get(Unknown Source)
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1590)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
	- <0x000000076b8d3728> (a java.util.concurrent.ThreadPoolExecutor$Worker)

"pool-1-thread-2" #12 prio=5 os_prio=31 tid=0x00007fba499d8800 nid=0x5a03 waiting on condition [0x000070000240d000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x000000076ba6b960> (a java.util.concurrent.CompletableFuture$Signaller)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.CompletableFuture$Signaller.block(CompletableFuture.java:1693)
	at java.util.concurrent.ForkJoinPool.managedBlock(ForkJoinPool.java:3323)
	at java.util.concurrent.CompletableFuture.waitingGet(CompletableFuture.java:1729)
	at java.util.concurrent.CompletableFuture.get(CompletableFuture.java:1895)
	at com.jverson.DemoTest.work(DemoTest.java:34)
	at com.jverson.hotel.DemoTest$$Lambda$1/1837760739.get(Unknown Source)
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1590)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
	- <0x000000076b8d2f98> (a java.util.concurrent.ThreadPoolExecutor$Worker)

"pool-1-thread-1" #11 prio=5 os_prio=31 tid=0x00007fba481c0800 nid=0x5903 waiting on condition [0x000070000230a000]
   java.lang.Thread.State: WAITING (parking)
	at sun.misc.Unsafe.park(Native Method)
	- parking to wait for  <0x000000076b92bd10> (a java.util.concurrent.CompletableFuture$Signaller)
	at java.util.concurrent.locks.LockSupport.park(LockSupport.java:175)
	at java.util.concurrent.CompletableFuture$Signaller.block(CompletableFuture.java:1693)
	at java.util.concurrent.ForkJoinPool.managedBlock(ForkJoinPool.java:3323)
	at java.util.concurrent.CompletableFuture.waitingGet(CompletableFuture.java:1729)
	at java.util.concurrent.CompletableFuture.get(CompletableFuture.java:1895)
	at com.jverson.DemoTest.work(DemoTest.java:34)
	at com.jverson.DemoTest$$Lambda$1/1837760739.get(Unknown Source)
	at java.util.concurrent.CompletableFuture$AsyncSupply.run(CompletableFuture.java:1590)
	at java.util.concurrent.ThreadPoolExecutor.runWorker(ThreadPoolExecutor.java:1149)
	at java.util.concurrent.ThreadPoolExecutor$Worker.run(ThreadPoolExecutor.java:624)
	at java.lang.Thread.run(Thread.java:748)

   Locked ownable synchronizers:
	- <0x000000076b8d24e0> (a java.util.concurrent.ThreadPoolExecutor$Worker)

"Service Thread" #10 daemon prio=9 os_prio=31 tid=0x00007fba49791800 nid=0xa903 runnable [0x0000000000000000]
   java.lang.Thread.State: RUNNABLE

   Locked ownable synchronizers:
	- None
```

因此，多个异步处理共用同一个线程池的时候要小心避免这种情况！


## 总结

关于线程池就先整理这些知识点，另外还有一些细节问题后续再补充，比如线程池的监控（通过 ThreadPoolExecutor 的一些方法获取线程池的运行指标）、线程池与 ThreadLocal 同时使用的注意事项（线程服用导致线程变量互串、线程结束没有调用 remove 导致内存泄漏等等）。


## 参考

- [如何优雅的使用和理解线程池](https://segmentfault.com/a/1190000015808897)
- [深入理解 Java 线程池：ThreadPoolExecutor](https://juejin.im/entry/58fada5d570c350058d3aaad)
