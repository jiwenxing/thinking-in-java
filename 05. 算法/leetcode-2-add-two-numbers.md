# 两数相加
---

给出两个**非空**的链表用来表示两个非负的整数。其中，它们各自的位数是按照 **逆序** 的方式存储的，并且它们的每个节点只能存储 **一位** 数字。如果，我们将这两个数相加起来，则会返回一个新的链表来表示它们的和。

您可以假设除了数字 0 之外，这两个数都不会以 0 开头。

示例：    
> 输入：(2 -> 4 -> 3) + (5 -> 6 -> 4)
输出：7 -> 0 -> 8
原因：342 + 465 = 807

思路：

刚开始做这道题的时候，我首先想到的就是既然是求和，就先把 list 转成 int，加完再转回去就好了，这样就不用考虑进位之类的细节了。于是很快写出了下面的代码，测试用例无误，提交显示未通过，哦，忘了考虑数值范围了，本题未限制链表长度，换句话就是转换成数值可以无限大，转整形肯定会越界，转任何数值型都会越界。此路不通，另寻它法！


```Java
class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        if(l1==null)
            return l2;
        if(l2==null)
            return l1;
        int int3 = listToInt(l1) + listToInt(l2);
        return intToList(int3);
    }
    
    int listToInt(ListNode node){
        int num = 0, j = 0;
        while(node != null){
            num += node.val * (int)Math.pow(10d, j);
            j++;
            node  = node.next;
        }
        return num;
    }
    
    ListNode intToList(int num){
        if(num==0){
            return new ListNode(0);
        }
        ListNode node = null;
        ListNode temp = null;
        while(num > 0){
            ListNode n = new ListNode(num%10);
            if(temp != null){
                temp.next = n;                
            }else{                
                node = n;
            }
            temp = n;
            num = num/10;    
        }
        return node;
    }
}
```

换个思路那就老老实实的一位一位加吧，处理好进位的细节即可，也是大概看过类似的解法，靠着记忆和推理这次提交直接通过。这种中级的题目确实稍微有点绕弯，还是多联系吧

```Java
class Solution {
    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        if(l1==null) //边界条件判断
            return l2;
        if(l2==null)
            return l1;
        int forward = 0; //进位标记
        ListNode t1 = l1, t2 = l2; //入参赋给两个临时变量
        ListNode t0 = new ListNode(-1), temp = t0;  //建一个虚拟的首节点，后面要用
        
        while(t1 != null || t2 != null || forward>0){ // 注意遍历结束条件
            int a1 = t1!=null?t1.val:0;
            int a2 = t2!=null?t2.val:0;
            int sum = a1 + a2 + forward;
            if(sum > 9){  //需要进位
                forward = 1;
                sum = sum - 10;
            }else{
                forward = 0;
            }
            ListNode node = new ListNode(sum);
            temp.next = node;
            temp = temp.next;
            if(t1!=null)
                t1 = t1.next;
            if(t2!=null)
                t2 = t2.next;
        }
        return t0.next;
    }
}
```
