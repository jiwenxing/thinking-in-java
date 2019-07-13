# 线程协作(wait/notify/sleep/yield/join)
---

Java 中常见的关于多线程协作的一些关键字和方法及其作用如下：

![](https://jverson.oss-cn-beijing.aliyuncs.com/935997150ff402d172055664c9122ed3.jpg)

要理解上面的关键字及方法的作用和原理首先需要了解线程的几种状态及其转换关系，这个之前有大概介绍过，下面就结合这些内容对以上这些关键字及方法逐一进行介绍

## sleep、yield、join & wait、notify、notifyAll()

wait，notify 和 notifyAll 只能在同步控制方法或者同步控制块里面使用，而 sleep 可以在任何地方使用。


## 参考
[Java 并发编程：线程间的协作(wait/notify/sleep/yield/join)](https://www.cnblogs.com/paddix/p/5381958.html)