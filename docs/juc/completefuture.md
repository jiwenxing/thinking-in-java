# CompleteFuture
---

# 背景

前面介绍过 Future 是 Java5 添加的类，用来描述一个异步计算的结果。你可以使用 isDone 方法检查计算是否完成，或者使用 get 阻塞住调用线程，直到计算完成返回结果，你也可以使用 cancel 方法停止任务的执行。

虽然 Future 以及相关使用方法提供了异步执行任务的能力，但是对于结果的获取却是很不方便，只能通过阻塞或者轮询的方式得到任务的结果。阻塞的方式显然和我们的异步编程的初衷相违背，轮询的方式又会耗费无谓的CPU资源，而且也不能及时地得到计算结果。另外有时候多个异步操作会有相互依赖，我们希望可以对其进行组合，依赖的异步任务执行完之后可以自动将结果给到被依赖的异步方法继续进行下一个异步操作，类似于 callback 或者 listener 的方式，Future 同样不能满足需求。

于是在 Java 8 中, 出现了一个包含50个方法左右的类: CompletableFuture，提供了非常强大的Future的扩展功能，可以帮助我们简化异步编程的复杂性，提供了函数式编程的能力，可以通过回调的方式处理计算结果，并且提供了转换和组合CompletableFuture的方法。

# CompletableFuture 介绍

```java
public class CompletableFuture<T> implements Future<T>, CompletionStage<T> { //... }
```

可以看到 CompletableFuture 类实现了 CompletionStage 和 Future 接口。我们可以看一下都提供了哪些方法

