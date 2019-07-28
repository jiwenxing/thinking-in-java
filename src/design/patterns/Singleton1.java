package design.patterns;

/**
 * 单例模式的懒汉线程安全实现
 */
public class Singleton1 {
    private Singleton1() {
    }
    private static Singleton1 singleton1;
    public synchronized static Singleton1 getInstance(){
        if (singleton1 == null){
            singleton1 = new Singleton1();
        }
        return singleton1;
    }
}
