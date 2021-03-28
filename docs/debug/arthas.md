# Arthas
---

Arthas 是Alibaba开源的Java诊断工具，深受开发者喜爱。在线排查问题，无需重启；动态跟踪Java代码；实时监控JVM状态。

Arthas 支持JDK 6+，支持Linux/Mac/Windows，采用命令行交互模式，同时提供丰富的 Tab 自动补全功能，进一步方便进行问题的定位和诊断。

Github: https://github.com/alibaba/arthas

文档: https://arthas.aliyun.com/doc/

强烈建议自己动手把这些都实践一遍（可以直接在 web 端搞）：https://start.aliyun.com/course?spm=a2ck6.17690074.0.0.46193150C9E1PL&id=PaiFAkJM

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/284a00f5-7329-4cff-bc83-aa10e226c7c5)

## 安装方式

可以现场时使用官方命令进行安装，任意目录执行`curl -O https://arthas.aliyun.com/arthas-boot.jar`，如果执行成功则直接运行`java -jar arthas-boot.jar`

有时候可能会因为内网环境导致hulk容器可能无法通过github或aliyun下载arthas的一些必要文件，所以事先将需要的文件下载打包放在了sftp上

0. mkdir /home/sankuai/.arthas/ 创建目录, cd /home/sankuai/.arthas/ 进入到该目录下

1. sftp jiwenxing@jumper.sankuai.com 通过 sftp 下载 arthas.tar 到当前目录 /home/sankuai/.arthas/

2. tar xzvf arthas.tar 解压

3. java -jar lib/3.5.0/arthas/arthas-boot.jar 运行目录下的arthas-boot.jar文件即可

详细使用方法见 arthas 官方使用手册：https://arthas.aliyun.com/doc/install-detail.html#id1

退出arthas，用quit或者exit命令，Attach到目标进程上的arthas还会继续运行，端口会保持开放，下次连接时可以直接连接上。如果想完全退出arthas，可以执行stop命令

## 常用功能

### 查看线程堆栈

dashboard 可以显示当前 attach 的 java 进程下的线程信息，默认会按照 cpu 使用倒序排列，很容易看到那个线程占用 cpu 较多（功能类似于 top -Hp pid）

