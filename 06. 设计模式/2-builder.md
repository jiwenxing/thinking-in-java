# Builder 模式
---

Builder 模式也叫作建造者模式，是用于构建复杂对象的一种模式。主要是用一个内部类去实例化一个对象，避免一个类出现过多构造函数，而且构造函数如果出现默认参数的话，很容易出错。平时开发中会发现很多框架都使用了 Builder 模式。

日常开发中我们常常会面临编写一个这样的实现类(假设类名叫 Person)，这个类拥有多个构造函数。随着属性的增多，我们不得不书写多种参数组合的构造函数，而且其中还需要设置默认参数值，这样的构造函数灵活性也不高，而且在调用时你不得不提供一些没有意义的参数值。

```Java
Person(String name);
Person(String name, int age);
Person(String name, int age, String address);
```

我们会用 Builder 模式对其进行改写如下，这样客户程序就可以很灵活的去构建这个对象。

```Java
public class Person {
    private final String name;
    private final int age;
    private final String address;

    public Person(Builder builder) {
        this.name = builder.name;
        this.age = builder.age;
        this.address = builder.address;
    }

    public String getName() {
        return name;
    }

    public int getAge() {
        return age;
    }

    public String getAddress() {
        return address;
    }

    public static class Builder {
        private String name = null;
        private int age = 0;
        private String address = null;

        public Builder(String name) {
            this.name = name;
        }

        public Builder age(int age) {
            this.age = age;
            return this;
        }

        public Builder address(String address) {
            this.address = address;
            return this;
        }

        public Person build() {
            return new Person(this);
        }
    }

    public static void main(String[] args) {
        Person person = new Builder("jverson").age(18).address("abroad").build();
        System.out.println(person.getName() + ", " + person.getAge() + ", " + person.getAddress());
    }
}
```