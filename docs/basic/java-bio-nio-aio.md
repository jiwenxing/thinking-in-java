# Java BIO（同步阻塞IO）vs NIO(同步非阻塞IO) vs AIO(异步非阻塞IO)
---

![](https://jverson.oss-cn-beijing.aliyuncs.com/854642bacb4453597fbba0f37a2279a5.jpg)

- BIO：同步阻塞式IO，服务器实现模式为一个**连接**一个线程，即客户端有连接请求时服务器端就需要启动一个线程进行处理，如果这个连接不做任何事情会造成不必要的线程开销，当然可以通过线程池机制改善。（上厕所没坑，你会主动地观察哪个坑的人先走，先走的坑你就先占住位置）

- NIO：同步非阻塞式IO，JDK1.4 引入。服务器实现模式为一个**请求**一个线程，即客户端发送的连接请求都会注册到多路复用器上，多路复用器轮询到连接有I/O请求时才启动一个线程进行处理。（有专人检测坑位状态，有空闲的就叫一个人进来） 

- AIO(NIO 2.0)：异步非阻塞式IO，JDK1.7 推出。服务器实现模式为一个**有效请求**一个线程，客户端的I/O请求都是由OS先完成了再通知服务器应用去启动线程进行处理。 （在厕所外面等坑内的人好之后出来提醒自己）


三者的处理流程大致如下图示意

![](https://jverson.oss-cn-beijing.aliyuncs.com/1d6a0e0fdce797b757a25a47135bebb3.jpg)


由于 AIO 概念上也属于 NIO，号称 NIO2，因此下面主要介绍传统的 BIO 与 NIO 之间的差异。另外目前来说 AIO 的应用还不是很广泛，Netty 据说尝试使用过 AIO 但目前依然是基于 NIO 实现。因此 AIO 这里将不过多介绍。



## IO 与 NIO

![](https://jverson.oss-cn-beijing.aliyuncs.com/9ef57df5f11586ee5d305b361ff052a3.jpg)


### 面向流 vs 面向缓冲

从上图可以看到 Java IO 和 NIO 之间第一个最大的区别是，IO 是面向流的，NIO 是面向缓冲区的。

所谓面向流指的是每次从流中读一个或多个字节，直至读取所有字节，它们没有被缓存在任何地方。而面向缓冲区则是将数据读取到一个它稍后处理的缓冲区，需要时可在缓冲区中前后移动，这就增加了处理过程中的灵活性。

### 阻塞 vs 非阻塞

Java IO 的各种流是阻塞的。这意味着，当一个线程调用 read() 或 write() 时，该线程被阻塞，直到有一些数据被读取，或数据完全写入。该线程在此期间不能再干任何事情了。

Java NIO 的非阻塞模式，它的读和写都是非阻塞的，即如果线程在某个通道发起读请求，他可以获取当前可用数据，如果没有可用数据，线程不会一直等着，而是去处理其他通道的读写请求，直到当前通道有可用数据之前。非阻塞写同理，因此在 NIO 中一个线程可以管理多个输入和输出通道（channel）。

### 选择器 Selectors

Java NIO 的选择器允许一个单独的线程来监视多个输入通道，你可以注册多个通道使用一个选择器，然后使用一个单独的线程来“选择”通道：这些通道里已经有可以处理的输入，或者选择已准备写入的通道。这种选择机制，使得一个单独的线程很容易来管理多个通道。

如果应用程序有多个通道(连接)打开，但每个连接的流量都很低，则可考虑使用它。 例如：在聊天服务器中。

## NIO 的特点和优势

NIO 本身是基于事件驱动思想来完成的，其主要想解决的是 BIO 的大并发问题： 在使用同步 I/O 的网络应用中，如果要同时处理多个客户端请求，或是在客户端要同时和多个服务器进行通讯，就必须使用多线程来处理。也就是说，将每一个客户端请求分配给一个线程来单独处理。这样做虽然可以达到我们的要求，但同时又会带来另外一个问题。由于每创建一个线程，就要为这个线程分配一定的内存空间（也叫工作存储器），而且操作系统本身也对线程的总数有一定的限制。如果客户端的请求过多，服务端程序可能会因为不堪重负而拒绝客户端的请求，甚至服务器可能会因此而瘫痪。

NIO 基于 Reactor，当 socket 有流可读或可写入 socket 时，操作系统会相应的通知引用程序进行处理，应用再将流读取到缓冲区或写入操作系统。也就是说，这个时候，已经不是一个连接就要对应一个处理线程了，而是有效的请求，对应一个线程，当连接没有数据时，是没有工作线程来处理的。

BIO 与 NIO 一个比较重要的不同，是我们使用 BIO 的时候往往会引入多线程，每个连接一个单独的线程；而 NIO 则是使用单线程或者只使用少量的多线程，每个连接共用一个线程。

![](https://jverson.oss-cn-beijing.aliyuncs.com/b4ef0168d1b34736a868d776651558b5.jpg)


## NIO 的核心组件

- Channels 参考 [Java NIO 之 Channel（通道）](https://mp.weixin.qq.com/s?__biz=Mzg2OTA0Njk0OA==&mid=2247484949&amp;idx=1&amp;sn=a8a9c3fcf736efa88917e8c32db35758&source=41#wechat_redirect)
  - DatagramChannel
  - SocketChannel
  - FileChannel
  - ServerSocketChannel
- Buffers，参考 [Java NIO 之 Buffer(缓冲区)](https://mp.weixin.qq.com/s?__biz=Mzg2OTA0Njk0OA==&mid=2247484950&amp;idx=1&amp;sn=796cd8c9141268e3683bf6b22736858e&source=41#wechat_redirect)
  - ByteBuffer
  - CharBuffer
  - ShortBuffer
  - IntBuffer
  - FloatBuffer
  - DoubleBuffer
  - LongBuffer
- Selectors 参考 [Java NIO之Selector（选择器）](https://mp.weixin.qq.com/s?__biz=Mzg2OTA0Njk0OA==&mid=2247484948&amp;idx=1&amp;sn=c077462dfeca9abacc43c22304c804cf&source=41#wechat_redirect)


## NIO 和 IO 的选择

对于 IO 或 NIO 的选择，也需要看具体的场景，并不是说 NIO 直接取代了传统 IO。NIO 可让您只使用一个（或几个）单线程管理多个通道（网络连接或文件），但付出的代价是解析数据可能会比从一个阻塞流中读取数据更复杂。

如果需要管理同时打开的成千上万个连接，这些连接每次只是发送少量的数据，例如聊天服务器，实现 NIO 的服务器可能是一个优势。同样，如果你需要维持许多打开的连接到其他计算机上，如P2P网络中，使用一个单独的线程来管理你所有出站连接，可能是一个优势。

如果你有少量的连接使用非常高的带宽，一次发送大量的数据，也许典型的 IO 服务器实现可能非常契合。

## 常见的问题

### 字节流与字符流有什么区别

由于对于字节和字符两种操作的需求比较广泛，所以 Java 专门提供了字符流与字节流相关IO类。对于程序运行的底层设备来说永远都只接受字节数据，所以当我们往设备写数据时无论是字节还是字符最终都是写的字节流。字符流是字节流的包装类，所以当我们将字符流向字节流转换时要注意编码问题（因为字符串转成字节数组的实质是转成该字符串的某种字节编码）。字符流和字节流的使用非常相似，但是实际上字节流的操作不会经过缓冲区（内存）而是直接操作文本本身的，而字符流的操作会先经过缓冲区（内存）然后通过缓冲区再操作文件。

### 字节流和字符流如何选择？

如果对于操作需要通过 IO 在内存中频繁处理字符串的情况使用字符流会好些，因为字符流具备缓冲区，提高了性能。其它情况选择字节流即可。


## 总结

IO 这一块平时接触较少，但其实应用很广泛，后期需要找时间专题研究一下，包括 Netty，下面也找了一些资料可以参考。


## 参考

- [漫话：如何给女朋友解释什么是Linux的五种IO模型？](https://mp.weixin.qq.com/s?__biz=Mzg3MjA4MTExMw==&mid=2247484746&idx=1&sn=c0a7f9129d780786cabfcac0a8aa6bb7&source=41#wechat_redirect)
-[BIO,NIO,AIO 总结](https://github.com/Snailclimb/JavaGuide/blob/master/docs/java/BIO-NIO-AIO.md)
- [Java NIO浅析](https://zhuanlan.zhihu.com/p/23488863)
- [谈谈你对 IO 流和 NIO 的理解](https://mp.weixin.qq.com/s/V4oFaI-LCMw_Mw_3D3akng)
- [Java NIO 之 Buffer(缓冲区)](https://mp.weixin.qq.com/s?__biz=Mzg2OTA0Njk0OA==&mid=2247484950&amp;idx=1&amp;sn=796cd8c9141268e3683bf6b22736858e&source=41#wechat_redirect)