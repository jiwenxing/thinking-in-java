# Java 并发编程知识体系
---

## to study list

基础知识
- [x] 并发优缺点及一些基本概念
- [x] 线程状态及基本操作，线程创建、状态转换、基本操作、守护线程

并发理论
- [x] JMM 内存模型
- [x] 指令重排序
- [x] happens-before 规则

并发关键字
- [x] synchronize 使用方法、内存语义、重入性、锁优化
- [x] volatile 实现原理、内存语义
- [ ] final 实现原理、使用方式
- [x] 并发三大特性：原子性、可见性、有序性

Lock 体系
- [ ] lock 与 synchronize 比较
- [ ] AQS 源码解析及使用方式
- [ ] ReentrantLock
- [ ] ReentrantReadWriteLock
- [ ] Condition 机制
- [ ] LockSupport

并发容器
- [ ] ConcurrentHashMap
- [ ] CopyOnWriteArArrayList
- [ ] ThreadLocal
- [x] BlockingQueue
- [ ] ConcurrentLinkedQueue

线程池
- [x] ThreadPoolExecutor
- [x] ScheduledThreadPoolExeuctor
- [x] FutureTask
- [ ] Fork/Join 框架

原子操作类
- [ ] 实现原理
- [ ] 常用类介绍

并发工具
- [ ] 倒计数器 CountDownLatch
- [ ] 循环栅栏 CyclicBarrier
- [ ] 以上两者比较
- [ ] 资源访问控制 Semaphore
- [ ] 数据交换 Exchanger

并发实践
- [x] 生产者消费者问题

找时间把这个系列的一些主题都梳理一下：[廖雪峰-多线程](https://www.liaoxuefeng.com/wiki/1252599548343744/1255943750561472)

## ecosystem

![](https://jverson.oss-cn-beijing.aliyuncs.com/d08340c896c473db0ef95ed8d47cacac.jpg)

注： [图片出处](https://www.processon.com/view/5ab5a979e4b0a248b0e026b3?fromnew=1#outline)