![](https://jverson.oss-cn-beijing.aliyuncs.com/9dfb120f6823d706deb0874184cd3dc6.jpg)

![](https://jverson.oss-cn-beijing.aliyuncs.com/bcff97753dba998152c38104ba2666f1.jpg)

CompletionStage 接口表示异步计算的某个阶段，可以通过该接口将不同的异步计算任务串联或组合起来。精确的解释见接口文档： [Interface CompletionStage](https://docs.oracle.com/javase/8/docs/api/java/util/concurrent/CompletionStage.html)

也有一部分接口是继承自 future，这里也简单做个介绍：

- boolean cancel (boolean mayInterruptIfRunning) 取消任务的执行。参数指定是否立即中断任务执行，或者等等任务结束
- boolean isCancelled () 任务是否已经取消，任务正常完成前将其取消，则返回 true
- boolean isDone () 任务是否已经完成。需要注意的是如果任务正常终止、异常或取消，都将返回true
- V get () throws InterruptedException, ExecutionException 等待任务执行结束，然后获得V类型的结果。InterruptedException 线程被中断异常， ExecutionException任务执行异常，如果任务被取消，还会抛出CancellationException
- V get (long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException 同上面的get功能一样，多了设置超时时间。参数timeout指定超时时间，uint指定时间的单位，在枚举类TimeUnit中有相关的定义。如果计 算超时，将抛出TimeoutException


**接下来重点看看 CompletableFuture 中的一些常用方法！**


# 创建 CompletableFuture 对象

## Use CompletableFuture as a simple Furture

CompletableFuture 本身实现了 Future 接口，所以也可以当做 Furture 来使用。我们之前常规的异步执行一般就是往线程池提交一个任务（runnable/callable），得到一个 furture，然后在需要结果的时候调用 furture.get 阻塞获取结果

```Java
public Future<String> calculateAsync() throws InterruptedException {
    CompletableFuture<String> completableFuture = new CompletableFuture<>();

    Executors.newCachedThreadPool().submit(() -> {
        Thread.sleep(500);
        completableFuture.complete("Hello");
        return null;
    });

    return completableFuture;
}

Future<String> completableFuture = calculateAsync();

// ... 这里继续执行其他逻辑，直到需要结果的时候再去获取

String result = completableFuture.get();
assertEquals("Hello", result);
```

如果我们已经知道了计算的结果，则可以直接创建一个计算好的 future，这时候 get 方法当然也不会阻塞直接返回结果

```java
Future<String> completableFuture =  CompletableFuture.completedFuture("Hello");

// ...

String result = completableFuture.get();
assertEquals("Hello", result);
```

## 直接提交异步方法

我们最常用的是下面四个静态方法来为一段异步执行的代码创建 CompletableFuture 对象。其中 run 开头的前两个入参是一个 runnable 对象因此没有返回结果；而以 supply 开头的两个方法则表示异步方法有返回值。另外还注意以 Async 结尾并且没有指定 Executor 的方法会使用 ForkJoinPool.commonPool() 作为它的线程池执行异步代码。

```Java
public static CompletableFuture<Void> 	runAsync(Runnable runnable)
public static CompletableFuture<Void> 	runAsync(Runnable runnable, Executor executor)

public static <U> CompletableFuture<U> 	supplyAsync(Supplier<U> supplier)
public static <U> CompletableFuture<U> 	supplyAsync(Supplier<U> supplier, Executor executor)
```

使用方法很简单

```Java
CompletableFuture<String> future
  = CompletableFuture.supplyAsync(() -> "Hello");

// ...

assertEquals("Hello", future.get());
```

# 处理异步计算的结果

有以下几个方法可以用来进行链式调用及对异步 action 进行串联。

这里再次说明一下每种方法都有三个相似的类型，其中方法以 Async 结尾会使用其它的线程去执行(如果没指定线程池则使用默认的 ForkJoinPool.commonPool() 线程池)，方法不以 Async 结尾，意味着 Action 使用上一阶段相同的线程执行(这里也会有例外，下面会进一步说明)！

The methods without the Async postfix run the next execution stage using a calling thread. In contrast, the Async method without the Executor argument runs a step using the common fork/join pool implementation of Executor that is accessed with the ForkJoinPool.commonPool() method. Finally, the Async method with an Executor argument runs a step using the passed Executor.

```java
// 异步执行结束后自动执行另一个action，不依赖异步的返回值，自己也没有返回结果
public CompletionStage<Void> thenRun(Runnable action);
public CompletionStage<Void> thenRunAsync(Runnable action);
public CompletionStage<Void> thenRunAsync(Runnable action,Executor executor);

// 异步获得结构后对其进行消费，不返回值
public CompletionStage<Void> thenAccept(Consumer<? super T> action);
public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action);
public CompletionStage<Void> thenAcceptAsync(Consumer<? super T> action,Executor executor);

// 异步获得结果后对其进行处理，返回处理后的结果
public <U> CompletableFuture<U> thenApply(Function<? super T,? extends U> fn)
public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn)
public <U> CompletableFuture<U> thenApplyAsync(Function<? super T,? extends U> fn, Executor executor)

// 拿到之前异步任务的结果后，以结果为入参进行另一个异步任务，返回一个新的 CompletableFuture
public <U> CompletableFuture<U> thenCompose(Function<? super T, ? extends CompletionStage<U>> fn) {
    return uniComposeStage(null, fn);
}
```

thenRun 方法不需要上一个 action 的结果，也没有返回值

```Java
CompletableFuture<String> completableFuture 
  = CompletableFuture.supplyAsync(() -> "Hello");

CompletableFuture<Void> future = completableFuture
  .thenRunAsync(() -> System.out.println("Computation finished."));

future.get();
```


```Java
CompletableFuture<String> completableFuture
  = CompletableFuture.supplyAsync(() -> "Hello");

CompletableFuture<String> future = completableFuture.thenApplyAsync(s -> s + "World");

assertEquals("Hello World", future.get());
```

使用举例，注册回调方法，监听和处理 CompletableFuture 的计算结果

```java
// 注册Runnable任务
CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<Void> future = completableFuture.thenRun(() -> System.out.println("Computation finished."));
future.get();// 输出Computation finished. 返回null

// 注册Consumer任务
CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<Void> future = completableFuture.thenAccept(s -> System.out.println("Computation returned: " + s)); 
future.get();// 输出Computation returned: Hello 返回null

// 注册Function任务
CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> future = completableFuture.thenApply(s -> s + " World"); 
assertEquals("Hello World", future.get());

// 给thenCompose传入返回CompletableFuture的supplier
CompletableFuture<String> completableFuture 
  = CompletableFuture.supplyAsync(() -> "Hello")
								.thenApply(s -> s + " Beautiful")
    						.thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " World"));// 
 
assertEquals("Hello Beautiful World", completableFuture.get());
```

## thenApply VS thenApplyAsync

可以参考这个链接：[What is the difference between thenApply and thenApplyAsync of Java CompletableFuture?](https://stackoverflow.com/questions/47489338/what-is-the-difference-between-thenapply-and-thenapplyasync-of-java-completablef)

两者都表示对异步计算的结果进行处理返回一个新的结果，并且不管是 thenApply 还是 thenApplyAsync，链式调用都是按照顺序执行的，Async 只是表示接下来的计算要使用哪个线程执行，如果指定了线程池则从其中选择一个线程执行，如果没指定则从默认线程池里选择一个。因此 Async 并不表示会并发执行。

**但是在测试过程中发现了一个诡异的问题，thenApply 的行为感觉有点无法预测，有的情况会阻塞主线程。而 thenApplyAsync 不管是指定线程池还是默认线程池都不会阻塞主线程。**

```Java
@Test
public void test() throws ExecutionException, InterruptedException {
    ExecutorService executor = Executors.newFixedThreadPool(10);
    System.out.println("begin: " + Thread.currentThread().getName());
    CompletableFuture<Void> completableFuture = CompletableFuture.supplyAsync(() -> {
        System.out.println("a: " + Thread.currentThread().getName());
        sleep(100); // 模拟计算耗时
        return 0;
    }, executor).thenApply(x -> {
        sleep(1000);
        System.out.println("b: " + Thread.currentThread().getName());
        return x + 1;
    }).thenApply(x -> {
        sleep(1000);
        System.out.println("c: " + Thread.currentThread().getName());
        return x + 1;
    }).thenAccept(x -> {
        sleep(1000);
        System.out.println("d: " + Thread.currentThread().getName());
        System.out.println("result = " + x);
    });
    System.out.println("end: " + Thread.currentThread().getName());
    completableFuture.get();
}
```

完整运行看到输出如下，符合预期，整个链式调用都使用了和 supplyAsync 相同的线程进行异步计算。

> begin: main  
a: pool-2-thread-1  
end: main  
b: pool-2-thread-1  
c: pool-2-thread-1  
d: pool-2-thread-1  
result = 2

但是当我们将 supplyAsync 中的 `sleep(100);` 这一行注释掉之后，运行结果如下。可以看到整个过程都是阻塞的，几个 then 计算都是在主线程完成的，这显然和预期是不一样的！

> begin: main  
a: pool-2-thread-1  
b: main  
c: main  
d: main  
result = 2  
end: main  

在这篇文章 [CompletableFuture – The Difference Between thenApply/thenApplyAsync](https://4comprehension.com/completablefuture-the-difference-between-thenapply-thenapplyasync/) 中是这么解释这个问题的，大概意思是一个 future 如果在计算完成之后再调用 thenApply，那么就会使用客户端线程（即主线程）继续执行，但是如果说 supplyAsync 里执行时间较长，可以在其执行完成之前就注册了 thenApply，那么 thenApply 的计算将使用 supplyAsync 同样的线程。

> if a future completes before calling thenApply(), it will be run by a client thread, but if we manage to register thenApply() before the task finished, it will be executed by the same thread that completed the original future

因此在那篇文章的最后作者也给出了建议，尽量使用 Async 类的方法并传入自定义的线程池，For our programs to be predictable, we should consider using CompletableFuture’s thenApplyAsync(Executor) as a sensible default for long-running post-completion tasks.

# 使用 thenCompose 组合多个 CompletableFuture

如果我们要对两个对立的 future 的结果做一些操作，这时候就可以使用 thenCompose 了，If we want to execute two independent Futures and do something with their results, we can use the thenCombine method that accepts a Future and a Function with two arguments to process both results:

```Java
CompletableFuture<String> completableFuture 
  = CompletableFuture.supplyAsync(() -> "Hello")
    .thenCombine(CompletableFuture.supplyAsync(
      () -> " World"), (s1, s2) -> s1 + s2));

assertEquals("Hello World", completableFuture.get());
```

如果我们对两个独立的 future 结果处理完也不需要返回结果，还可以使用 thenAcceptBoth

```Java
CompletableFuture future = CompletableFuture.supplyAsync(() -> "Hello")
  .thenAcceptBoth(CompletableFuture.supplyAsync(() -> " World"),
    (s1, s2) -> System.out.println(s1 + s2));
```

## thenApply VS thenCompose

参考 [Difference Between thenApply() and thenCompose()](https://www.baeldung.com/java-completablefuture#Combining-1)

thenApply 类似于 Stream 里的 map 操作，将上个阶段的 CompletableFuture<U> 经过计算转化成 CompletableFuture<V> 返回，可以转换结果类型。而 thenCompose 类似于 Stream 里的 flatMap 操作，用来连接两个 CompletableFuture，返回值是新的 CompletableFuture。

```java
CompletableFuture<String> completableFuture = CompletableFuture.supplyAsync(() -> "Hello").thenCompose(s -> CompletableFuture.supplyAsync(() -> s + " World"));
System.out.println(completableFuture.get());

// 上面的方法要用 thenApply 的话结果就会变成 CompletableFuture 的嵌套

CompletableFuture<CompletableFuture<String>> completableFuture = CompletableFuture.supplyAsync(() -> "Hello").thenApplyAsync(s -> CompletableFuture.supplyAsync(() -> s + " World"));
System.out.println(completableFuture.get().get());
```

# 多个 Futures 并行计算

并行计算多个 futures，然后等所有的 futures 执行完之后再做一些计算，可以使用 CompletableFuture.allOf 将 futures 合并起来得到一个新的组合 CompletableFuture<Void> 。

```Java
CompletableFuture<String> future1  
  = CompletableFuture.supplyAsync(() -> "Hello");
CompletableFuture<String> future2  
  = CompletableFuture.supplyAsync(() -> "Beautiful");
CompletableFuture<String> future3  
  = CompletableFuture.supplyAsync(() -> "World");

CompletableFuture<Void> combinedFuture 
  = CompletableFuture.allOf(future1, future2, future3);

// ...

combinedFuture.get();

assertTrue(future1.isDone());
assertTrue(future2.isDone());
assertTrue(future3.isDone());
```

但是这个方法显然有一个弊端是它不能返回结果，我们可以换一种方式如下。其中 CompletableFuture.join() 方法和 get() 方法类似，区别是 join 方法 throws an unchecked exception in case the Future does not complete normally，这样就允许我们使用 Stream.map() 这些函数；而 get() 方法会抛出受检异常如 ExecutionException, InterruptedException 必须在程序中显式处理。

```Java
String combined = Stream.of(future1, future2, future3)
  .map(CompletableFuture::join)
  .collect(Collectors.joining(" "));

assertEquals("Hello Beautiful World", combined);
```

# 计算结果完成时的处理

当 CompletableFuture 的计算结果完成，或者抛出异常的时候，我们可以执行特定的 Action。主要是下面的方法：

```Java
// 可以看到 whenComplete 的 Action 的类型是 BiConsumer<? super T,? super Throwable>，它可以处理正常的计算结果，或者异常情况。
public CompletableFuture<T>     whenComplete(BiConsumer<? super T,? super Throwable> action)
public CompletableFuture<T>     whenCompleteAsync(BiConsumer<? super T,? super Throwable> action)
public CompletableFuture<T>     whenCompleteAsync(BiConsumer<? super T,? super Throwable> action, Executor executor)

// exceptionally方法返回一个新的CompletableFuture，当原始的CompletableFuture抛出异常的时候，就会触发这个CompletableFuture的计算，调用function计算值.如果没抛异常，则原始的CompletableFuture正常计算完后，这个新的CompletableFuture也计算完成，它的值和原始的CompletableFuture的计算的值相同。
public CompletableFuture<T>     exceptionally(Function<Throwable,? extends T> fn)

// 下面一组方法虽然也返回CompletableFuture对象，但是对象的值和原来的CompletableFuture计算的值不同。当原先的CompletableFuture的值计算完成或者抛出异常的时候，会触发这个CompletableFuture对象的计算，结果由BiFunction参数计算而得。因此这组方法兼有whenComplete和转换的两个功能。

public <U> CompletableFuture<U>     handle(BiFunction<? super T,Throwable,? extends U> fn)
public <U> CompletableFuture<U>     handleAsync(BiFunction<? super T,Throwable,? extends U> fn)
public <U> CompletableFuture<U>     handleAsync(BiFunction<? super T,Throwable,? extends U> fn, Executor executor)
```

几个方法都会返回 CompletableFuture，当 Action 执行完毕后它的结果返回原始的 CompletableFuture 的计算结果或者返回异常。

这里扩展一下 BiConsumer，代表了一个接受两个输入参数的操作，并且不返回任何结果，与之对应的 BiFunction<T,U,R> 则代表一个接受两个输入参数的方法，并且返回一个结果！

```Java
@FunctionalInterface
public interface BiConsumer<T, U> {
 
    
    void accept(T t, U u);
 
    /**本接口中的accept先执行，传入的BiConsumer 接口类型的参数，后执行accept*/
    default BiConsumer<T, U> andThen(BiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
 
        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }
}
```


## 异常处理

CompletableFuture 的异常处理也有自己的风格，可以使用 handle 接收两个参数（成功后的结果和异常时的异常）进行处理，示例如下：

```Java
String name = null;

// ...

CompletableFuture<String> completableFuture  
  =  CompletableFuture.supplyAsync(() -> {
      if (name == null) {
          throw new RuntimeException("Computation error!");
      }
      return "Hello, " + name;
  })}).handle((s, t) -> s != null ? s : "Hello, Stranger!");

assertEquals("Hello, Stranger!", completableFuture.get());
```

如果我们需要手动完成一个 future，可以使用一个正常的 result 进行 complete，也可以使用一个异常去 complete，示例如下：

```Java
CompletableFuture<String> completableFuture = new CompletableFuture<>();

// ...

completableFuture.completeExceptionally(
  new RuntimeException("Calculation failed!"));

// ...

completableFuture.get(); // ExecutionException
```


# JDK9 对 CompletableFuture API 的增强

Java 9 主要对 CompletableFuture 做了以下改进，增加了 8 个新方法和 5 个静态工具方法

1. 支持 delays 和 timeouts
2. 提升了对子类化的支持
3. 新的工厂方法

```Java
Executor defaultExecutor()
CompletableFuture<U> newIncompleteFuture()
CompletableFuture<T> copy()
CompletionStage<T> minimalCompletionStage()
CompletableFuture<T> completeAsync(Supplier<? extends T> supplier, Executor executor)
CompletableFuture<T> completeAsync(Supplier<? extends T> supplier)
CompletableFuture<T> orTimeout(long timeout, TimeUnit unit)
CompletableFuture<T> completeOnTimeout(T value, long timeout, TimeUnit unit)

Executor delayedExecutor(long delay, TimeUnit unit, Executor executor)
Executor delayedExecutor(long delay, TimeUnit unit)
<U> CompletionStage<U> completedStage(U value)
<U> CompletionStage<U> failedStage(Throwable ex)
<U> CompletableFuture<U> failedFuture(Throwable ex)
```


# 参考

- [Java CompletableFuture 详解](https://colobu.com/2016/02/29/Java-CompletableFuture/)
- [Guide To CompletableFuture](https://www.baeldung.com/java-completablefuture#Combining-1)
- [Java 9 CompletableFuture API Improvements](https://www.baeldung.com/java-9-completablefuture)
- [Java 9 改进的 CompletableFuture API](https://www.runoob.com/java/java9-completablefuture-api-improvements.html)
