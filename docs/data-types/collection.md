# 容器的基本概念
---

所谓持有对象就是保存和持有对象的引用，实现的方式包括 List、Queue、Map、Set，这些对象类型也被称为“集合类”，但是由于 Java 中用 Collection 这个名字指代该类库的一个特殊子集（List、Set 及 Queue 等），因此可以使用范围更广的“容器”来称呼。

Java 容器类库的用途就是“保存对象”，可以划分成两个不同的概念：

### Collection 

一个独立元素的序列，包括 List、Set 及 Queue 等，继承树如下所示，后续重点关注其中常用的一些实现类原理

![](https://jverson.oss-cn-beijing.aliyuncs.com/16c6ed925039c694f14da4d0b976d798.jpg)



### Map

一组成对的“键值对”对象，后续将重点介绍其中常用的 HashMap、HashTable & ConcurrentHashMap 等实现

![](https://jverson.oss-cn-beijing.aliyuncs.com/7cbf202b26e96c7a6ea2906044cc2b60.jpg)





## 容器添加元素

可以使用 Collection 的 add 方法，也可以使用 `java.util.Collections` 工具类中提供的添加元素的静态方法，但是有一点要注意的是使用 `Arrays.asLists()` 方法得到的 list 底层数据结构仍为数组，因此不能进行 add 和 delete 等修改其尺寸的操作。

```java
public static void main(String[] args) {
		/**
		 * asList：This method acts as bridge between array-based and collection-based APIs
		 * ArrayList构造函数接受一个实现Collection接口的类型作为参数，使用asList将一个数组转化为大小固定的List
		 */
		Collection<Integer> collection = new ArrayList<>(Arrays.asList(1,2,3,4,5));
		
		/**
		 * 几种不同的添加元素方法
		 * 注意Collections类包含了很多非常有用的操作集合类的静态方法
		 */
		Integer[] moreInts = {9,8,7,6};
		collection.addAll(Arrays.asList(moreInts));
		Collections.addAll(collection, 11, 12);
		Collections.addAll(collection, moreInts);
		
		System.out.println(collection); //可直接打印
		
		/**
		 * Arrays.asList得到的list其底层数据结构仍然是数组，因此不能调整尺寸，也就是说不能添加和删除元素
		 * 如果进行尺寸改变的操作便会抛出“java.lang.UnsupportedOperationException”
		 */
		List<Integer> list = Arrays.asList(1, 2, 3);
		list.add(99);
		System.out.println(list);
		
		/**
		 * 包括Map在内的所有容器类型都可以直接打印输出，而不像数组必须使用Arrays.toString()方法
		 */
		Map<String, String> map = new HashMap<String, String>();
		map.put("cat", "ketty");
		map.put("dog", "alaska");
		System.out.println(map);
	}
```

### 容器打印

正如上面代码看到的，容器类型可以直接打印(包括Map类型)，这是因为 `AbstractCollection` 中已经实现了默认的 toString 方法。而打印数组则必须使用 `Arrays.toString()` 方法来产生可打印的形式。