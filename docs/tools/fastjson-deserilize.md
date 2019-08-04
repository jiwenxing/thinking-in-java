# fastjson 涉及到泛型的反序列化
---

平时习惯使用 fastjson 处理 json 和 Java 对象之间的转换，fastjson 确实功能也很强大，效率也不会拖后腿。

当对泛型的对象序列化之后想反序列化，实现的方式和正常的用法略有不同！

例如已知一个序列化的对象是这样定义的 `ResultVo<PageVo<DataVo>>`，其中 ResultVo 的定义如下

```java
public class ResultVo<T> extends BaseVo {

	private T data;

	//... ...
}
```

PageVo 定义如下

```java
public class PageVo<T> {
    private int total;
	private int pageNum;
	private int pageSize;
    private List<T> list;
    //... ...
}
```

这时想要将序列化后的 json 字符串反序列化为原来的类结构，需要下面这样的方式实现

```java
Type type = new TypeReference<ResultVo<PageVo<DataVo>>>(){}.getType();
ResultVo<PageVo<DataVo>> resultVo = JSONObject.parseObject(resultJson, type);
```

> 关于 fastjson 的其它一些用法可以参考其 [官方 wiki](https://github.com/alibaba/fastjson/wiki/JSON_API_cn)