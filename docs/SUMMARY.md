# Summary

- [Introduction](README.md)
- Java 特性

  - [Lambda 表达式 & 函数式接口](jdk/lambda_expression.md)
  - [几种常用的函数式接口](jdk/interface_function_commons.md)
  - [函数式接口 Function 实战](jdk/interface_function_practise.md)
  - [Java8 中的双冒号操作符](jdk/double_colon_operator.md)
  - [Java8 中的 Streams API](jdk/streams_api.md)
  - [Java Optional 类的使用](jdk/optional.md)
  - [Java8 中的 Map API](jdk/map_api.md)
  - [行为参数化（Behavior parameterization）](jdk/behavior_parameterization.md)
- Java 泛型及反射

  - [Java 泛型的理解和使用](basic/generics.md)
  - [泛型中的 Type 接口](basic/type.md)
  - [Class 对象 - Java 类型信息](jvm/java-reflection-class.md)
  - [Java 反射特性](jvm/java-reflection.md)
- 数据结构

  - [字符串基础](data-types/string.md)
  - [容器(持有对象)的基本概念及操作](data-types/collection.md)
  - [List 接口及实现类](data-types/list-interface.md)
  - [Queue 接口及实现类概述](data-types/queue-interface.md)
  - [PriorityQueue 和 Heap](data-types/PriorityQueue.md)
  - [BlockingQueue 介绍](data-types/BlockingQueue.md)
  - [ArrayBlockingQueue 源码解析](data-types/ArrayBlockingQueue.md)
  - [Set 接口及实现](data-types/set-interface.md)
  - [HashMap](data-types/HashMap.md)
  - [ConcurrentHashMap](data-types/ConcurrentHashMap.md)
  - [HashMap、HashTable & ConcurrentHashMap](data-types/HashMap-Hashtable-CocurrentHashMap.md)
  - [TreeMap 和 LinkedHashMap 介绍](data-types/TreeMap-LinkedHashMap.md)
- Java 多线程

  - [Java 并发知识体系](juc/juc-1-ecosystem.md)
  - [多线程基础](juc/juc-2-basic.md)
  - [创建线程的几种方式](juc/juc-3-thread-creation.md)
  - [线程池的使用](juc/juc-4-threadpool.md)
  - [ForkJoinPool](juc/juc-forkjoinpool.md)
  - [CompleteFuture](juc/completefuture.md)
  - [异步分片请求](juc/juc-8-shardasync.md)
  - [线程协作 - wait/notify/notifyAll](juc/juc-5-thread-collaboration.md)
  - [线程协作 - sleep/yield/join](juc/juc-sleep-yield-join.md)
  - [JMM Java 内存模型_](juc/juc-6-jmm.md)
  - [volatile 关键字](juc/juc-7-volatile.md)
  - [synchronized & 锁优化](juc/juc-8-synchronized.md)
  - [Lock & ReentrantLock](juc/juc-9-lock.md)
  - [Lock & Condition](juc/lock-condition.md)
  - [AQS 原理_](juc/juc-10-AQS.md)& 并发工具类
  - [并发工具类 - CountDownLatch](juc/juc-11-tools.md)
  - [ThreadLocal 介绍](juc/juc-threadlocal.md)
- 异步及响应式编程
  - [性能优化的角度看异步及全链路异步](async/completely-async-system.md)
  - [RPC 框架如何支持异步](async/rpc-async.md)
  - [RxJava 及响应式编程](async/rxjava.md)
- JVM 虚拟机

  - [JVM 宏观认识](jvm/jvm-introduction.md)
  - [JVM 组成](jvm/jvm-components.md)
  - [JVM 内存管理](jvm/jvm-memory-management.md)
  - [GC 垃圾回收机制](jvm/jvm-gc.md)
  - [ClassLoader 类加载器](jvm/classloader.md)
- 算法

  - [常见算法套路](algorithm/algorithm-summary.md)
  - [排序算法](algorithm/sort.md)
  - [二分查找](algorithm/binary-search.md)
  - [编辑距离与文本相似度](algorithm/edit-distance.md)
  - [AC 算法与字符串搜索](algorithm/3-aho-corasick.md)
  - [sword-1：String 转 int](algorithm/sword-1-str2int.md)
  - [sword-8： 旋转数组的最小数字](algorithm/sword-8-binary-search.md)
  - [sword-15： 链表中倒数第 k 个节点](algorithm/sword-15-kth-element.md)
  - [LeetCode-1： 数组中和为指定值得两个元素](algorithm/leetcode-1-twosum.md)
  - [leetcode-2: 两个链表的数相加](algorithm/leetcode-2-add-two-numbers.md)
  - [leetcode-20: 合法的括号](algorithm/leetcode-20-valid-parentheses.md)
  - [leetcode-26: 删除有序数据中的重复值](algorithm/leetcode-26-rm-duplicates-from-sorted-array.md)
  - [leetcode-70: 爬楼梯问题](algorithm/leetcode-70-climb-stairs.md)
  - [leetcode-21: 合并两个有序链表](algorithm/leetcode-21-merge-two-sorted-lists.md)
  - [leetcode-141: 环形链表判断](algorithm/leetcode-141-list-cycle.md)
  - [leetcode-206: 单链表逆转](algorithm/leetcode-206-list-reverse.md)
- 设计模式

  - [UML 类图_](design/0-uml.md)
  - [Singleton 单例模式](design/1-singleton.md)
  - [Builder 建造者模式](design/2-builder.md)
  - [代理模式之JDK代理和CGLib代理](basic/JDK代理及CGLib代理.md)
  - [Java SPI](design/java-spi.md)
- 故障诊断
  - [Jstack 工具使用](debug/jstack.md)
  - [show-busy-java-threads](debug/show-busy-java-threads.md)
  - [Arthas](debug/arthas.md)


- 待归档

  <!-- - [fastjson 泛型的反序列化](tools/fastjson-deserilize.md) -->
  - [Comparator 与排序](tools/comparator-sort.md)
  - [重试工具：Guava Retryer](tools/guava-retryer.md)
  - [Java IO & NIO 介绍](basic/java-bio-nio-aio.md)

- 方法论

  - [工程质量建设](methodology/software-quality.md)
  - [怎样做 Mentor？](methodology/mentor.md)

- Machine Learning

  - [机器学习知识体系](ai/knownadge-system.md)
  - [Embedding & 预训练](ai/embedding.md)
  - [CTR 预估模型](ai/ctr-predict.md)

- 基础知识
  - Java 基本概念
    - [继承与初始化过程](basic/初始化和类的加载.md)
    - [多态](basic/多态.md)
    - [抽象类](basic/抽象类.md)
    - [接口](basic/接口.md)
    - [内部类](basic/inner-class.md)
    - [枚举](basic/enum.md)
    - [访问权限控制](basic/访问权限控制.md)
    - [Java 异常处理](basic/异常处理.md)
    - [final 关键字](basic/final.md)
