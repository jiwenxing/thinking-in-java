package concurrency;

import java.util.concurrent.atomic.AtomicInteger;

public class ThreadTest extends Thread {
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
    }

    public static void main(String[] args) {
        ThreadTest threadTest = new ThreadTest();
        threadTest.start();

        AtomicInteger
    }
}
