# synchronized 关键字
---

在《深入理解 Java 虚拟机》中，有这样一段话：

> synchronized 关键字在需要原子性、可见性和有序性这三种特性的时候都可以作为其中一种解决方案，看起来是 “万能” 的。的确，大部分并发控制操作都能使用 synchronized 来完成。

synchronized 的作用可以用一句话总结： ** 用于保证同一时刻只能由一个线程进入到临界区，同时保证共享变量的可见性、原子性和有序性。**

## synchronized 特性

1. 原子性：被 synchronized 修饰的代码在同一时间只能被一个线程访问，在锁未释放之前，无法被其他线程访问到。
2. 可见性：对一个变量解锁之前，必须先把此变量同步回主存中。这样解锁后，后续线程就可以访问到被修改后的值。
3. 有序性：synchronized 本身是并不会禁止指令重排和处理器优化。但是由于其互斥特性，并不会出现多个线程同时访问临界区的情况，而编译器和处理器无论如何优化，都必须遵守 as-if-serial 语义（不管怎么重排序，单线程程序的执行结果都不能被改变），因此可以保证有序性！

## synchronized 的用法和作用范围

![](https://jverson.oss-cn-beijing.aliyuncs.com/d230f0bad92930b95d22aa3a32f5e555.jpg)

上图可以看到 synchronized 可以修饰代码块、普通方法以及静态方法，不同的使用方式作用范围稍有不同。下面就详细解释一下三种不同的使用方式。

![](https://jverson.oss-cn-beijing.aliyuncs.com/9d2d1b4220e15a6a8df204681ef01140.jpg)

先定义一个同步方法如下：

```Java
public class ThreadSyn implements Runnable{
    private  volatile static int count = 0;
    @Override
    public void run() {synchronized(this) {for (int i = 0; i < 5; i++) {try {System.out.println(Thread.currentThread().getName() +":"+ (count++));
                    Thread.sleep(100);
                } catch (InterruptedException e) {e.printStackTrace();
                }
            }
        }
    }
}
```

- 当两个并发线程 (thread1 和 thread2) 访问 ** 同一个对象中 ** 的 synchronized(this) 代码块时，在同一时刻只能有一个线程得到执行，另一个线程阻塞等待当前线程执行完后才能执行该代码块。thread1 和 thread2 是互斥的，因为在执行 synchronized 代码块时会锁定当前的对象，一个对象就一个锁。    
```Java
ThreadSyn threadSynOne = new ThreadSyn();
Thread thread1 = new Thread(threadSynOne,"ThreadSynOne");
Thread thread2 = new Thread(threadSynOne,"ThreadSynTwo");
thread1.start();
thread2.start();
```

- 当多个线程访问不同对象的同步代码块，线程不会阻塞，互不干扰。示例如下：    
```Java
ThreadSyn threadSynOne = new ThreadSyn();
ThreadSyn threadSynTwo = new ThreadSyn();
Thread thread1 = new Thread(threadSynOne,"ThreadSynOne");
Thread thread2 = new Thread(threadSynTwo,"ThreadSynTwo");
thread1.start();
thread2.start();
```

- 一个线程访问对象的同步代码块，另一个线程可以访问该对象的非同步代码。


- synchronized 作用的对象是一个静态方法，则它取得的是类的锁，该类所有的对象同一把锁。当一个线程访问类中某个同步的静态方法时，该类中其它所有的静态同步方法都会阻塞。

- synchronized 作用于一个类时，和静态方法类似，也是给这个类加锁，这个类的所有对象用的是同一把锁。    
```Java
public  class  ThreadSyn implements Runnable {
	private volatile static int count = 0;
	@Override
	public void run() {method();
	}

	public  static void method() {synchronized (ThreadSyn.class) {for (int i = 0; i < 5; i++) {try {System.out.println(Thread.currentThread().getName() +" count :"+ (count++));
	                Thread.sleep(100);
	            } catch (InterruptedException e) {e.printStackTrace();
	            }
	        }
	    }
	}
}
```

** 总结一下：** 无论 synchronized 关键字加在方法上还是对象上，如果它作用的对象是非静态的，则它取得的锁属于该对象；如果 synchronized 作用的对象是一个静态方法或一个类，则它取得的锁属于这个类，该类所有的对象共用同一把锁。

## synchronized 一些注意事项

- synchronized 关键字不能继承。虽然 synchronized 可以修改方法，但它并不属于方法定义的一部分。
- 在定义接口方法时不能使用 synchronized 关键字。 
- 构造方法不能使用 synchronized 关键字，但可以使用 synchronized 代码块来进行同步。
- 将一个大的方法声明为 synchronized 会大大影响效率，因此推荐尽量缩小范围并且使用 synchronized 代码块实现同步。
- 由于在 JVM 中具有 String 常量池缓存的功能，因此相同字面量是同一个锁！！！所以一般不要将 String 作为锁对象，而应该改用其他非缓存对象
- 当线程间需要相互等待对方已持有的锁时，就形成死锁，进而产生死循环，需要在写代码是注意

这里延伸一个问题：如何排查代码死锁问题？

通过 jps 查看当前 Java 进程，得到进程 id 后使用 jstack 查看进程堆栈信息，如果发生死锁，堆栈中应该可以看到有线程一直处理 Blocked 状态，并且有 Java-level=deadlock 的提示

> jps 是 jdk 提供的一个查看当前 java 进程的小工具， 可以看做是 JavaVirtual Machine Process Status Tool 的缩写。    
jstack 是 java 虚拟机自带的一种堆栈跟踪工具。jstack 用于打印出给定的 java 进程 ID 或 core file 或远程调试服务的 Java 堆栈信息。 Jstack 工具可以用于生成 java 虚拟机当前时刻的线程快照。线程快照是当前 java 虚拟机内每一条线程正在执行的方法堆栈的集合，生成线程快照的主要目的是定位线程出现长时间停顿的原因，如线程间死锁、死循环、请求外部资源导致的长时间等待等。 线程出现停顿的时候通过 jstack 来查看各个线程的调用堆栈，就可以知道没有响应的线程到底在后台做什么事情，或者等待什么资源。



## synchronized 原理

通过 javap 工具查看同步方法生成的 class 文件，可以看到 JVM 实现同步的原理。其中同步代码块是使用 monitorenter 和 monitorexit 指令实现的，而同步方法 JVM 底层实现依靠的是方法修饰符上的 ACC_SYNCHRONIZED 实现。 

- 同步代码块：monitorenter 指令插入到同步代码块的开始位置，monitorexit 指令插入到同步代码块的结束位置，JVM 需要保证每一个 monitorenter 都有一个 monitorexit 与之相对应。任何对象都有一个 monitor 与之相关联，当且一个 monitor 被持有之后，他将处于锁定状态。线程执行到 monitorenter 指令时，将会尝试获取对象所对应的 monitor 所有权，即尝试获取对象的锁；

- 同步方法：synchronized 方法则会被翻译成普通的方法调用和返回指令如: invokevirtual、areturn 指令，在 VM 字节码层面并没有任何特别的指令来实现被 synchronized 修饰的方法，而是在 Class 文件的方法表中将该方法的 access_flags 字段中的 synchronized 标志位置 1，表示该方法是同步方法并使用调用该方法的对象或该方法所属的 Class 在 JVM 的内部对象表示 Klass 做为锁对象。

无论是 ACC_SYNCHRONIZED 还是 monitorenter、monitorexit 都是基于 Monitor 实现的，在 Java 虚拟机 (HotSpot) 中，Monitor 是基于 C++ 实现的，由 ObjectMonitor 实现。详细原理参考 [深入理解多线程（四）—— Moniter 的实现原理](http://www.hollischuang.com/archives/2030)





## 锁优化

事实上，只有在 JDK1.6 之前，synchronized 的实现才会直接调用 ObjectMonitor 的 enter 和 exit，这种锁被称之为重量级锁。在 JDK1.6 中出现对锁进行了很多的优化，进而出现轻量级锁，偏向锁，锁消除，适应性自旋锁，锁粗化，这些操作都是为了在线程之间更高效的共享数据 ，解决竞争问题。


### 自旋锁

线程的阻塞和唤醒需要 CPU 从用户态转为核心态，频繁的阻塞和唤醒对 CPU 来说是一件负担很重的工作，势必会给系统的并发性能带来很大的压力。同时我们发现在许多应用上面，对象锁的锁状态只会持续很短一段时间，为了这一段很短的时间频繁地阻塞和唤醒线程是非常不值得的。所以引入自旋锁。

所谓自旋锁，就是让该线程等待一段时间，不会被立即挂起，看持有锁的线程是否会很快释放锁。怎么等待呢？执行一段无意义的循环即可（自旋）。 
自旋等待不能替代阻塞，先不说对处理器数量的要求（多核，貌似现在没有单核的处理器了），虽然它可以避免线程切换带来的开销，但是它占用了处理器的时间。如果持有锁的线程很快就释放了锁，那么自旋的效率就非常好，反之，自旋的线程就会白白消耗掉处理的资源，它不会做任何有意义的工作，典型的占着茅坑不拉屎，这样反而会带来性能上的浪费。所以说，自旋等待的时间（自旋的次数）必须要有一个限度，如果自旋超过了定义的时间仍然没有获取到锁，则应该被挂起。 

### 自适应自旋锁

JDK 1.6 引入了更加聪明的自旋锁，即自适应自旋锁。所谓自适应就意味着自旋的次数不再是固定的，它是由前一次在同一个锁上的自旋时间及锁的拥有者的状态来决定。它怎么做呢？线程如果自旋成功了，那么下次自旋的次数会更加多，因为虚拟机认为既然上次成功了，那么此次自旋也很有可能会再次成功，那么它就会允许自旋等待持续的次数更多。反之，如果对于某个锁，很少有自旋能够成功的，那么在以后要或者这个锁的时候自旋的次数会减少甚至省略掉自旋过程，以免浪费处理器资源。 

### 锁消除

为了保证数据的完整性，我们在进行操作时需要对这部分操作进行同步控制，但是在有些情况下，JVM 检测到不可能存在共享数据竞争，这是 JVM 会对这些同步锁进行锁消除。锁消除的依据是逃逸分析的数据支持。 

### 锁粗化

我们知道在使用同步锁的时候，需要让同步块的作用范围尽可能小—仅在共享数据的实际作用域中才进行同步，这样做的目的是为了使需要同步的操作数量尽可能缩小，如果存在锁竞争，那么等待锁的线程也能尽快拿到锁。 
在大多数的情况下，上述观点是正确的，LZ 也一直坚持着这个观点。但是如果一系列的连续加锁解锁操作，可能会导致不必要的性能损耗，所以引入锁粗话的概念。 

### 轻量级锁

引入轻量级锁的主要目的是在多没有多线程竞争的前提下，减少传统的重量级锁使用操作系统互斥量产生的性能消耗。其对锁的获取和释放依靠更加轻量的 cas 实现，从而减少性能损耗。

对于轻量级锁，其性能提升的依据是 “对于绝大部分的锁，在整个生命周期内都是不会存在竞争的”，如果打破这个依据则除了互斥的开销外，还有额外的 CAS 操作，因此在有多线程竞争的情况下，轻量级锁比重量级锁更慢；


### 偏向锁

为了在无多线程竞争的情况下尽量减少不必要的轻量级锁执行路径（即减少不必要的 cas 操作）。上面提到了轻量级锁的加锁解锁操作是需要依赖多次 CAS 原子指令的。那么偏向锁是如何来减少不必要的 CAS 操作呢？偏向锁只需要检查是否为偏向锁、锁标识为以及 ThreadID 即可。

### 重量级锁

重量级锁通过对象内部的监视器（monitor）实现，其中 monitor 的本质是依赖于底层操作系统的 Mutex Lock 实现，操作系统实现线程之间的切换需要从用户态到内核态的切换，切换成本非常高。

## 参考

- [Java 虚拟机的锁优化技术](https://www.hollischuang.com/archives/2344)
- [深入分析 synchronized 的实现原理](https://blog.csdn.net/shandian000/article/details/54927876)
- [Java 多线程——synchronized 使用详解](https://blog.csdn.net/zhangqilugrubby/article/details/80500505)









