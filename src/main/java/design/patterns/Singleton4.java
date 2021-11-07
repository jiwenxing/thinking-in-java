package design.patterns;

/**
 * 单例模式的静态内部类实现方式
 */
public class Singleton4 {
    private Singleton4() {
    }
    private static class InnerClass {
        private static final Singleton4 singleton4 = new Singleton4();
    }
    public static Singleton4 getInstance() {
        return InnerClass.singleton4;
    }
}
