# RxJava 及响应式编程

---

# 介绍

这篇文章主要讲了 RxJava 在异步场景下和 Future、callback、compelableFuture 等的区别，然后介绍了 RxJava 的 API，最后通过具体的业务使用示例了 RxJava 在项目中的应用。

[Java 9's Reactive Streams](https://community.oracle.com/docs/DOC-1006738) aka [Flow API](https://docs.oracle.com/javase/9/docs/api/java/util/concurrent/Flow.html) are a set of Interfaces implemented by various [reactive streams](http://www.reactive-streams.org/) libraries such as [RxJava 2](https://github.com/ReactiveX/RxJava/wiki/Reactive-Streams), [Akka Streams](http://doc.akka.io/docs/akka-stream-and-http-experimental/1.0-M2/stream-design.html), and [Vertx](http://vertx.io/). They allow these reactive libraries to interconnect, while preserving the all important back-pressure. 

# 举个栗子

通过一个例子对比一下分别使用 future、callback、completableFuture、RxJava 不同方式来实现下面这个调用流程（或者说是一个 DAG 图）。

![](https://jverson.oss-cn-beijing.aliyuncs.com/de41b312bc6e5b9a1c9cb90c2e75997c.jpg)

## Future

![](https://jverson.oss-cn-beijing.aliyuncs.com/c5592395451fea35df8ab8e19521b505.jpg)

我们知道 Future.get() 本质还是阻塞的，并不能达到非阻塞的目的。 

## Callback

![](https://jverson.oss-cn-beijing.aliyuncs.com/fc202e3a3233a25cb36495e892a54ca9.jpg)

上面只是部分实现，callback 看上去可以实现完全非阻塞，但是代码就变成了层层嵌套的回调地狱，很丑难维护

## CompletableFuture

显然 CompletableFuture 很适合这种场景，利用其各种 then/when/combile 等接口可以容易组装成一个DAG执行拓扑实现纯异步执行。代码这里就不写了。

## RxJava

![](https://jverson.oss-cn-beijing.aliyuncs.com/0f45d04347d0cc15ab1244dface4444d.jpg)

再来看看 RxJava，和 CompletableFuture 的用法很类似，所有的任务都可以在调用方同一个层次上获取引用，并且可以随意组合、变换等，堪称信手拈来。就跟搭积木一样！

那么问题来了，通过以上对比，显然决赛选手是 RxJava 和 CompletableFuture ，那么他们有啥区别呢？



# RxJava

Reactive Extension for Java，是最开始根据微软的 [http://Rx.Net](http://rx.net/) 为基础，由 Netflix 主导做出的提供在 JVM 上实现 Reactive Programming 的一种方式。同类的库还有 Project Reactor、Akka 和 Google 的 Agera 等等。但是目前网上分享几乎都是 Android 端的使用，很少有后端使用案例？？

下面重点介绍一下 RxJava，RxJava 是 ReactiveX 家族的一份子，而 ReactiveX 致力于为反应式编程提供工具库。目前的 ReactiveX 家族已经很庞大了如下图所示。

![](https://jverson.oss-cn-beijing.aliyuncs.com/6643814d40e0e244c9c6fc15b5192d5a.jpg)

ReactiveX = 观察者 + 迭代器 + 函数式，这三个词很好地概括了 ReactiveX 的核心：函数式异步数据流 **Observable<T>**。

Observable可以认为是异步多值的数据结构，它与我们常见的Iterable类似，我们在日常的业务开发中，也更多地是将 Observable 当作增强版的 Future 来使用。

| **操作** | **Iterable(pull)** | **Observable(push)** |
| -------- | ------------------ | -------------------- |
| 查询     | T next()           | onNext(T)            |
| 错误     | throws Exception   | onError(Exception)   |
| 完成     | !hasNext()         | onCompleted          |

## Observable 生命周期

和 CompletableFuture 或者 Stream 类似都分为创建、组合/转换、消费三个环节

![](https://jverson.oss-cn-beijing.aliyuncs.com/5c7caf125b93803a01713076981bb603.jpg)

### 创建阶段 API

| 方式       | 示例                                                         | 说明                                                         |
| ---------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 现有数据   | Observable.just(100)Observable.from(Arrays.asList(1, 2, 3))Observable.from(future) | RxJava 2.x版本API会有变化这些方法用于将现有数据装箱为Observable类型，目的是为了后续的转换与组合 |
| 自定义逻辑 | Observable.create(...)                                       | 最佳实践：可以自己封装helper方法，将Observable<T>当成增强版的Future<T>使用![](https://jverson.oss-cn-beijing.aliyuncs.com/47e054d299498ceac11b13f17888f355.jpg) |

### 转换组合阶段API

| 模式     | 说明                                                         | 代码                                                         | 示意图                                                       |
| -------- | ------------------------------------------------------------ | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 依赖关系 | Observable.flatMap例如f3依赖于f1的结果                       | ![](https://jverson.oss-cn-beijing.aliyuncs.com/d87cd5778944ee0dd6bbe8b55ad2d858.jpg) | ![](https://jverson.oss-cn-beijing.aliyuncs.com/1570f2e201169bf4c105c9cb933d64aa.jpg) |
| 并行关系 | Observable.zip例如f3、f4、f5并行，并组成最终结果             | ![](https://jverson.oss-cn-beijing.aliyuncs.com/e279ac60e82e564307d1b20736473d09.jpg) | ![](https://jverson.oss-cn-beijing.aliyuncs.com/3330d9a6fcdc31a777f160ef0a8a85bf.jpg) |
| 分块处理 | 化大为小，分而治之Observable.from(list)    .buffer(n)    .flatMap(subList -> subResult)    .reduce((acc, subResult) -> result) | ![](https://jverson.oss-cn-beijing.aliyuncs.com/8edd463b2bb8fbc72f6ceab4cf7ccff4.jpg) | ![](https://jverson.oss-cn-beijing.aliyuncs.com/7f6ecada332ce66abea98bc6479c28cc.jpg) |

### 监听/消费 阶段API

| 方式       | 说明                                                         | 代码                                                         |
| ---------- | ------------------------------------------------------------ | ------------------------------------------------------------ |
| 非阻塞方式 | Observable.subscribe回调函数中执行的代码是异步的，有可能主线程完成了，异步线程代码还未执行完成 | ![](https://jverson.oss-cn-beijing.aliyuncs.com/d9a5e2264746cb45883a61f7cde98322.jpg) |
| 阻塞方式   | Observable.toBlockingfirst/firstOrDefaultlast/lastOrDefaultsingleforEachtoFuture/toIterable | ![](https://jverson.oss-cn-beijing.aliyuncs.com/dd80c2e10cdd9fc14bb4fc0dae423ff8.jpg) |

### 错误处理

| 方式                             | 说明                                              | 代码                                                         |
| -------------------------------- | ------------------------------------------------- | ------------------------------------------------------------ |
| doOnError(Throwable)             | 一般用于打log或者发告警，无法影响错误的产生及传播 |                                                              |
| onErrorReturn(Throwable, T)      | 出错时返回降级默认值                              | ![](https://jverson.oss-cn-beijing.aliyuncs.com/bf8e2bd77ef506df0747aaf6fdccb299.jpg) |
| onErrorResumeNext(Observable<T>) | 出错后执行异步逻辑，返回降级异步值                | ![](https://jverson.oss-cn-beijing.aliyuncs.com/7db2d5c4a7be2ca4ff5ad0b5a61505f0.jpg) |

### 线程池

虽然 RxJava 是异步编程的利器，但是如果我们只是按照前文所述一顿操作的话，默认是单线程跑的。我们需要通过Scheduler指定调度代码的执行线程池。实际使用过程中，调用Observable.subscribeOn(Scheduler)即可指定该Observable产值和通知的线程池，也就是我们业务开发中的耗时操作（例如RPC调用、IO操作等）对应的线程池。

| 调度器                    | 对应线程池特点                                       |
| ------------------------- | ---------------------------------------------------- |
| Schedulers.io()           | CachedThreadPool无上界不排队的线程池                 |
| Schedulers.computation()  | 计算线程池，线程数=CPU核数                           |
| Schedulers.immediate()    | 使用当前线程                                         |
| Schedulers.from(Executor) | 通过JDK Executor自定义线程池，可根据业务特点自行定义 |

# 到底什么是响应式编程（Reactive Programming）

关于响应式编程(Reactive Programming)，看了上面对 RxJava 的介绍，你可能有过这样的疑问：我们已经有了 Java8 的 Stream, CompletableFuture, 以及 Optional，为什么还必要存在 RxJava 和 Reactor？

[八个层面比较 Java 8, RxJava, Reactor](https://cloud.tencent.com/developer/article/1356284) 这篇文章值得一看，从八个层面对比了七个不同的工具，帮助我们理解标准特性与这些库之间的区别。

八个层面分别是：

1. Composable（可组合）
2. Lazy（惰性执行）
3. Reusable（可复用）
4. Asynchronous（异步）
5. Cacheable（可缓存）
6. Push or Pull（推拉模型）
7. Backpressure（回压）
8. Operator fusion（操作融合）

对比的七个工具分别为

1. CompletableFuture（Java 8）
2. Stream（Java 8）
3. Optional（Java 8）
4. Observable (RxJava 1)
5. Observable (RxJava 2)
6. Flowable (RxJava 2)
7. Flux (Reactor Core)

结论汇总为一张图如下

![](https://jverson.oss-cn-beijing.aliyuncs.com/e162fcc1aedf31f535b61fdfccaa4488.jpg)