![](https://jverson.oss-cn-beijing.aliyuncs.com/90e74213c8a1b3b3bef64d5f1af13653.jpg)


得到线程id后可以查看线程堆栈

![](https://jverson.oss-cn-beijing.aliyuncs.com/97e379a0762677288ce78fc2ac7c5ad9.jpg)


查看所有线程信息 `thread`

查看具体线程的栈，例如查看线程ID 16的栈: `thread 16`

查看CPU使用率top n线程的栈: `thread -n 3`

查看5秒内的CPU使用率top n线程栈: `thread -n 3 -i 5000`

查找线程是否有阻塞: `thread -b`

### 查看方法入参和返回结果

例如有这么一段代码，不通过 debug 查看方法的出入参信息

```java
public String getNameById(int cityId, boolean isOversea) {
    String cityName = StringUtils.EMPTY;
    CityInfo cityInfo = null;
    if (isOversea) {
        cityInfo = this.overseaCityId2Info.get(cityId);
    } else {
        cityInfo = this.domesticCityId2Info.get(cityId);
    }
    if (null != cityInfo) {
        cityName = cityInfo.getName();
    }
    return cityName;
}
```

通过 watch 命令如下，其中 -x 表示遍历深度，可以调整来打印具体的参数和结果内容，默认值是1。-n 2 表示只执行两次，不设置则会一直执行

> watch com.meituan.dataapp.search.poi.rerank.hotel.service.HotelCityManager getNameById "{params,returnObj}" -x 2 -n 2

![](https://jverson.oss-cn-beijing.aliyuncs.com/2e3615382552e53c74a6327dbcfdc3a9.jpg)


注意上面的命令如果不加 -x 2 的话打印出来是这样的，入参的两个值只打印了一个数组，并不会展开打印导致我们看不到入参值

![](https://jverson.oss-cn-beijing.aliyuncs.com/361bbdbe0445f0f187325b2113849b65.jpg)


watch 功能很强大，命令定义了4个观察事件点，即 -b 方法调用前，-e 方法异常后，-s 方法返回后，-f 方法结束后（默认），另外还可以加一些条件

```bash
# "params[0]<120" 第一个入参值小于120；"params[0]==402" 等于
[arthas@323341]$ watch com.meituan.dataapp.search.poi.rerank.hotel.service.HotelCityManager getNameById "{params[0],returnObj}" "params[0]<120" -x 2 -n 2
Press Q or Ctrl+C to abort.
Affect(class count: 1 , method count: 1) cost in 221 ms, listenerId: 11
method=com.meituan.dataapp.search.poi.rerank.hotel.service.HotelCityManager.getNameById location=AtExit
ts=2021-03-20 21:30:15; [cost=0.020612ms] result=@ArrayList[
    @Integer[118],
    @String[香港],
]

# 方法抛出异常时打印，注意表示异常信息的变量是throwExp
$ watch demo.MathGame primeFactors "{params[0],throwExp}" -e -x 2
Press Ctrl+C to abort.
Affect(class-cnt:1 , method-cnt:1) cost in 62 ms.
ts=2018-12-03 19:38:00; [cost=1.414993ms] result=@ArrayList[
    @Integer[-1120397038],
    java.lang.IllegalArgumentException: number is: -1120397038, need >= 2
	at demo.MathGame.primeFactors(MathGame.java:46)
	at demo.MathGame.run(MathGame.java:24)
	at demo.MathGame.main(MathGame.java:16)
,
]

# 如果耗时大于200ms
$ watch demo.MathGame primeFactors '{params, returnObj}' '#cost>200' -x 2
Press Ctrl+C to abort.
Affect(class-cnt:1 , method-cnt:1) cost in 66 ms.
ts=2018-12-03 19:40:28; [cost=2112.168897ms] result=@ArrayList[
    @Object[][
        @Integer[1],
    ],
    @ArrayList[
        @Integer[5],
        @Integer[428379493],
    ],
]

```

### 反编译线上代码

jad package.classname(类的全限定名)， 例如 jad com.meituan.dataapp.search.poi.rerank.hotel.postrank.BlackListPostRanker 即可将其反编译打印出来，当你怀疑线上代码和本地不一致时这样可以很方便的反编译出来进行对比

![](https://jverson.oss-cn-beijing.aliyuncs.com/5b50afe0713b63c728e2dfcbe3fd4b9a.jpg)


sc 查看JVM已加载的类信息

```bash
[arthas@1799]$ sc -d *MathGame # -d 表示打印详细信息
 class-info        demo.MathGame                                                                                   
 code-source       /home/shell/arthas-demo.jar                                                                     
 name              demo.MathGame                                                                                   
 isInterface       false                                                                                           
 isAnnotation      false                                                                                           
 isEnum            false                                                                                           
 isAnonymousClass  false                                                                                           
 isArray           false                                                                                           
 isLocalClass      false                                                                                           
 isMemberClass     false                                                                                           
 isPrimitive       false                                                                                           
 isSynthetic       false                                                                                           
 simple-name       MathGame                                                                                        
 modifier          public                                                                                          
 annotation                                                                                                        
 interfaces                                                                                                        
 super-class       +-java.lang.Object                                                                              
 class-loader      +-sun.misc.Launcher$AppClassLoader@1b6d3586                                                     
                     +-sun.misc.Launcher$ExtClassLoader@7237d833                                                   
 classLoaderHash   1b6d3586  
 ```          

### 热更新线上代码

可以通过这个示例 https://start.aliyun.com/handson/PaiFAkJM/case-jad-mc-redefine-cn 进行熟悉，很简单

可以通过 jad/mc/redefine 命令组合实现动态更新代码的功能。

- jad 反编译 class 为 java 文件后对其进行修改 `jad --source-only com.example.demo.arthas.user.UserController > /tmp/UserController.java`
- sc 查找加载 UserController（热更新类） 的 ClassLoader `sc -d *UserController | grep classLoaderHash`  输出 classLoaderHash 1be6f5c3
- 使用mc(Memory Compiler)命令来编译，并且通过-c或者--classLoaderClass参数指定ClassLoader, `mc --classLoaderClass org.springframework.boot.loader.LaunchedURLClassLoader /tmp/UserController.java -d /tmp 或 mc -c 1be6f5c3 /tmp/UserController.java -d /tmp` 或 `mc -c 1be6f5c3 /tmp/UserController.java -d /tmp`

- 最后再使用redefine命令重新加载新编译好的UserController.class即可 `redefine /tmp/com/example/demo/arthas/user/UserController.class`

### Arthas后台异步任务

当线上出现偶发的问题，比如需要watch某个条件，而这个条件一天可能才会出现一次时，异步后台任务就派上用场了，详情请参考这里