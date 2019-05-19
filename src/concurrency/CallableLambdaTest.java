package concurrency;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

public class CallableLambdaTest {
    public static void main(String[] args) {
        FutureTask<Integer> futureTask = new FutureTask<>(() -> {
            int sum = 0;
            for (int i = 0; i <= 5; i++) {
                sum += i;
            }
            return sum;
        });
        new Thread(futureTask, "t1").start();
        try {
            System.out.println(futureTask.get());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
    }
}
