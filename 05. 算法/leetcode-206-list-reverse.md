# 单链表的逆置
---

力扣连接：[反转单链表](https://leetcode-cn.com/problems/reverse-linked-list/)

问题描述：如何实现单链表的逆置，给出完整程序，并测试结果

示例：

> 原链表：1->2->3->4->null    
反转后：4->3->2->1->null

思路：

属于 easy 级别，然后我却花了挺长时间，链表的套路还是不太熟。可以使用迭代和递归的方式实现，迭代的思路更简单一些，而递归稍微绕一点。

迭代解法

```Java
class Solution {
    public ListNode reverseList(ListNode head) {
        if(head==null || head.next==null)
            return head;
        ListNode cur=head, pre=null; 
        while(cur != null){
            ListNode temp = cur.next; // 由于反转操作时会断链，因此使用临时节点存暂存下个节点
            cur.next = pre; // 每次只是将 cur 指向 pre
            pre = cur; // 统一向后移动一位进行迭代
            cur = temp;
        }
        return pre; //注意返回的元素是 pre 不是 cur（此时为 null）
    }
}
```

递归解法

```Java
class Solution {
    public ListNode reverseList(ListNode head) {
        if(head==null || head.next==null)
            return head;
        ListNode node = reverseList(head.next);
        head.next.next = head; //这一步非常关键，不太好理解
        head.next = null;
        return node;
    }
}
```

详细的解题思路见 [LeetCode 官方解法](https://leetcode-cn.com/problems/reverse-linked-list/solution/fan-zhuan-lian-biao-by-leetcode/)

----------
![](//
jverson.oss-cn-beijing.aliyuncs.com/blogpic/E___0109GD00SIGT.gif)