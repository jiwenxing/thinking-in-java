# ConcurrentHashMap
---

ConcurrentHashMap 在 JDK1.8 之前都是使用锁分段的原理，表现在多个线程操作上，它不用做额外的同步的情况下默认同时允许16个线程读和写这个 Map 容器。因为不像`HashTable`和`Synchronized Map`，`ConcurrentHashMap`不需要锁整个Map，相反它划分了多个段(segments)，要操作哪一段才上锁那段数据。

在 JDK1.8 中则主要是采用了 CAS（Compare And Swap） 算法实现线程安全的。要深刻理解 JDK1.8 中 ConcurrentHashMap 的实现原理，需要先熟悉 Java 内存模型，volatile 关键字和 CAS 算法等基础知识。

#### **悲观锁与乐观锁**

所谓 CAS 简单讲，它具有三个操作数，a、内存位置V，预期值A和新值B。如果在执行过程中，发现内存中的值V与预期值A相匹配，那么他会将V更新为新值A。如果预期值A和内存中的值V不相匹配，那么处理器就不会执行任何操作。CAS算法就是我再技术点中说的“无锁定算法”，因为线程不必再等待锁定，只要执行CAS操作就可以，会在预期中完成。

