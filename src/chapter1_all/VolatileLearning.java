/**
 * thinking-in-java
 * this is test code for learning java
 */
package chapter1_all;

/**
 * @author Jverson
 *
 */
public class VolatileLearning {

	public static int count = 0;

    public static void inc() {

        //这里延迟1毫秒，使得结果明显
        try {
            Thread.sleep(3);
        } catch (InterruptedException e) {
        }

        count++;
    }

    public static void main(String[] args) {

        //同时启动1000个线程，去进行i++计算，看看实际结果

        for (int i = 0; i < 100; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                	VolatileLearning.inc();
                }
            }).start();
        }
        
        System.out.println("运行结果:Counter.count=" + count);
    }
	
}
