package concurrency;

public class RunnableTest implements Runnable{
    @Override
    public void run() {
        System.out.println(Thread.currentThread().getName());
    }
    public static void main(String[] args) {
        RunnableTest target = new RunnableTest();
        Thread t1 = new Thread(target, "t1");
        Thread t2 = new Thread(target, "t2");
        t1.start();
        t2.start();
    }
}
