# 异步分片请求

---

开发过程中经常会遇到这种场景：分批并行调用，最后将结果进行聚合！下面针对这种场景的一些具体情况来看一下处理方式。

## 准备

下面是一个简单的异步 rpc 调用模拟，后面的示例中都会用到

```Java
// 模拟 rpc 异步调用（rpc 框架一把都支持异步 callback 或 future 异步调用，不需要自己通过线程池伪异步实现）
public static CompletableFuture<List<String>> rpcRequesAsync(List<Integer> ids) {
    return CompletableFuture.supplyAsync(() -> rpcRequest(ids));
}
// 模拟接口内部逻辑
public static List<String> rpcRequest(List<Integer> ids) {
    Thread.sleep(3000);
    return ids.stream().map(i -> "NO." + i).collect(Collectors.toList());
}
```
## 场景一

调用某个批量接口，接口对每次调用的数量有限制（或者处于性能考虑），因此需要分片并行调用，然后再将结果合并

```
@Test
public void testShardRequest() throws ExecutionException, InterruptedException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    List<Integer> ids = Lists.newArrayList(1,2,3,4,5,6);
    // 参数分片
    List<List<Integer>> partition = Lists.partition(ids, 2);
    // 发起异步并发请求得到 List<CompletableFuture>
    List<CompletableFuture<List<String>>> futures = partition.stream().map(part ->   rpcRequesAsync(part)).collect(Collectors.toList());
    System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS)); // 60ms
    // List<CompletableFuture<List<String>>> 获取结果并将结果合并
    List<String> collect = futures.stream().flatMap(future -> future.join().stream()).collect(Collectors.toList());
    System.out.println(collect); // [NO.1, NO.2, NO.3, NO.4, NO.5, NO.6]
    System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS)); // 3078ms  可以看到耗时和单次 rpc 耗时接近，达到并发调用的目的
}
```

这里一定要注意有个坑可能会踩到，下面的写法和上面基本一致，唯一不同的是，将异步请求和获取合并结果的流程放在一个 stream 流里处理了，我们知道 stream 是惰性执行的，只有遇到求值操作时才会触发计算整个流的逻辑，stream 流本身是类似迭代器那样迭代执行的，每个迭代中都会阻塞就导致了整个流程变成了串行执行。虽然说这里把 stream 改成 parallelStream 也能达到并行的目的，但是这并不是我们想要的并行方式。

```Java
@Test
public void testShardRequest() throws ExecutionException, InterruptedException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    List<Integer> ids = Lists.newArrayList(1,2,3,4,5,6);
    List<List<Integer>> partition = Lists.partition(ids, 2);
    // warning! 在一个 stream 里执行异步请求和阻塞获取结果和串行执行是一个效果
    List<String> mergedRet = partition.stream().map(part -> rpcRequesAsync(part))
            .flatMap(future -> future.join().stream())
            .collect(Collectors.toList());
    System.out.println(mergedRet); // [NO.1, NO.2, NO.3, NO.4, NO.5, NO.6]
    System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS)); // 9076 这里是串行执行的时间
}
```
## 场景二

上面的例子是对某一个 rpc 进行分批请求，最后将结果合并后同步返回。如果希望整个接口都返回异步结果，即最终返回的是 CompletableFuture<List<String>>，里面是合并后的结果。应该怎么做呢？

使用 allOf 、flatMap、thenApplyAsync 等将异步结果合并串联

```Java
@Test
public void testFutures() throws ExecutionException, InterruptedException {
    Stopwatch stopwatch = Stopwatch.createStarted();
    List<Integer> ids = Lists.newArrayList(1,2,3,4,5,6);
    List<List<Integer>> partition = Lists.partition(ids, 2);

    // 异步分片并发请求得到 List<CompletableFuture>
    List<CompletableFuture<List<String>>> completableFutureList
            = partition.stream().map(part -> rpcRequesAsync(part)).collect(Collectors.toList());

    // 得到 combinedFuture，即所有子 future 都完成 combinedFuture 才算完成
    CompletableFuture<Void> combinedFuture = CompletableFuture.allOf(completableFutureList.toArray(new CompletableFuture[completableFutureList.size()]));

    // combinedFuture 完成后异步将结果合并，这里可以是 list 的合并，也可以是 map 的合并
    CompletableFuture<List<String>> listCompletableFuture = combinedFuture.thenApplyAsync(avoid -> completableFutureList.stream().flatMap(future -> future.join().stream()).collect(Collectors.toList()));
    
    System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS)); // 69 到这为止都是异步操作
    System.out.println(listCompletableFuture.get()); // [NO.1, NO.2, NO.3, NO.4, NO.5, NO.6] 阻塞看一下是不是符合预期
    System.out.println(stopwatch.elapsed(TimeUnit.MILLISECONDS)); // 3075 最终的耗时也是差不多单次 rpc 的耗时相当
}
```
## 扩展

如果接口获取到是个 Map 对象改怎么合并？整体写法和上面一样，合并的地方

```
public CompletableFuture<Map<Long, Integer>> getMapCompletableFuture(List<Long> ids, HotelSearchContext hotelSearchContext, boolean isGoods) {
    List<List<Long>> partition = Lists.partition(ids, BATCH_SIZE);
    List<CompletableFuture<Map<Long, Integer>>> futureList = partition.stream()
            .map(splitIds -> isGoods ? getShardGoodsRsMapAsync(splitIds, hotelSearchContext) : getShardPoiRsMapAsync(splitIds, hotelSearchContext))
            .collect(Collectors.toList());
    CompletableFuture[] futuresArray = futureList.toArray(new CompletableFuture[futureList.size()]);
    CompletableFuture<Map<Long, Integer>> mapCompletableFuture = CompletableFuture.allOf(futuresArray)
            .thenApply(avoid ->
                    futureList.stream().map(CompletableFuture::join)
                            .collect(HashMap::new, Map::putAll, Map::putAll)
            );
    return mapCompletableFuture;
}
```

<https://stackoverflow.com/questions/52048460/how-to-combine-multiple-completionstage-responses-of-type-listfor-me-or-some-o>