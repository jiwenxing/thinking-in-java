Java 中经常需要对一个列表进行排序，列表中可能是基本数据类型，也可能是自定义对象，对于自定义对象的排序我们可能只想按照其某个属性排序，甚至多种条件组合对其排序，这些都可以借助于 Comparator 接口来实现。

## 基本数据类型集合排序

基本数据类型的排序比较简单，一般使用 JDK 本身提供了默认的比较器 naturalOrder、reverseOrder 等就可以实现基本的一些排序需求。

```java
List<Integer> numbers = Lists.newArrayList();
numbers.add(new Integer(5));
numbers.add(new Integer(15));
numbers.add(new Integer(53));
numbers.add(new Integer(35));
numbers.add(new Integer(16));

System.out.println("input list is " + numbers);

numbers.sort(Comparator.naturalOrder());
System.out.println("after sort is " + numbers);

numbers.sort(Comparator.reverseOrder());
System.out.println("after reverse sort is " + numbers);
```

## 自定义对象集合排序

自定义对象的排序需要对象本身提供比较的规则，最常见的方式是实现`Comparable`接口，另外还可以实现`Comparator`接口达到多种规则排序的目的，这里重点要介绍一下 JDK8 中对排序的一些改进实现。

### 传统方法1-实现`Comparable`接口

这是最传统的实现方法，需要实体类实现额外的接口，代码侵入比较严重，并且写法不够灵活，不能实现多种排序方式，不建议使用。

```java
class Student implements Comparable<Student>{
    private String name;
    private int score;
    private int age;

    //构造方法 ...
    
    //get set 方法 ...
   
    //实现Comparable的compareTo方法
    @Override
    public int compareTo(Student stu) {
        // TODO Auto-generated method stub
        return this.getScore()-stu.getScore();
    }

    public static void main(String [] args){
        List<Student> list = new ArrayList<Student>();
        list.add(new Student("张三",89,20));
        list.add(new Student("李四",60,21));
        list.add(new Student("路人",99,15));
        Collections.sort(list);
        for(Student stu : list){
            System.out.println(stu.toString());
        }
    }

}
```

### 传统方法2-实现`Comparator`接口

这种写法代码侵入较小，但需要为排序单独创建实现了 Comparator 接口的类，相较于上一种方式稍微有所改进。

```java
//按照年龄排序的比较器
//sortAge实现Comparator接口
class sortAge implements Comparator<Student>{

    @Override
    //实现Comparator的compare方法
    public int compare(Student stu1, Student stu2) {
        // TODO Auto-generated method stub
        return stu1.getAge()-stu2.getAge();
    }

}

//按照名字排序的比较器
//sortAge实现Comparator接口
class sortName implements Comparator<Student>{

    @Override
    //实现Comparator的compare方法
    public int compare(Student stu1, Student stu2) {
        // TODO Auto-generated method stub
        return stu1.getName().compareTo(stu2.getName());
    }

}

public static void main(String [] args){
    List<Student> list = new ArrayList<Student>();
    list.add(new Student("A",89,20));
    list.add(new Student("C",60,21));
    list.add(new Student("B",99,15));

    //按照年龄排序
    Collections.sort(list, new sortAge());
    
    //按照名字排序
    Collections.sort(list, new sortName());

    //匿名内部类排序
    Collections.sort(list, new Comparator<Student>() {
	    public int compare(Student p1, Student p2){
	    	return Integer.valueOf(p1.getAge()).compareTo(p2.getAge());
	    }
    });
}
```

### JDK8 的各种实现方式

利用 JDK8 提供的新特性，可以使得排序写法更加简洁优雅，强烈推荐使用。

```java
//自定义类列表
List<Person> persons = Lists.newArrayList();
Person p1 = new Person("zhang",25,52000);
Person p2 = new Person("wang",35,32000);
Person p3 = new Person("chen",29,27000);
persons.add(p1);
persons.add(p2);
persons.add(p3);

System.out.println("---使用JDK8方式对自定义对象排序---");
System.out.println("origin list: " + persons);
//年龄顺序
Collections.sort(persons, Comparator.comparing(Person::getAge)); //这里使用了JDK8的方法传递特性
System.out.println("age sort: " + persons);
//薪水顺序
Collections.sort(persons, Comparator.comparing(Person::getSalary));
System.out.println("salary sort: " + persons);
//薪水逆序
Collections.sort(persons, Comparator.comparing(Person::getSalary).reversed());
System.out.println("salary reversed sort: " + persons);

/*使用静态方法引用方式*/
//1. 需要在Person类中定义一个静态的比较方法compareBySalaryThenAge
public static int compareBySalaryThenAge(Person h1, Person h2) {
	if (h1.getSalary().equals(h2.getSalary())) {
	    return Integer.compare(h1.getAge(), h2.getAge());
	}
	return h1.getSalary().compareTo(h2.getSalary());
}
//2. 是用静态引用进行比较
persons.sort(Person::compareBySalaryThenAge); //这里就是典型的行为参数化，将函数当做参数传入，这也是lambda表达式的本质
System.out.println("static reference sort: " + persons);


/*使用lambda表达式*/
Comparator<Person> comparator = (h1, h2) -> h1.getAge().compareTo(h2.getAge());
persons.sort(comparator);
persons.sort(comparator.reversed());

/*上面可以简写为如下形式*/
persons.sort((Person h1, Person h2) -> h1.getAge().compareTo(h2.getAge()));

/*统计年龄大于25的人数，使用 stream 一句搞定*/
long cnt = persons.stream().filter(person -> person.getAge()>25).count();
System.out.println(cnt);

```