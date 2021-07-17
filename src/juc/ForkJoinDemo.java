package juc;

import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class ForkJoinDemo {

    public static void main(String[] args) {
        Executors.newWorkStealingPool();

        ForkJoinPool fjp = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
        Fibonacci fib = new Fibonacci(5);
        Integer result = (int)fjp.invoke(fib);
        System.out.println(result);
    }

    static class Fibonacci extends RecursiveTask {
        final int n;
        Fibonacci(int n) {
            this.n = n;
        }
        @Override
        protected Integer compute() {
            if (n <= 1) {
                return n;
            }
            Fibonacci f1 = new Fibonacci(n - 1);
            f1.fork();
            Fibonacci f2 = new Fibonacci(n - 2);
            return f2.compute() + (int)f1.join();
        }
    }

}
