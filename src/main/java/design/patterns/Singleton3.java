package design.patterns;

/**
 * 单例模式的饿汉方式实现
 */
public class Singleton3 {
    private static Singleton3 singleton3 = new Singleton3();
    private Singleton3() {
    }
    public static Singleton3 getInstance() {
        return singleton3;
    }
}
