# ForkJoinPool
---

前面介绍了线程池的基本知识，但都是基于 ThreadPoolExecutor 不同参数配置得到。JDK7 开始还提供了一种特殊的线程池 ForkJoinPool，下图可知它和 ThreadPoolExecutor 都继承自 AbstractExecutorService，看上去是提供了一种完全不同类型的线程池，那么它有啥特别的呢？

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/ebf292ae-bd76-40b0-97cd-da5814f45301)

## 分治思想

分治，顾名思义，即分而治之，是一种解决复杂问题的思维方法和模式；具体来讲，指的是把一个复杂的问题分解成多个相似的子问题，然后再把子问题分解成更小的子问题，直到子问题简单到可以直接求解。

分治思想（英文叫 “Divide and conquer”）在很多领域都有广泛的应用，例如算法领域有分治算法（归并排序、快速排序都属于分治算法，二分法查找也是一种分治算法）；大数据领域知名的计算框架 MapReduce 背后的思想也是分治。既然分治这种任务模型如此普遍，那 Java 显然也需要支持，Java 并发包里提供了一种叫做 Fork/Join 的并行计算框架，就是用来支持分治这种任务模型的。

分治任务模型可分为两个阶段：一个阶段是任务分解，也就是将任务迭代地分解为子任务，直至子任务可以直接计算出结果；另一个阶段是结果合并，即逐层合并子任务的执行结果，直至获得最终结果。

在这个分治任务模型里，任务和分解后的子任务具有相似性，这种相似性往往体现在任务和子任务的算法是相同的，但是计算的数据规模是不同的。具备这种相似性的问题，我们往往都采用**递归算法**。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/b48a5fcb-2ff5-494f-89c6-af09661116c9)

## Fork/Join 框架

Fork/Join 是一个并行计算的框架，主要就是用来支持分治任务模型的，这个计算框架里的Fork 对应的是分治任务模型里的任务分解，Join 对应的是结果合并。Fork/Join 计算框架主要包含两部分，一部分是分治任务的线程池 ForkJoinPool，另一部分是分治任务 ForkJoinTask。这两部分的关系类似于ThreadPoolExecutor 和 Runnable 的关系，都可以理解为提交任务到线程池，只不过分治任务有自己独特类型 ForkJoinTask。

ForkJoinTask 是一个抽象类，它的方法有很多，最核心的是 fork() 方法和 join() 方法，其中 fork() 方法会异步地执行一个子任务，而 join() 方法则会阻塞当前线程来等待子任务的执行结果。ForkJoinTask 有两个子类 —— RecursiveAction 和 RecursiveTask，通过名字你就应该能知道，它们都是用递归的方式来处理分治任务的。这两个子类都定义了抽象方法 compute()，不过区别是 RecursiveAction 定义的 compute() 没有返回值，而 RecursiveTask 定义的 compute() 方法是有返回值的。这两个子类也是抽象类，在使用的时候，需要你定义子类去扩展。

## ForkJoinPool 工作原理

Fork/Join 并行计算的核心组件是 ForkJoinPool，所以下面我们就来简单介绍一下 ForkJoinPool 的工作原理。

我们知道 ThreadPoolExecutor 本质上是一个生产者-消费者模式的实现，内部有一个任务队列，这个任务队列是生产者和消费者通信的媒介；ThreadPoolExecutor 可以有多个工作线程，但是这些工作线程都共享一个任务队列。

ForkJoinPool 本质上也是一个生产者-消费者的实现，但是更加智能，你可以参考下面的ForkJoinPool 工作原理图来理解其原理。ThreadPoolExecutor 内部只有一个任务队列，而 ForkJoinPool 内部有多个任务队列，当我们通过 ForkJoinPool 的 invoke() 或者 submit() 方法提交任务时，ForkJoinPool 根据一定的路由规则把任务提交到一个任务队列中，如果任务在执行过程中会创建出子任务，那么子任务会提交到工作线程对应的任务队列中。

如果工作线程对应的任务队列空了，是不是就没活儿干了呢？不是的，ForkJoinPool 支持一种叫做“任务窃取”（Work Stealing Algorithm）的机制，如果工作线程空闲了，那它可以“窃取”其他工作任务队列里的任务，例如下图中，线程T2对应的任务队列已经空了，它可以“窃取”线程T1对应的任务队列的任务。如此一来，所有的工作线程都不会闲下来了。

ForkJoinPool 中的任务队列采用的是双端队列，工作线程正常获取任务和“窃取任务”分别是从任务队列不同的端消费，这样能避免很多不必要的数据竞争。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/b2bc7201-5fb3-42f3-b6fa-65a53ea2e27c)


## Java's Fork/Join vs ExecutorService

其实根据前面的介绍，我们差不多知道 ForkJoinPool 只适用于特定的场景（可并行纯计算类型），在下面的这个问答 [Java's Fork/Join vs ExecutorService - when to use which?](https://stackoverflow.com/questions/21156599/javas-fork-join-vs-executorservice-when-to-use-which) 里有一句感觉描述的比较到位

> In practice ExecutorService is usually used to process many independent requests (aka transaction) concurrently, and fork-join when you want to accelerate one coherent job.

另外 Java8 里 Executors 新增了一个 newWorkStealingPool 方法用于创建一个 work-stealing thread pool，看源码其实就是创建了一个 ForkJoinPool。关于其代码解读可以参考这篇文章 [Diving Into Java 8's newWorkStealingPools](https://dzone.com/articles/diving-into-java-8s-newworkstealingpools)

```Java
public static ExecutorService newWorkStealingPool() {
    return new ForkJoinPool
        (Runtime.getRuntime().availableProcessors(),
         ForkJoinPool.defaultForkJoinWorkerThreadFactory,
         null, true);
}
```

## 总结

Fork/Join 并行计算框架的核心组件是 ForkJoinPool。ForkJoinPool 支持任务窃取机制，能够让所有线程的工作量基本均衡，不会出现有的线程很忙，而有的线程很闲的状况，所以性能很好。Java 1.8 提供的 Stream API 里面并行流也是以 ForkJoinPool 为基础的。不过需要你注意的是，默认情况下所有的并行流计算都共享一个 ForkJoinPool，这个共享的ForkJoinPool 默认的线程数是 CPU 的核数；如果所有的并行流计算都是 CPU 密集型计算的话，完全没有问题，但是如果存在 I/O 密集型的并行流计算，那么很可能会因为一个很慢的 I/O 计算而拖慢整个系统的性能。所以建议用不同的 ForkJoinPool 执行不同类型的计算任务。

1. ForkJoinPool 适合于“分而治之”算法的实现；
2. ForkJoinPool 和 ThreadPoolExecutor 是互补的，不是谁替代谁的关系，二者适用的场景不同；
3. ForkJoinTask 有两个核心方法——fork() 和 join()，有三个重要子类——RecursiveAction、RecursiveTask 和 CountedCompleter；
4. ForkjoinPool 内部基于“工作窃取”算法实现；
5. 每个线程有自己的工作队列，它是一个双端队列，自己从队列头存取任务，其它线程从尾部窃取任务；
6. ForkJoinPool 最适合于计算密集型任务，但也可以使用 ManagedBlocker 以便用于阻塞型任
