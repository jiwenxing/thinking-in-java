package others;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadLocalTest {
    private static final AtomicInteger nextId = new AtomicInteger(1);
    private static final ThreadLocal<Integer> threadId = ThreadLocal.withInitial(() -> nextId.getAndIncrement());
    public static void main(String[] args) {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 10; i++) {
            executorService.submit(() -> System.out.println(Thread.currentThread().getName() + ": " + threadId.get()));
        }
        executorService.shutdown();
    }
}
