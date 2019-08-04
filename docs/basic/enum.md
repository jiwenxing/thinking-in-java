# 枚举类详解
---

之前在 [单例模式](https://jverson.com/thinking-in-java/design/1-singleton.html) 这一篇中有提到可以使用枚举实现单例，并简单介绍了一下原理，这里讲讲平时在开发中使用枚举的一些常见姿势，学以致用！

![](https://jverson.oss-cn-beijing.aliyuncs.com/019405c13fea520514c24d2ebd9960f7.jpg)


## 关于枚举的一些知识点

枚举 enum 关键字是在 Java 5 中引入，是隐式继承自 `java.lang.Enum` 的一种特殊的类，通常被用于常量定义，相对于 `public static final` 这种方式，使用枚举常量可以使代码更易读、支持编译时类型检测避免非法参数等好处，此外枚举类型还自带一些有用的方法可直接使用。下面是枚举类型的一些特点：

- 所有的枚举类都隐式的继承了 Enum 抽象类，因此无法再继承其它类，但是可以实现接口；但是你自己却不能显示的定义一个继承了 Enum 的类，Javac 编译器禁止这种行为

- 枚举类是类型安全的，你不能给一个指向枚举的引用赋值其它任何类型的变量

- 可以通过 `MyEnum.values()` 获取到所有该枚举类型的实例数组，其顺序与定义的顺序保持一致；ordinal() 方法则返回枚举在该数组中的下标

- Enum 常量隐式的是 static 和 final 的，这个可以从之前的字节码解析文件中看到

- Enum 可以直接使用 `==` 比较，这时因为 enum types ensure that only one instance of the constants exist in the JVM

- 可以用于 switch 语句

- Enum 实例只能从内部创建而不能通过 new 创建，因为其构造函数是私有的

- Enum 实例会在其第一次被调用/引用的时候创建，这也是使用枚举实现单例模式懒加载的原理


## 枚举用于定义常量

```Java
public enum GenderEnum {
    MALE(1, "男"), FEMALE(2, "女");

    private String name;
    private int code;

    private static final Map<Integer, GenderEnum> GENDER_ENUM_MAP = new HashMap<>();

    static {
        Arrays.stream(GenderEnum.values()).forEach(genderEnum -> GENDER_ENUM_MAP.put(genderEnum.code, genderEnum));
    }

    GenderEnum(int code, String name) {
        this.name = name;
        this.code = code;
    }

    public static GenderEnum getByCode(int code) {
        return GENDER_ENUM_MAP.get(code);
    }

    public static String getNameByCode(int code) {
        return GENDER_ENUM_MAP.containsKey(code) ? GENDER_ENUM_MAP.get(code).name : null;
    }
}
```

## 支持定义方法，同时支持方法的重写

```Java
public enum ColorEnum {
    RED {
        @Override
        public void paint() {
            System.out.println("red");
        }
    },
    BLUE {
        @Override
        public void paint() {
            System.out.println("blue");
        }
    },
    DEFAULT;

    public void paint() {
        System.out.println("default");
    }

    public static void main(String[] args) {
        DEFAULT.paint();
        RED.paint();
        BLUE.paint();

    }
}
```

通过 javac 编译该枚举可以得到三个 class 文件：`ColorEnum$1.class ColorEnum$2.class ColorEnum.class`。再次执行 `javap -v ColorEnum$1.class` 可以看到一行 `public class ColorEnum extends java.lang.Enum<ColorEnum>`，可知 RED 和 BLUE 其实是 ColorEnum 类的匿名内部类，他们都继承了 ColorEnum 类，因此是子类对父类方法的重写。但是注意 Enum 类都是 final 的，按理说不能够被继承，从现象来看可能是编译器对内部类继承 final 类的限制有所不同。


## 通过实现接口对枚举进行分组

我们知道枚举类不能被继承，因为它是 final 的，那么枚举类能继承其它类吗？答案也是不能，因为其已经隐式的继承了 Enum 抽象类，但是这对枚举类实现接口没有任何限制，因此可能通过实现接口对同一类枚举进行分组，同时用接口去规范枚举类里面的方法。

```Java
public interface Food {
    enum Appetizer implements Food{
        SALAD, SOUP, SPRING_ROLLS
    }
    enum Coffee implements Food{
        BLACK_COFFEE, TEA, LATTE
    }
    enum Dessert implements Food{
        FRUIT, GELATO, LASAGNE
    }
}
```

## EnumSet

> The EnumSet is a specialized Set implementation meant to be used with Enum types.

EnumSet 是专门为枚举常量设计的一种集合实现，针对枚举类 EnumSet 要比 HashSet 的效率更高，因为它底层使用了 `Bit Vector Representation` 的结构。EnumSet 是一个抽象类，有 2 个默认实现 `RegularEnumSet` 和 `JumboEnumSet`，内部会根据常量的数量在两者之间自动转换。


## EnumMap

如果 Map 的 Key 类型是枚举类型的话， 相比于 HashMap，更推荐使用 EnumMap。EnumMap 的 key 只能是 Enum 类，底层实现是基于数组的。一般用于保存一个枚举类对应的额外的业务信息。

## 使用枚举实现单例

参考之前的文章 [枚举方式实现单例](https://jverson.com/thinking-in-java/design/1-singleton.html#%E6%9E%9A%E4%B8%BE%E6%96%B9%E5%BC%8F)

## 使用枚举实现策略模式

传统的策略模式是定义一个接口，然后不同的策略是不同的实现类，这就意味着每新增一种策略就需要新增一个实现类。如果通过枚举类实现策略模式的话则只需要增加一个枚举常量即可。

```Java
public enum PizzaDeliveryStrategy {
    EXPRESS {
        @Override
        public void deliver(Pizza pz) {
            System.out.println("Pizza will be delivered in express mode");
        }
    },
    NORMAL {
        @Override
        public void deliver(Pizza pz) {
            System.out.println("Pizza will be delivered in normal mode");
        }
    };
 
    public abstract void deliver(Pizza pz);
}

//在 Pizza class 中添加下面的方法

public void deliver() {
    if (isDeliverable()) {
        PizzaDeliverySystemConfiguration.getInstance().getDeliveryStrategy()
          .deliver(this);
        this.setStatus(PizzaStatus.DELIVERED);
    }
}

//测试

@Test
public void givenPizaOrder_whenDelivered_thenPizzaGetsDeliveredAndStatusChanges() {
    Pizza pz = new Pizza();
    pz.setStatus(Pizza.PizzaStatus.READY);
    pz.deliver();
    assertTrue(pz.getStatus() == Pizza.PizzaStatus.DELIVERED);
}

```



## 参考

- [A Guide to Java Enums](https://www.baeldung.com/a-guide-to-java-enums)
- [Beginner’s Guide to Java eNum – Why and for What should I use Enum? Java Enum Examples](https://crunchify.com/why-and-for-what-should-i-use-enum-java-enum-examples/)
- [Java枚举类，字节码层面的深入浅出](https://blog.csdn.net/grandachn/article/details/83183254)