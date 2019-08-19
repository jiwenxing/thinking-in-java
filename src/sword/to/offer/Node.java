package sword.to.offer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Node {
    private int data;
    private Node next;
    public Node(Integer data) {
        this.data = data;
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

    static void testArr(int[] arr){
        System.out.println(arr);

    }

    public static void main(String[] args) {
        Node node1 = new Node(1);
        Node node2 = new Node(2);
        Node node3 = new Node(3);
        Node node4 = new Node(4);
        Node node5 = new Node(5);

        node1.next = node2;
        node2.next = node3;
        node3.next = node4;
        node4.next = node5;

        System.out.println(findKthNode(node1, 5).data);
        System.out.println(findKthNode(node1, 1).data);
        System.out.println(findKthNode(node1, 6));
        System.out.println(Math.pow(10,2));;
        String s = "a";
        System.out.println(s.substring(0,1));
        testArr(null);
        Set set = new HashSet<>();
        set.add(1);
        System.out.println(set.iterator().next());
    }
}
