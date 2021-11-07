package concurrency;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CallableTest implements Callable<Integer> {
    @Override
    public Integer call() {
        int sum = 0;
        System.out.println(Thread.currentThread().getName() + " 开始执行!");
        for (int i = 0; i <= 5; i++) {
            sum += i;
        }
        System.out.println(Thread.currentThread().getName() + " 执行完毕! sum=" + sum);
        return sum;
    }
    public static void main(String[] args) {
        CallableTest callableTest = new CallableTest();
        FutureTask<Integer> futureTask = new FutureTask<>(callableTest);
        Thread thread = new Thread(futureTask, "t1");
        thread.start();
        try {
            System.out.println(futureTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
