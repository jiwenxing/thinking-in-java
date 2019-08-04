# 函数式接口 Function 实战
---

上一篇中详细介绍了函数式接口的原理和使用方法，平时在代码中基本也都是对集合类使用一些定义好的具有函数式接口参数的方法，还没有自己定义过此类方法供别人使用，属于比较初级的使用。

## 业务场景

最近再面对一个典型场景的时候终于排上了用场：我经常需要使用 scroll api 对 es 做一些补数或者数据矫正的工作，不同的矫正工作中大部分操作和逻辑都是相同的，不同的地方主要有以下两点

1. 查询条件    
`BoolQueryBuilder bqb = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("has_images"));`
2. 封装更新参数逻辑，这个方法可能会比较复杂。    
`List<EsCommentModel> comments = wrapHasImagesBatchUpdateModels(scrollResp.getHits().getHits());`

比如对不存在`has_images`字段的记录补充值，常规实现如下：
```java
public void updateByScroll() {
	LOGGER.info(">>>>>>>>>>>>> updateByScroll method begin >>>>>>>>>>>> ");
    Date  begin = new Date();
    Integer sum = 0;
    BoolQueryBuilder bqb = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("has_images")); //1. 查询逻辑
    SearchResponse scrollResp = transportClient.prepareSearch(aliasSearch).setTypes(TYPE)
    		.setQuery(bqb)
            .setScroll(new TimeValue(60000))
            .setSize(100).get(); //max of 100 hits will be returned for each scroll
    //Scroll until no hits are returned
    do {
    	sum += scrollResp.getHits().getHits().length;
    	List<EsCommentModel> comments = wrapHasImagesBatchUpdateModels(scrollResp.getHits().getHits()); //2. 封装更新参数逻辑
        try {
			ESUtil.bulkUpdate(comments, false);
		} catch (SearchExecuteException e) {
			LOGGER.error("bulkUpdate ex", e);
		}
        scrollResp = transportClient.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
    } while(scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
    
    Date  end = new Date();
    LOGGER.info(">>>>>>>>>>>>> updateByScroll method end >>>>>>>>>>>> ");
    LOGGER.info(">>>>>>>>>>>>> updateByScroll method update sum: {} >>>>>>>>>>>>", sum);
    LOGGER.info(">>>>>>>>>>>>> updateByScroll method costs: {} >>>>>>>>>>>>", end.getTime() - begin.getTime());
}
```

这时如果要新增一个批量修正的逻辑，有3种选择：

1. 将上面的方法复制一份，修改其中不同的逻辑。**最初级的方式，不推荐**
2. 使用模板方法设计模式，定义一个抽象基类作为模板方法，在抽象类中实现公共逻辑，独有逻辑在子类中各自实现，这样就需要每来一个需求派生一个子类。这种方式在很多场景都很适用，但是这个场景中我不希望创建那么多子类，毕竟就是一个补丁方法，我希望全部在一个类中完成，并且不要有重复代码。 **场景不适用**
3. 这时我想到了函数式编程中的一个概念，将方法或者函数当做参数传递，也就是说定义并实现公共方法，其中个性化的逻辑抽象为方法参数，然后在每个调用的位置将各自独有的实现方法作为参数传入，这就要借住 Function 这个函数式接口。 **推荐**

## 使用函数式接口重构

具体实现如下：

1. 首先定义公共方法，将个性化逻辑（封装更新参数）抽象为参数，这里就使用到了 Function<T, R> 这个函数式接口，即给定参数 T（ES 查询结果），返回结果 R（封装好的更新参数）。

```java
private void updateByScroll(BoolQueryBuilder bqb, Function<SearchHit[], List<EsCommentModel>> function) {
	LOGGER.info(">>>>>>>>>>>>> updateByScroll method begin >>>>>>>>>>>> ");
    Date  begin = new Date();
    Integer sum = 0;
    SearchResponse scrollResp = transportClient.prepareSearch(aliasSearch).setTypes(TYPE)
    		.setQuery(bqb)
            .setScroll(new TimeValue(60000))
            .setSize(100).get(); //max of 100 hits will be returned for each scroll
    //Scroll until no hits are returned
    do {
    	sum += scrollResp.getHits().getHits().length;
    	List<EsCommentModel> comments = wrapBatchUpdateModels(scrollResp.getHits().getHits(), function);
        try {
			ESUtil.bulkUpdate(comments, false);
		} catch (SearchExecuteException e) {
			LOGGER.error("bulkUpdate ex", e);
		}
        scrollResp = transportClient.prepareSearchScroll(scrollResp.getScrollId()).setScroll(new TimeValue(60000)).execute().actionGet();
    } while(scrollResp.getHits().getHits().length != 0); // Zero hits mark the end of the scroll and the while loop.
    
    Date  end = new Date();
    LOGGER.info(">>>>>>>>>>>>> updateByScroll method end >>>>>>>>>>>> ");
    LOGGER.info(">>>>>>>>>>>>> updateByScroll method update sum: {} >>>>>>>>>>>>", sum);
    LOGGER.info(">>>>>>>>>>>>> updateByScroll method costs: {} >>>>>>>>>>>>", end.getTime() - begin.getTime());
}
```

