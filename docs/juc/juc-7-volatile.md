# volatile 关键字
---

上一篇提到了多线程中可见性、有序性及原子性的问题，通常使用 Synchronized 可以解决这些问题，但是其是一个重量级操作对系统性能影响比较大，应该尽量避免。

而 volatile 关键字就是 Java 提供的另一种解决可见性和有序性问题的方案。**但是对于原子性，volatile 变量的单次读/写操作可以保证原子性的，如 long 和 double 类型变量，但是并不能保证i++这种操作的原子性，因为本质上i++是读、写两次操作。**

## volatile 的作用

一旦一个共享变量（类的成员变量、 类的静态成员变量） 被 volatile 修饰之后， 那么就具备了两层语义：

- 保证了不同线程对这个变量进行读取时的可见性， 即一个线程修改了某个变量的值， 这新值对其他线程来说是立即可见的。 (volatile 解决了线程间共享变量的可见性问题)。
- 禁止进行指令重排序， 阻止编译器对代码的优化。


### 实现可见性

在前文中已经提及过，线程本身并不直接与主内存进行数据的交互，而是通过线程的工作内存来完成相应的操作。这也是导致线程间数据不可见的本质原因。对volatile变量的写操作与普通变量的主要区别有两点：

- 修改volatile变量时会强制将修改后的值刷新的主内存中。
- 修改volatile变量后会导致其他线程工作内存中对应的变量值失效。因此，再读取该变量值的时候就需要重新从读取主内存中的值。

### 防止重排序

重排序分为编译器重排序和处理器重排序。为了实现volatile内存语义，JMM会对volatile变量限制这两种类型的重排序。

### 保证原子性

因为 long 和 double 两种数据类型的操作可分为高32位和低32位两部分，因此普通的long或double类型读/写可能不是原子的。因此**将共享的long和double变量设置为volatile类型，这样能保证任何情况下对long和double的单次读/写操作都具有原子性**。

但是volatile无法保证类似`i++`这样的复合操作具有原子性的，因为它包含了三个步骤：读取i、执行+1、将i写回内存。这个可以通过AtomicInteger或者 Synchronized 来保证+1操作的原子性。


## 实现原理-内存屏障

为了实现 volatile 的内存语义，JVM底层是通过一个叫做“内存屏障”的东西来完成。加入 volatile 关键字时，编译器在生成字节码时，会在指令序列中插入内存屏障，内存屏障，也叫做内存栅栏，是一组处理器指令，用于实现对内存操作的顺序限制。

内存屏障， 有 2 个作用：

- 先于这个内存屏障的指令必须先执行， 后于这个内存屏障的指令必须后执行。
- 使得内存具有可见性。如果你的字段是 volatile， 在读指令前插入读屏障， 可以让高速缓存中的数据失效，重新从主内存加载数据。 在写指令之后插入写屏障， 能让写入缓存的最新数据写回到主内存。

## 总结

与锁相比，Volatile 变量是一种非常简单但同时又非常脆弱的同步机制，它在某些情况下将提供优于锁的性能和伸缩性。然而，使用 volatile 的代码往往比使用锁的代码更加容易出错。并且必须同时满足下面两个条件才能保证在并发环境的线程安全：

- 对变量的写操作不依赖于当前值
- 该变量没有包含在具有其他变量的不变式中

个人感觉还是应该尽量避免在代码中使用，第一使用场景有限，第二容易出错！



## 参考

- [正确使用 Volatile 变量](https://www.ibm.com/developerworks/cn/java/j-jtp06197.html)
- [Java 并发编程：volatile的使用及其原理](https://www.cnblogs.com/paddix/p/5428507.html)