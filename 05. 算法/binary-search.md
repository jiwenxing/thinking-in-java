# 二分查找
---

二分查找是针对有序数组进行查找的一种算法，算法复杂度是 O(logn)。

> 将目标元素与数组的中间元素比较，小于中间元素则说明其在前半部分数组中，对前半部分重新执行二分查找；同理如果大于中间元素则在后半部分重新执行二分查找；相等直接返回当前下标即可（注意中间元素已经比较过，递归的过程中不需要再包含）。显然这是一个迭代或者递归的过程，代码实现也分为两种方式，迭代实现和递归实现。


迭代实现代码示例：

```Java
public static int binarySearch(int[] arr, int key) {
    if (arr == null || arr.length == 0)
        return -1;
    int low = 0;
    int hign = arr.length - 1;
    while (low <= hign) {
        int mid = (low + hign) / 2;
        if (arr[mid] > key) {
            hign = mid - 1;
        } else if (arr[mid] < key) {
            low = mid + 1;
        } else {
            return mid;
        }
    }
    return -1;
}
```

递归实现如下所示

```Java
public static int binarySearchRecursively(int[] arr, int key, int low, int hign) {
    if (low > hign)
        return -1;
    int mid = (low + hign) / 2;
    if (arr[mid] == key) {
        return mid;
    } else if (arr[mid] < key) {
        return binarySearchRecursively(arr, key, mid + 1, hign);
    } else {
        return binarySearchRecursively(arr, key, low, mid - 1);
    }
}
```



两种方式都 ok 主要看个人喜好，一般情况下迭代方式会相对更友好一些，因为递归总是要占用更多的栈空间，有引发 `StackOverflowException` 的风险。像 `int index = Arrays.binarySearch(sortedArray, key);` 方法中默认就使用了迭代方式而非递归方式。

和线性查找比起来，理论上二分查找效率呀更高一些，但是也看具体情况。二分查找需要先对数组进行排序而线性查找则不需要，如果要找的数比较小的情况下线性查找的次数也要比二分查找更少。因此在实际使用中还需要针对具体场景进行选择。


## 参考

- [Binary Search Algorithm in Java](https://www.b.com/java-binary-search)