其中 `Function<T, R>` 在 JDK8 中的定义如下，表示输入参数 t，返回结果 r，输入输出可以为不同类型，形式正好符合封装参数的方法。 

`List<EsCommentModel> comments = wrapHasImagesBatchUpdateModels(scrollResp.getHits().getHits());`

```java
@FunctionalInterface
public interface Function<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the function argument
     * @return the function result
     */
    R apply(T t);

    /**
     * Returns a composed function that first applies the {@code before}
     * function to its input, and then applies this function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of input to the {@code before} function, and to the
     *           composed function
     * @param before the function to apply before this function is applied
     * @return a composed function that first applies the {@code before}
     * function and then applies this function
     * @throws NullPointerException if before is null
     *
     * @see #andThen(Function)
     */
    default <V> Function<V, R> compose(Function<? super V, ? extends T> before) {
        Objects.requireNonNull(before);
        return (V v) -> apply(before.apply(v));
    }

    /**
     * Returns a composed function that first applies this function to
     * its input, and then applies the {@code after} function to the result.
     * If evaluation of either function throws an exception, it is relayed to
     * the caller of the composed function.
     *
     * @param <V> the type of output of the {@code after} function, and of the
     *           composed function
     * @param after the function to apply after this function is applied
     * @return a composed function that first applies this function and then
     * applies the {@code after} function
     * @throws NullPointerException if after is null
     *
     * @see #compose(Function)
     */
    default <V> Function<T, V> andThen(Function<? super R, ? extends V> after) {
        Objects.requireNonNull(after);
        return (T t) -> after.apply(apply(t));
    }

    /**
     * Returns a function that always returns its input argument.
     *
     * @param <T> the type of the input and output objects to the function
     * @return a function that always returns its input argument
     */
    static <T> Function<T, T> identity() {
        return t -> t;
    }
}
```

2. 定义 Function 方法，将封装逻辑抽象为函数式参数，从调用处传入

```java
private static List<EsCommentModel> wrapBatchUpdateModels(SearchHit[] hits, Function<SearchHit[], List<EsCommentModel>> function) {
    return function.apply(hits);
}
```

3. 调用时传入具体的处理逻辑

```java
@Override
public void updateReplyStatusByScroll() {
	BoolQueryBuilder bqb = QueryBuilders.boolQuery().should(QueryBuilders.existsQuery("reply_status_jdc")).should(QueryBuilders.existsQuery("reply_status_vendor"));
	LOGGER.info("updateReplyStatusByScroll bqb = {}", bqb.toString());
	updateByScroll(bqb, hits -> {
		List<EsCommentModel> comments = new ArrayList<EsCommentModel>(hits.length); 
	    for (SearchHit hit : hits) {
	    	String id = hit.getId();
	        EsCommentModel model = new EsCommentModel();
	        model.setCommentId(Long.valueOf(id));
	        model.setReplyStatusVendor(ReplyStatusEnum.NONE.getStatus());
	        comments.add(model);
	    }
	    return comments;
	});
}


@Override
public void updateHasImagesByScroll() {
	BoolQueryBuilder bqb = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("has_images"));
	updateByScroll(bqb, hits -> {
		List<EsCommentModel> comments = new ArrayList<EsCommentModel>(hits.length); 
	    for (SearchHit hit : hits) {
	    	String id = hit.getId();
	        Map<String, Object> source = hit.getSourceAsMap();
	        EsCommentModel model = new EsCommentModel();
	    	Integer imageStatus = MapUtils.getIntValue(source, "image_status", 0);
	        Integer videoStatus = MapUtils.getIntValue(source, "video_status", 0);
	        model.setCommentId(Long.valueOf(id));
	        model.setImageStatus(imageStatus);
	        model.setVideoStatus(videoStatus);
	        comments.add(model);
	    }
	    return comments;
	});
}
```

## Function 接口的其它变体

JDK 中还有 Function 接口其它一些变体形式以适用更多的场景，比如

1. `BiFunction<T, U, R>`：R apply(T t, U u); accepts two arguments and produces a result，
2. `IntFunction<R>`：R apply(int value); 入参固定为 int，同理还有 `DoubleFunction<R>`
3. `ToIntFunction<T>`: int applyAsInt(T value); 返回结果固定为 int

## 延伸思考

Function 支持一个入参，BiFunction 支持两个入参，如果方法入参有3个或者更多怎么办呢，其实很简单我们照着定义一个新的函数式接口即可。

```java
@FunctionalInterface
interface TriFunction<A, B, C, R> { 
    R apply(A a, B b, C c); 
    default <V> TriFunction<A, B, C, V> andThen( Function<? super R, ? extends V> after) { 
        Objects.requireNonNull(after); 
        return (A a, B b, C c) -> after.apply(apply(a, b, c)); 
    } 
}
```


