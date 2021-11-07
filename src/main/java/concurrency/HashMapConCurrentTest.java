package concurrency;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * jdk7 中下面程序会出现死循环
 */
public class HashMapConCurrentTest extends Thread{

    static HashMap<Integer,Integer> map = new HashMap<>(2);
    static AtomicInteger ai = new AtomicInteger();

    @Override
    public void run() {
        while (ai.get() < 100000){
            map.put(ai.get(), ai.get());
            System.out.println(map.get(ai.get()));
            ai.incrementAndGet();
        }
    }

    public static void main(String[] args) {
        for (int i = 0; i < 5; i++) {
            new HashMapConCurrentTest().start();
        }
    }
}
