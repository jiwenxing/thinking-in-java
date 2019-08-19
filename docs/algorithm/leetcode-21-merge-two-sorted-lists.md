# 合并两个有序链表
---

力扣连接：[合并两个有序链表](https://leetcode-cn.com/problems/merge-two-sorted-lists/)

题目描述：将两个有序链表合并为一个新的有序链表并返回。新链表是通过拼接给定的两个链表的所有节点组成的。 

链表结构定义：
```Java
public class ListNode {
   int val;
   ListNode next;
   ListNode(int x) { val = x; }
}
```

示例：

> 输入：1->2->4, 1->3->4    
输出：1->1->2->3->4->4


思路：这道题暴力解决的话能感觉到逻辑会较为复杂，并且代码会很乱，应该会有更优雅的方法。复杂问题要简单化递归的思路总可以拿出来套一套，看看适合不适合。不难发现要求 l1 和 l2 的合并链表，如果 l1.val <= l2.val，那么第一个节点就是 l1，而下一个节点指向 l1.next 和 l2 的合并链表即可。这不就是递归吗！

```Java
class Solution {
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        if(l1==null)
            return l2;
        if(l2==null)
            return l1;
        if(l1.val > l2.val){
            l2.next = mergeTwoLists(l1, l2.next);
        }else{
            l1.next = mergeTwoLists(l1.next, l2);
        }
        return l1.val <= l2.val ? l1:l2;
    }
}
``` 

由之前的经验可知，递归的问题一般也可以通过迭代的方式实现，例如典型的二分查找。迭代的思想就是，使用一个 while 循环，在循环内部不断的去修改 while 的判断条件直至其退出循环，此时计算处理完成得到结果。

```Java
class Solution {
    public ListNode mergeTwoLists(ListNode l1, ListNode l2) {
        ListNode prehead = new ListNode(-1);  //求新链表的问题一般都需要定义一个虚拟节点，其指向真正的头节点
        ListNode temp = prehead;  //临时节点，循环中需要不停的往后游动
        while(l1!=null || l2!=null){  //循环处理直至最后一个节点
            if(l1!=null && l2!=null){ //两个链表节点都不为空的情况，next 指向其中较小的节点
                if(l1.val <= l2.val){
                    temp.next = l1;
                    l1 = l1.next;
                }else{
                    temp.next = l2;
                    l2 = l2.next;
                }
                temp = temp.next;
            }else if(l1!=null){  //只要有其中一个链表已经遍历完，则只需要将 next 指向另一个链表剩余节点的头部即可
                temp.next = l1;
                break;
            }else{
                temp.next = l2;
                break;
            }
        }
        return prehead.next;
    }
}
```