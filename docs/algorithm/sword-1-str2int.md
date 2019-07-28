# 剑指1：String 转 int
---

 题目:

 > 不使用 Java 的工具类，将字符串转成 int 类型，即实现类似 `Integer.valueOf(str)` 这个方法

 思路：

 1. 程序鲁棒性，是不是能完整的考虑到各种边界条件
 2. 转换方法以及对 Interger 类型的掌握


 注意点：

 边界条件包括空字符、非法字符、正负数、大小越界等等
 使用 `char - '0'` 进行字符和数字的转化

 程序实现：


 ```Java
public class Str2Int {
    public static int str2int(String str) {
        if (str == null || str.trim() == "")
            throw new RuntimeException("invalid input!");

        int num = 0;
        long limit = Integer.MAX_VALUE; // 使用 long 类型校验最大最小边界

        //正负处理
        boolean negative = false;
        if (str.startsWith("-")) {
            negative = true;
            str = str.substring(1);
        } else if (str.startsWith("+")) {
            str = str.substring(1);
        }

        //不能只是"+"或"-"
        if (str.isEmpty())
            throw new RuntimeException("only '+' or '-' included!");

        if (negative)
            limit = -(long)Integer.MIN_VALUE; //使用 long 型存储边界值

        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            //不能有非数字字符
            if (chars[i] - '0' > 9 || chars[i] - '0' < 0)
                throw new RuntimeException("invalid character included!");
            //如果超出 Integer 范围抛异常
            if (num * 10L + chars[i] - '0' <= limit) //确保下面的计算不要越界
                throw new RuntimeException("exceed range of int!");
            num = num * 10 + chars[i] - '0';
        }
        return negative ? -num : num;
    }

    public static void main(String[] args) {
        System.out.println(Integer.MAX_VALUE); // 2147483647
        System.out.println(Integer.MIN_VALUE); // -2147483648
        System.out.println(-Integer.MIN_VALUE); // -2147483648
        //正负
        String str = "-2147483648";
        System.out.println(str2int(str));
        System.out.println(Integer.valueOf(str));
    }

}
```

其中有一点需要特别注意，在判断大小是否越界的时候需要用到 Integer.MAX_VALUE 和 Integer.MIN_VALUE，需要特别注意这两个值的表示方式！

首先我们知道 Interger 是四个字节，其中

- **Integer.MAX_VALUE** 十六进制表示： 0x7fffffff 即 2^31-1 = 2147483647，二进制表示：0111 1111 1111 1111 1111 1111 1111 1111，大概是 21 亿多的数量级；
- **Integer.MIN_VALUE** 十六进制表示： 0x80000000 即 -2^31 = -2147483648，二进制表示：1000 0000 0000 0000 0000 0000 0000 0000，注意这是采用补码的形式表示，最高位表示正负

因此这里我们判断转换的数的绝对值是否越界时就不能简单的认为负数时绝对值边界是 `limit = -Integer.MIN_VALUE`，因为 int 类型不能给赋值为 2147483648，这时这个操作本身就已经越界了，实际上 `-Integer.MIN_VALUE = Integer.MIN_VALUE`。因此这里我们用一个 long 型去判断边界！

> System.out.println(Integer.MIN_VALUE); // -2147483648    
  System.out.println(-Integer.MIN_VALUE); // -2147483648

这时候就有一个有趣的现象是 `Integer.MAX_VALUE + 1 = Integer.MIN_VALUE`，我们在平时使用当中如果不小心就可能造成下面这样的死循环:


```Java
int a = Integer.MAX_VALUE - 10;
while (a <= Integer.MAX_VALUE){
    System.out.println("a = " + a);
    a++;
    Thread.sleep(100);
}
```

