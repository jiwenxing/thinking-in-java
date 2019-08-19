# 剑指15：单向链表中导数第 k 个节点
---

题目：输入一个链表，输出该链表中导数第 k 个节点，链表定义如下：

```Java
public class Node {
    private int data;
    private Node next;
    // ...
}
```

思路：显然这是一个单向链表，最容易想到也是最笨的方法时遍历一遍列表得到链表长度 n，那么导数第 k 个元素就是第 n-k+1 个元素，再遍历一遍即可。这种方式需要遍历两遍链表，有没有更好的方式呢？

可以定义两个指针都指向头结点，让第一个指针从头先走 k-1 步，然后两个一起走，这样当前面那个指针走到头的时候第二个指针正好到第 k 个结点

代码实现如下，思路和实现都较简单，但是要注意程序的鲁棒性，考虑到一些边界条件

```Java
public class Node {
    private int data;
    private Node next;
    public Node(Integer data, Node next) {
        this.data = data;
        this.next = next;
    }
    static Node findKthNode(Node head, int k){
        if (head == null || k <=0)
            return null;
        Node n1 = head, n2 = null;
        for (int i = 0; i < k-1; i++) {
            if (n1.next != null){
                n1 = n1.next;
            }else {
                return null;
            }
        }
        n2 = head;
        while (n1.next != null){
            n1 = n1.next;
            n2 = n2.next;
        }
        return n2;
    }
}
```

