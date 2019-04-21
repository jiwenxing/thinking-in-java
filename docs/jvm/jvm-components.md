# JVM 组成 

 JVM 主要由四大部分组成：ClassLoader（类加载器），Runtime Data Area（运行时数据区，内存分区），Execution Engine（执行引擎），Native Interface（本地库接口），下图可以大致描述 JVM 的结构。

![](https://jverson.oss-cn-beijing.aliyuncs.com/bffcc48b5af9cfaf8ef9d2a1eb7d3f42.jpg)


这四大组成部分可以换一种方式描述会更加好理解和记忆一些。JVM 是执行 Java 程序的虚拟计算机系统，那我们来看看执行过程：首先需要准备好编译好的 Java 字节码文件（即class文件），计算机要运行程序需要先通过一定方式（**类加载器**）将 class 文件加载到内存中（**运行时数据区**），但是字节码文件是JVM定义的一套指令集规范，并不能直接交给底层操作系统去执行，因此需要特定的命令解释器（**执行引擎**）将字节码翻译成特定的操作系统指令集交给 CPU 去执行，这个过程中会需要调用到一些不同语言为 Java 提供的接口（例如驱动、地图制作等），这就用到了本地 Native 接口（本地库接口）。

### ClassLoader

ClassLoader 负责加载字节码文件即 class 文件，class 文件在文件开头有特定的文件标示，并且 ClassLoader 只负责class 文件的加载，至于它是否可以运行，则由 Execution Engine 决定。

### Execution Engine

执行引擎，也叫 Interpreter。Class 文件被加载后，会把指令和数据信息放入内存中，Execution Engine 则负责把这些命令解释给操作系统，即将 JVM 指令集翻译为操作系统指令集。

### Native Interface

负责调用本地接口的。他的作用是调用不同语言的接口给 JAVA 用，他会在 Native Method Stack 中记录对应的本地方法，然后调用该方法时就通过 Execution Engine 加载对应的本地 lib。原本多用于一些专业领域，如JAVA驱动，地图制作引擎等，现在关于这种本地方法接口的调用已经被类似于Socket通信，WebService等方式取代。

### Runtime Data Area

Runtime Data Area 是存放数据的，分为五部分：Stack（虚拟机栈），Heap（堆），Method Area（方法区），PC Register（程序计数器），Native Method Stack（本地方法栈）。几乎所有的关于 Java 内存方面的问题，都是集中在这块。

执行引擎及本地方法接口不过多进行介绍，后续我们重点关注其余两个部分：

1. 类加载器相关知识点，包括类的双亲委派加载机制，加载过程等。
2. JVM 内存管理相关知识点，包括内存划分、常用设置、JVM 调优、垃圾回收及常见内存溢出问题的排查解决等。