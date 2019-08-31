# 行为参数化 - 将方法作为参数传递
---

所谓的行为参数化英文叫做 `Behavior parameterization`，属于设计模式中的 [策略模式](https://www.runoob.com/design-pattern/strategy-pattern.html)，即一个类的行为或其算法可以在运行时更改，这种类型的设计模式属于行为型模式。这里是通过将行为（方法体）当做参数传递到达运行时更改的目标。

网上有很多关于行为参数化的帖子，大部分都举得是同一个筛选苹果的例子，该示例来自于 [这里](https://livebook.manning.com/book/java-8-in-action/chapter-2/)，可以直接去这里看，就不翻译了。这里我想举一个我在实际工作中使用的一个场景。

我之前做了一个 ES 刷数的工具类，经常需要对 ES 中满足某些条件的数据进行一些操作，可以看到模板方法中互异的查询条件 bqb 可以通过参数传入，而根据查询结构封装更新逻辑则需要将方法作为参数传入。


这里的核心就是将方法作为参数，从而使得不同的方法之间既可以复用逻辑又可以有不同的逻辑。这里自然就想到了 JDK8 的函数式接口和 lambda 表达式，而 JDK 自身提供的 Function 函数式接口就很符合这个场景， Function 就是一个函数，其作用类似于数学中函数的定义 `y = f(x)`，另外还可以使用 Function 函数式接口的 compose 和 andThen 实现更复杂的逻辑控制。

```java
//模板方法
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
		List<EsCommentModel> comments = function.apply(scrollResp.getHits().getHits());
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

//具体方法实现1
public void updateSellerAccount() {
	BoolQueryBuilder bqb = QueryBuilders.boolQuery().filter(termQuery("seller_account", "arnontakorn1"));
	LOGGER.info("updateSellerAccount bqb = {}", bqb.toString());
	updateByScroll(bqb, hits -> {
		List<EsCommentModel> comments = new ArrayList<EsCommentModel>(hits.length);
		for (SearchHit hit : hits) {
			String id = hit.getId();
			EsCommentModel model = new EsCommentModel();
			model.setCommentId(Long.valueOf(id));
			model.setSellerAccount("arnontakorn");
			comments.add(model);
		}
		return comments;
	});
}

//具体方法实现2
public void updateHasImagesByScroll() {
	BoolQueryBuilder bqb = QueryBuilders.boolQuery().mustNot(QueryBuilders.existsQuery("has_images"));
	LOGGER.info("updateHasImagesByScroll bqb = {}", bqb.toString());
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


上面的例子抽象点说就是一个类里面需要多个类似的方法，这些方法内部会依次按照 step1/2/3/4 往下执行，不同的方法只有 step2 不一样，这时候应该怎么去设计这个方法，不需要以后没新增一个方法都去复制一份改，而是只需要完成逻辑不一致的部分即可。这个方法就是这里说的行为参数化，可以通过 Java8 的 Lambda 表达式很优雅的实现。



## 参考

- [Passing code with behavior parameterization](https://livebook.manning.com/book/java-8-in-action/chapter-2/)