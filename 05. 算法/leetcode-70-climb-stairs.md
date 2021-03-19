# 爬楼梯问题
---

LeetCode 链接：[爬楼梯](https://leetcode-cn.com/problems/climbing-stairs/)

题目：

假设你正在爬楼梯。需要 n 阶你才能到达楼顶。每次你可以爬 1 或 2 个台阶。你有多少种不同的方法可以爬到楼顶呢？

思路：

这个问题很常见了，就是斐波那契数列的形式，使用递归即可。公式：f(n) = f(n-1) + f(n-2), 并已知 n=1 时为 1，n=2 时为 2。

很容易写出下面这样的代码：

```Java
class Solution {
    public int climbStairs(int n) {
        if(n==1)
            return 1;
        if(n==2)
            return 2;
        return climbStairs(n-1)+climbStairs(n-2);
    }
}
```

严格来讲这段代码逻辑上并没有什么问题，但是会发现几乎有一倍的重复计算，在 LeetCode 上提交也会显示超时而不通过，因此需要想办法优化一下逻辑减少重复计算，很容易想到将计算过的结果缓存一下，每次取缓存，缓存没有再回源。


```Java
class Solution {
    static Map<Integer, Integer> map = new HashMap<>();
    public int climbStairs(int n) {
        if(n==1)
            return 1;
        if(n==2)
            return 2;
        int result1 = map.keySet().contains(n-1)?map.get(n-1):climbStairs(n-1);
        int result2 = map.keySet().contains(n-2)?map.get(n-2):climbStairs(n-2);
        int result = result1 + result2;
        map.put(n, result);
        return result;
    }
}
```

其实也可以不用递归实现，既然知道 f(n) = f(n-1) + f(n-2), 并f(1)=1, f(2)=2。我们从小到大顺序计算到 f(n) 不就行了吗

```Java
class Solution {
    static Map<Integer, Integer> map = new HashMap<>();
    static {
        map.put(1,1);
        map.put(2,2);
    }
    public int climbStairs(int n) {
        if(n<3)
            return map.get(n)==null?0:map.get(n);
        for(int i=3; i<=n; i++){
            int num = map.get(i-1) + map.get(i-2);
            map.put(i, num);
        }
        return map.get(n);
    }
}
```

上面的算法还可以继续优化，因为它借助了一个 O(n) 的 map，空间复杂度能不能降到 O(1) 呢？那我们就采用临时变量不断游走的方式

```Java
class Solution {
    public int climbStairs(int n) {
        if(n<3)
            return n>0 ? n:0;
        int pre1 = 1;
        int pre2 = 2;
        int num = 0;
        for(int i=3; i<=n; i++){
            num = pre1 + pre2;
            pre1 = pre2;
            pre2 = num;
        }
        return num;
    }
}
```