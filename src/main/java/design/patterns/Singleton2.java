package design.patterns;

/**
 * 单例模式的双重锁方式实现
 */
public class Singleton2 {
    private static volatile Singleton2 singleton2;
    private Singleton2() {
    }
    public static Singleton2 getInstance() {
        if (singleton2 == null) {
            synchronized (Singleton2.class) {
                if (singleton2 == null){
                    singleton2 = new Singleton2();
                }
            }
        }
        return singleton2;
    }
}
