# JVM 内存管理
---

JVM 在执行 Java 程序的过程中会将其管理的内存划分为若干个不同的数据区域如下图所示，这些区域各自有不同的不同的用途、创建和销毁时间，有的区域随着虚拟机进程启动而存在，而有的则依赖用户线程的启动和结束而建立和销毁；有些区域是属于线程私有，而另外一些区域则是是线程共享。本篇将将根据各内存区域是属于线程私有还是线程共享将其分为两大类分别进行说明。

![](https://jverson.oss-cn-beijing.aliyuncs.com/2d4872048ed0b91a683b003723556ebe.jpg)


## 线程私有的内存区域


### 程序计数器（Program Counter Register）

程序计数器是一块较小的内存空间（可能位于cpu的寄存器，有待确认），可以看做是当前字节码指令执行的行号指示器，记录了当前正在执行的虚拟机字节码指令地址。每个线程都有各自独立的程序计数器，注意如果正在执行的是 Native方法，则程序计数器为空（Undifined），并且 JVM 规范中并没有对程序计数器定义 **OutOfMemoryError** 异常。

###  虚拟机栈（VM Stack）

虚拟机栈也是线程私有的，它描述的是**Java方法执行的内存模型**：每个方法在执行的同时都会创建一个栈帧（Stack Frame）用于存储局部变量表、操作数栈、动态链接、方法出口等信息，每一个方法从调用直至完成的过程，就对应着一个栈帧在虚拟机栈中入栈和出栈的过程。

虚拟机栈帧中，局部变量表是比较为人所熟知的，也就是平常所说的“栈”，局部变量表所需的内存空间在编译期间分配完成，当进入一个方法时，这个方法需要在栈帧中分配多大的局部变量空间是完全确定的，在方法运行期间不会改变局部变量表的大小。

虚拟机栈有两种异常情况：

1. **StackOverflowError**：线程请求的栈深度大于虚拟机所允许的深度，特别是方法的递归调用时（有空程序模拟一下）
2.  **OutOfMemoryError**：虚拟机栈无法满足线程所申请的空间需求，即使经过动态扩展仍然无法满足时抛出（有空程序模拟一下）

### 本地方法栈（Native Method Stack）

本地方法栈与虚拟机栈相似，不过服务于本地方法，有些虚拟机将这两个区域合二为一。本地方法栈中抛出异常的情况与虚拟机栈相同。



## 线程共享的内存区域

### 堆（Heap）

通常来说，堆是Java虚拟机管理的内存中最大的一块，被所有线程共享，在虚拟机启动时创建，堆的作用就是存储对象实例。

堆也是垃圾收集器所管理的主要区域，因此很多时候也被称作**GC堆**。从内存回收的角度来看，由于现在收集器基本都采用分代收集算法，因此堆还可以被细分为：**新生代和老年代**。再继续细分可以分为：**Eden空间、From Survivor空间、To Survivor空间等**，从内存分配的角度来看，线程共享的堆中还可以划分出多个线程私有的分配缓冲区（Thread Local Allocation Buffer，TLAB）。

堆可以是物理上不连续的空间，只要逻辑上是连续的即可，-Xmx和-Xms参数可以控制堆的最大和最小值。

堆的空间大小不满足时将抛出OutOfMemoryError异常。

### 方法区（Method Area）

用于存储已被虚拟机加载的类信息、常量、静态变量、JIT编译后的代码等数据。Java虚拟机规范将方法区描述为堆的一个逻辑部分，但是它却有一个别名叫做Non-Heap（非堆）。

方法区同样会抛出OutOfMemoryError异常。

在方法区中有一部分区域用来存储编译期产生的各种字面量和符号引用，这部分内容将在类加载后进入方法区的运行时常量池中存放。这里需要说明一点，常量并不是只能在编译期产生，运行期间也会产生新的常量并被发在常量池中，如 String 类的 intern() 方法。

## 直接内存

注意直接内存不属于虚拟机运行时数据区的一部分，也不是 JVM 规范中定义的内存区域，其主要用于 JDK1.4 引入的基于通道(Channel)和缓冲区(Buffer)的 NIO 类，可以避免在 Native 堆和 Java 堆之间来回复制数据从而提高性能。该部分内存分配不受 Java 堆内存大小的限制，但是肯定也受限于机器硬件内存的限制。

## reference

- [一张图看懂Java虚拟机内存区域模型](https://zhuanlan.zhihu.com/p/31503944)