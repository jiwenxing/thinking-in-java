package design.patterns;

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
