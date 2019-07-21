# 重试工具：Guava Retryer
---

业务中经常会遇到失败重试的情况，有时候甚至要求能够实现一定的重试策略，比如说重试 3 次，每次重试间隔时间递增等等，其实这些都不需要我们自己去实现，Guava 作为 Java 开发中实用的工具类也早已为我们提供了优秀的重试工具 Retryer。

> The guava-retrying module provides a general purpose method for retrying arbitrary Java code with specific stop, retry, and exception handling capabilities that are enhanced by Guava's predicate matching.

首先需要单独引入该工具类

```xml
<dependency>
      <groupId>com.github.rholder</groupId>
      <artifactId>guava-retrying</artifactId>
      <version>2.0.0</version>
</dependency>
```


```java
public class RetryTest {

	private static Integer num = 0;
	
	public static void main(String[] args) {
		Boolean result = false;
		Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()  
		        .retryIfException() // 异常时重试
		        .retryIfExceptionOfType(IllegalStateException.class) // 特定异常时才重试
		        .retryIfRuntimeException // 只会在抛 runtime 异常的时候才重试，checked 异常和 error 都不重试。
		        .retryIfResult(Predicates.equalTo(false)) // 返回结果 false 也重试
		        .withStopStrategy(StopStrategies.stopAfterAttempt(3)) // 重试次数
//		        .withWaitStrategy(WaitStrategies.fixedWait(2, TimeUnit.SECONDS))
		        .withWaitStrategy(WaitStrategies.incrementingWait(1, TimeUnit.SECONDS, 1, TimeUnit.SECONDS))  // 重试策略
		        .build(); 
		try {System.out.println("aaaaaaaaaaa");
		    result = retryer.call(getTokenUserCall);  
		} catch (Exception e) {System.err.println("still failed after retry.");} 
		System.out.println(result);
	}
	
	
	private static Callable<Boolean> getTokenUserCall = new Callable<Boolean>() {

		@Override
		public Boolean call() throws Exception {
			num++;
			System.out.println("calling..........num=" + num);
			if (num==4) {return true;}
			return false;
		}
		
	};	
	
}
```

Guava retryer 在支持重试次数和重试频度控制基础上，能够兼容支持多个异常或者自定义实体对象的重试源定义，让重试功能有更多的灵活性。Guava Retryer 也是线程安全的，入口调用逻辑采用的是 Java.util.concurrent.Callable 的 call 方法。

另外当发生重试之后，假如我们需要做一些额外的处理动作，比如发个告警邮件啥的，那么可以使用 RetryListener。每次重试之后，guava-retrying 会自动回调我们注册的监听。可以注册多个 RetryListener，会按照注册顺序依次调用。

监听类实现 RetryListener 接口

```Java
public class MyRetryListener<Boolean> implements RetryListener {   
    @Override  
    public <Boolean> void onRetry(Attempt<Boolean> attempt) {   
        // 第几次重试,(注意:第一次重试其实是第一次调用)  
        System.out.print("[retry]time=" + attempt.getAttemptNumber());   
        // 距离第一次重试的延迟  
        System.out.print(",delay=" + attempt.getDelaySinceFirstAttempt());   
        // 重试结果: 是异常终止, 还是正常返回  
        System.out.print(",hasException=" + attempt.hasException());  
        System.out.print(",hasResult=" + attempt.hasResult());   
        // 是什么原因导致异常  
        if (attempt.hasException()) {  
            System.out.print(",causeBy=" + attempt.getExceptionCause().toString());  
        } else {  
            // 正常返回时的结果  
            System.out.print(",result=" + attempt.getResult());  
        }   
        // bad practice: 增加了额外的异常处理代码  
        try {  
            Boolean result = attempt.get();  
            System.out.print(",rude get=" + result);  
        } catch (ExecutionException e) {  
            System.err.println("this attempt produce exception." + e.getCause().toString());  
        }   
    }  
}
```

然后在 RetryerBuilder 中注册监听器即可

```Java
.withRetryListener(new MyRetryListener<>()) 
```


更多姿势的其它用法可以直接去参考官方文档 [guava-retrying](https://github.com/rholder/guava-retrying)

