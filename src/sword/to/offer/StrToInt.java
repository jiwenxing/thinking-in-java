package sword.to.offer;

/**
 * 题目：将 string 转换成数字类型
 * 转换较简单，但需要考虑的边界条件比较多
 * 入参非空校验、正负号、非数字字符、最大最小值
 */
public class StrToInt {
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
        if (str == "")
            throw new RuntimeException("only '+'/'-' included!");

        if (negative)
            limit = -(long)Integer.MIN_VALUE; //使用 long 型存储边界值

        char[] chars = str.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            //不能有非数字字符
            if (chars[i] - '0' > 9 || chars[i] - '0' < 0)
                throw new RuntimeException("invalid character included!");
            //如果超出 Integer 范围抛异常
            if (num * 10L + chars[i] - '0' > limit) //确保下面的计算不要越界
                throw new RuntimeException("exceed range of int!");
            num = num * 10 + chars[i] - '0';
        }
        return negative ? -num : num;
    }

    public static void main(String[] args) throws InterruptedException {
        System.out.println(Integer.MAX_VALUE); // 2147483647
        System.out.println(Integer.MIN_VALUE); // -2147483648
        System.out.println(-Integer.MIN_VALUE); // -2147483648
        //正负
        String str = "-2147483648";
        System.out.println(str2int(str));
        System.out.println(Integer.valueOf(str));

        int a = Integer.MAX_VALUE - 10;
        while (a <= Integer.MAX_VALUE){
            System.out.println("a = " + a);
            a++;
            Thread.sleep(100);
        }
    }

}
