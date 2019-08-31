# 环形链表
---

LeetCode 链接：[环形链表](https://leetcode-cn.com/problems/linked-list-cycle/submissions/)

给定一个链表，判断链表中是否有环。

典型的套路题，记住思路即可，双指针不同速度向前移动，如果存在环的话肯定会进入到环里套圈，也就是说 fast 和 slow 会再次相遇

```Java
public class Solution {
    public boolean hasCycle(ListNode head) {
        if(head==null || head.next==null)
            return false;
        ListNode slow = head;
        ListNode fast = head.next;
        while(slow != fast){
            if(fast==null || fast.next==null) //fast 遇到 null 则说明没有环可以走到末尾
                return false;
            slow = slow.next;
            fast = fast.next.next;
        }
        return true;
    }
}
```