# Jstack 工具使用
---

```bash
top # 查看cpu占用情况，得到异常进程id
top -Hp <pid> # 看一下该进程中哪些线程占用较高，得到线程id
printf “0x%x\n” <tid> # 得到线程的十六进制表达
jstack -l <pid> | grep '0x3be91' -A20 # 查看对应线程堆栈，可能需要加 -F 参数

```

jstack 可以用来跟踪线程的堆栈，通过生成一个当前的 jvm 堆栈快照，分析堆栈快照可以定位问题的根源。

其用法可以通过 jstack -help 来查看，常用的是 jstack -l <pid>

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/80c80a07-7c73-4e22-9e60-8e691814f5c8)

`top` 命令查看 cpu 占用情况，一般都是 java 进程占用最高，得到 java 进程 id ： 245332
![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/b83e2b36-01ea-47cb-9bcd-da50b9498a38)

`top -Hp <pid>` 看一下 java 进程中哪些线程占用较高。其中 -p 表示查看指定进程的 cpu 占用情况，-H 表示该进程下线程 cpu 占用倒序展示

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/20543180-bb5e-4de8-831d-cb5951822d04)

这是比如说第一个线程 245393 占用 cpu 过多，我们就可以打印它的线程堆栈来分析以下，首先需要将线程 id 转 16 进制，因为在线程堆栈文件中是 16 进制打印的。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/0f6001e3-d092-43bb-9732-d71a872cadc1)

然后对进程进行 jstack 并 grep 出这个线程的相关堆栈信息 `jstack -l 245332 | grep '0x3be91' -A20`

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/00d8c524-822b-49b3-a182-987dbad6a0b8)

可以看到占用 cpu 最高的线程是异步上报特征

