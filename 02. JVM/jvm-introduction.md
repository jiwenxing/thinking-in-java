# JVM 介绍
---

学习 Java 虚拟机首先需要对它有一个准确的宏观认识，JVM 是个什么东西，能做什么，在我们的日常开发中具体处于什么位置？这些问题都需要搞清楚。

首先从名字开始，既然叫虚拟机，什么是虚拟机呢？有的人可能在 Windows PC 上通过 VMware 或者 Virtual Box 安装过 Linux，这里你的物理 PC 上的 CPU、内存、寄存器以及 Windows 操作系统等组成了一个物理真实的计算机系统， 而 VMware 及 Virtual Box 就是一个虚拟机的角色，它同样具有完整的虚拟硬件环境（包括处理器，内存，I/O 设备），连同安装的 Linux 操作系统也组成了一个完整的虚拟计算机系统。

**虚拟机**就是指可以通过软件模拟的具有完整硬件系统功能的、运行在一个完全隔离环境中的完整计算机系统。

回到 Java 虚拟机上，Java 虚拟机也有自己完善的硬体架构，如处理器、堆栈、寄存器等，还具有相应的指令系统。Java 虚拟机屏蔽了与具体操作系统平台相关的信息，使得Java 程序只需生成在 Java 虚拟机上运行的目标代码（字节码），就可以在多种平台上不加修改地运行。Java 虚拟机（JVM）可以以一次一条指令的方式来解释字节码（把它映射到实际的处理器指令），或者字节码也可以由实际处理器中称作 just-in-time 的编译器进行进一步的编译。

换句话说，Java 虚拟机（JVM）是运行所有 Java 程序的抽象计算机，是 Java 语言的运行环境。JVM 包括一套字节码指令集、一组寄存器、一个栈、一个垃圾回收堆和一个存储方法域。

### Sun HotSpot VM

运行 `java -version` 指令可以查看当前的 JDK 版本，信息类似下面这样

![](https://jverson.oss-cn-beijing.aliyuncs.com/1ab2da38d72889ae0e9af8c168aa064f.jpg)


能看到当前 JDK 的版本信息，同时最下面一行显示 64 位的 HotSpot Server VM，说明当前 JDK 使用的是 HotSpot Server 版本虚拟机，实际上从 JDK1.3 开始，HotSpot VM 就已经是 Sun JDK 及 OpenJDK（JDK 的开源版本） 的默认虚拟机了，也是目前使用最广的虚拟机。名称中的 HotSpot 代表其具有的热点代码探测技术，可以通过计数器找出最具有编译价值的代码，然后通知 JIT 编译器以方法为单位进行编译。这样通过编译器和解释器恰当的协同工作，可以在程序响应时间和执行性能中取得最佳的平衡。

这里需要注意 Java HotSpot Client VM 和 Java HotSpot Server VM 是 JDK 关于 JVM 的两种不同的实现，前者可以减少启动时间和内存占用，而后者则提供更加优秀的程序运行速度，可以参考官方文档 [Java Virtual Machine Technology](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/index.html)。

除了 JDK 默认的 HotSpot 虚拟机以外，比较著名的商用虚拟机还有 IBM J9 VM 以及 Google Android Dalvik VM，前者是 IBM 公司的 Java 执行平台，而后者则是大名鼎鼎的 Android 平台的核心组成部分，但是注意 Dalvik VM 并不是一个 Java 虚拟机，它并没有遵循 Java 虚拟机规范，不能直接执行 Java 的 Class 文件，但是其执行的 dex 文件可以通过 class 文件转化而来，并且可以使用 Java 语法编写应用，可以直接使用大部分的 Java API。


### JVM 和 JDK 及 JRE 的关系

**JDK**  (Java Development Kit) 是 Java 语言的软件开发工具包(SDK)，是支持 Java 程序开发的最小环境。在 JDK 的安装目录下有一个 jre 目录，里面有两个文件夹 bin 和 lib，在这里可以认为 bin 里的就是 JVM，lib 中则是 JVM 工作所需要的类库，而 JVM 和 lib 合起来就称为 JRE。

**JRE**（Java Runtime Environment，Java 运行环境），包含 JVM 标准实现及 Java 核心类库，是支持 Java 程序运行的标准环境。由于 JRE 是运行环境，并不是一个开发环境，所以没有包含任何开发工具（如编译器和调试器）。

**JVM**（Java Virtual Machine，Java 虚拟机），JVM 可以理解为是一个虚拟出来的计算机，具备着计算机的基本运算方式，它主要负责将 Java 程序生成的字节码文件解释成具体系统平台上的机器指令。让具体平台如 windows 运行这些 Java 程序。

mac os 系统中运行 `/usr/libexec/java_home -V` 查看 JDK 的安装目录，可以看到：

![](https://jverson.oss-cn-beijing.aliyuncs.com/7d88556c2d44ee62c6583c9f9c847498.jpg)


其中最下面一行即当前使用的 JDK 安装目录，查看该目录下的文件会看到

![](https://jverson.oss-cn-beijing.aliyuncs.com/5816ce012c377eb008d7af5a1b25a195.jpg)


一张图概括三者之间的关系如下

![](https://jverson.oss-cn-beijing.aliyuncs.com/dd1f5d655f538858075253b9fe0feb33.jpg)

> JRE = JVM + libraries to run Java application.    
JDK = JRE + tools to develop Java Application.

在我们的具体开发当中，我们首先利用 IDE（例如 Eclipse）和 JDK（主要是其中的 Java API）开发属于我们自己的 Java 程序，然后通过 JDK 中的编译程序（javac）将我们的 Java 源码文件编译成 Java 字节码，在 JRE 上运行这些字节码，JVM 则负责解析这些字节码，映射到 CPU 指令集或 OS 的系统调用。

Java execution flow

![](https://jverson.oss-cn-beijing.aliyuncs.com/bb6da1ba261fcbc72f91433610b7dfb7.jpg)



### JVM 和 Tomcat 的关系

Tomcat 是用 Java 语言开发的一个 Servlet 容器（一种 Web 服务器），所以简单来讲 Tomcat 就是一个 Java 应用（和我们自己用 Java 写的 hello world 并无本质区别）。那只要是 Java 程序就都需要跑在 JVM 中，因此在启动 Tomcat 之前必须配置好 JRE。



### Java 为什么具有跨平台的特性

Java 语言的一个非常重要的特点就是与平台的无关性，这个特性也要归功于 JVM。一般的高级语言如果要在不同的平台上运行，至少需要编译成不同的目标代码（这时因为不同的平台具有不同的指令集和数据结构）。而 Java 语言使用 JVM 屏蔽了与具体平台相关的信息，只需要将源码通过编译程序编译成在 JVM 上运行的目标代码（字节码），就可以在多种平台上不加修改地运行。而 JVM 在执行字节码时，把字节码解释成具体平台上的机器指令执行（不同平台上的 JVM 解释器肯定不同）。

从 DOS 到 Window8，从 Unix 到 Ubuntu 和 CentOS，还有 MAC OS 等等，不同的操作系统指令集以及数据结构都有着差异，而 JVM 通过在操作系统上建立虚拟机，自己定义出来的一套统一的数据结构和操作指令，把同一套语言翻译给各大主流的操作系统，实现了跨平台运行，可以说 JVM 是 Java 的核心，是 Java 可以一次编译到处运行的本质所在。
