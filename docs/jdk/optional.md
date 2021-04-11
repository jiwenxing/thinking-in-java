# Optional 的使用
---

ref：[Guide To Java 8 Optional](https://www.baeldung.com/java-optional)

Java8 中带来的 Optional 对象可以让我们在代码中减少很多判空的 if 代码，看上去更加优雅，功能也很强大

# 一些基本用法

```Java
public class TempTest {
    @Test
    public void optionalTest() {
        Optional<String> empty = Optional.empty(); // 创建空 optional 对象
        assertFalse(empty.isPresent());

        String name = "baeldung";
        Optional<String> opt = Optional.of(name); // 注意这里 name 不能为 null
        assertTrue(opt.isPresent());

        String name2 = null;
        Optional<String> opt2 = Optional.ofNullable(name2); // 如果可能为 null，则需要使用 ofNullable
        assertFalse(opt2.isPresent());


        Optional<String> opt3 = Optional.of("Baeldung");
        assertTrue(opt3.isEmpty()); // isEmpty（Java11 才有） 和 isPresent 正好是相反的功能
        assertFalse(opt3.isPresent());

        // 之前总是要判空
        if(name != null) {
            System.out.println(name.length());
        }
        // Optional 一行搞定. The ifPresent() method enables us to run some code on the wrapped value if it's found to be non-null.
        Optional.ofNullable(name).ifPresent(s -> System.out.println(s.length()));

        // 注意 orElse 和 orElseGet 的区别，orElseGet 的参数是一个 supplier functional interface。
        String nullName = null;
        name = Optional.ofNullable(nullName).orElse("john"); // orElse taking a value to return if the Optional value is not present,
        assertEquals("john", name);
        name = Optional.ofNullable(nullName).orElseGet(() -> "john"); // orElseGet takes a supplier functional interface, which is invoked and returns the value of the invocation
        assertEquals("john", name);

        // 不为 null 的情况下，如果 default 是一个表达式，orElse 依然会调用 getMyDefault，但是 orElseGet 就不会再调用
        name = Optional.ofNullable("john").orElse(getMyDefault());
        name = Optional.ofNullable("john").orElseGet(() -> getMyDefault());

    }

    public String getMyDefault() {
        System.out.println("Getting Default Value");
        return "Default Value";
    }

    // 如果为 null 抛出异常，用起来也很方便
    @Test(expected = IllegalArgumentException.class)
    public void whenOrElseThrowWorks_thenCorrect() {
        String nullName = null;
        String name = Optional.ofNullable(nullName).orElseThrow(IllegalArgumentException::new);
        name = Optional.ofNullable(nullName).orElseThrow(); // Java10 增加了 orElseThrow()，这个是默认抛出 NoSuchElementException
    }


    @Test(expected = NoSuchElementException.class)
    public void givenOptionalWithNull_whenGetThrowsException_thenCorrect() {
        Optional<String> opt = Optional.of("baeldung");
        String name = opt.get();
        assertEquals("baeldung", name);

        opt = Optional.ofNullable(null);
        name = opt.get(); // 如果是 null 的话直接调用 get 会抛出 NoSuchElementException 异常，这一点设计的不好
        name = opt.orElse(""); // 所以一般使用 orElse 代替 get 取值
    }

}
```

# 高阶用法

## Conditional Return With filter()

场景举例：我们想挑选一个价格在一定区间的 Model 光猫，来看看常规写法和 Optional 写法

```Java
// 常规写法
public boolean priceIsInRange1(Modem modem) {
    boolean isInRange = false;

    if (modem != null && modem.getPrice() != null 
      && (modem.getPrice() >= 10 
        && modem.getPrice() <= 15)) {

        isInRange = true;
    }
    return isInRange;
}

// 骚写法
public boolean priceIsInRange1(Modem modem) {
    return Optional.ofNullable(modem).map(Modem::getPrice).filter(p -> p >= 10 && p <= 15).isPresent();
}
```

## Transforming Value With map()

The map method returns the result of the computation wrapped inside Optional。We can chain map and filter together to do something more powerful.

场景举例：判断给定的密码是否合法（可能为空）

```Java
@Test
public void givenOptional_whenMapWorks_thenCorrect2() {
    String password = " password ";
    // 常规写法
    boolean valid1 = StringUtils.isBlank(password)? false : "password".equals(password.trim());
    // 骚写法，不过貌似比常规写法还要长🤣
    boolean valid2 = Optional.ofNullable(password).map(String::trim).filter(pass -> pass.equals("password")).isPresent();
}
```

## Transforming Value With flatMap()

我们在实际编程中常常用到的都是一个复杂的对象，假设对象里的方法返回的也是一个 Optional（hibernate 的接口就是这样的），我们在使用过程中就会出现下面例子中的 Optional 套 Optional 的情况，这时候可以用 flatMap 来处理。

```Java
@Data
@AllArgsConstructor
public class Person {
    private String name;
    private int age;

    public Optional<String> getName() {
        return Optional.ofNullable(name);
    }

    public Optional<Integer> getAge() {
        return Optional.ofNullable(age);
    }
    // normal constructors and setters
}

@Test
public void givenOptional_whenFlatMapWorks_thenCorrect2() {
    Person person = new Person("john", 26);
    Optional<Person> personOptional = Optional.of(person);

    Optional<Optional<String>> nameOptionalWrapper
            = personOptional.map(Person::getName);
    Optional<String> nameOptional
            = nameOptionalWrapper.orElseThrow(IllegalArgumentException::new);
    String name1 = nameOptional.orElse("");
    assertEquals("john", name1);

    // 使用 flatMap
    String name = personOptional
            .flatMap(Person::getName)
            .orElse("");
    assertEquals("john", name);
}
```

## The ifPresentOrElse() Method （since Java9）

ifPresentOrElse() 实现了一个类似于回调的功能

```Java
@Test
public void givenOptional_whenPresent_thenShouldExecuteProperCallback() {
    // given
    Optional<String> value = Optional.of("properValue");
    AtomicInteger successCounter = new AtomicInteger(0);
    AtomicInteger onEmptyOptionalCounter = new AtomicInteger(0);

    // when
    value.ifPresentOrElse(
            v -> successCounter.incrementAndGet(), // present 回调
            onEmptyOptionalCounter::incrementAndGet); // empty 回调

    // then
    assertEquals(successCounter.get(), 1);
    assertEquals(onEmptyOptionalCounter.get(), 0);
}
```

## 注意事项

Optional 是设计用作返回值得，并不推荐用于方法参数，Optional is meant to be used as a return type. Trying to use it as a field type is not recommended.

using Optional in a serializable class will result in a NotSerializableException. Our article [Java Optional as Return Type](https://www.baeldung.com/java-optional-return) further addresses the issues with serialization. 这个文章里同时介绍了很多不适合用 Optional 做返回值的场景 there are many scenarios that we would be better off to not use Optional return type for a getter. 