# 有效的括号
---

> 力扣连接：[有效的括号](https://leetcode-cn.com/problems/valid-parentheses/)

题目描述：

给定一个只包括 '('，')'，'{'，'}'，'['，']' 的字符串，判断字符串是否有效。

有效字符串需满足：
1. 左括号必须用相同类型的右括号闭合。
2. 左括号必须以正确的顺序闭合。

实例：

> - "()[]{}" - true
- "(]" - false
- "([)]" - false
- "{[]}" - true


思路：

首先想到暴力解决，发现逻辑写着写着就写不下去了，情况还挺复杂，最后参考了官方解法，使用栈的方式可以巧妙的解决。这种就属于技巧型的题目。

从题目中先找规律，如果是和一个合法的串，会发现两个重要的规律：

1. 第一个闭括号的前一个字符一定是它的开括号
2. 剔除找到的第一个有效的括号对，剩下的串应该也是合法的

这样的话就可以去搜索第一个闭括号，判断其前一个字符是不是其开括号，如果不是直接返回 false，如果是则从整个串里剔除这对继续判断剩余的，如果到最后串全部剔除了，说明整个串就是一个合法有效的括号。

代码具体怎么实现呢？我们可以使用一个 list 去维护这个不断减少的串，应该也可以实现，但是这里更适合一种特殊的 list 结构，那就是不经常使用的栈 Stack。

使用栈的话，遍历字符数组，遇到开括号压入栈，遇到闭括号则从栈顶取一个元素比较是不是一对，如果不是返回 false，如果是则弹出栈顶元素继续比较。

```Java
class Solution {
    private static Map<Character, Character> map = new HashMap<>();
    static {
        map.put('(', ')');
        map.put('[', ']');
        map.put('{', '}');
    }
    public boolean isValid(String s) {
        if (s==null || s=="")
            return false;
        int len = s.length();
        if (len%2 != 0)
            return false; //合法的肯定为偶数个
        //使用栈的思路解决，规律是：1.如果是和一个合法的串，则第一个闭括号的前一个字符一定是它的开括号；2. 剔除找到的第一个有效的括号对，剩下的串应该也是合法的；典型的使用栈的场景
        Stack<Character> stack = new Stack<>();
        char[] chars = s.toCharArray();
        for (int i = 0; i < len; i++) {
            if (map.keySet().contains(chars[i])){
                stack.push(chars[i]);
            }else {
                if (i>0 && chars[i] == map.get(stack.pop()))
                    continue;
                return false;
            }
        }
        return stack.isEmpty();
    }
}
```

