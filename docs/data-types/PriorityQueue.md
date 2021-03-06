# HEAP 堆和优先队列 PriorityQueue
---

## 堆基础

堆(Heap)是具有这样性质的数据结构：

1. 完全二叉树 
2. 所有节点的值大于等于(或小于等于)子节点的值
3. 堆可以用数组存储，插入、删除会触发节点 shift_down、shift_up 操作，时间复杂度O(logn)
4. 堆是优先级队列(PriorityQueue)的底层数据结构，**较常使用优先级队列而非直接使用堆处理问题**。

要进一步了解堆的结构可以通过可视化的构建堆来理解 [可视化构建堆](https://www.cs.usfca.edu/~galles/visualization/Heap.html)

## 堆排序

上面的介绍可知，堆是具有以下性质的完全二叉树：每个结点的值都大于或等于其左右孩子结点的值，称为大顶堆；或者每个结点的值都小于或等于其左右孩子结点的值，称为小顶堆。如下图：

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/91b1b017-d1c2-469e-b2fb-f2779ed0e255)

同时，我们对堆中的结点按层进行编号，将这种逻辑结构映射到数组中就是下面这个样子，之所以可以将堆映射为数组，就是因为其完全二叉树的结构，我们可以很容易通过下标计算任何节点的父亲节点和左右子节点的下标。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/52bade3c-9267-4ea2-9fdf-aff5c09aec3f)

该数组从逻辑上讲就是一个堆结构，我们用简单的公式来描述一下堆的定义就是：

- 大顶堆：`arr[i] >= arr[2i+1] && arr[i] >= arr[2i+2]`
- 小顶堆：`arr[i] <= arr[2i+1] && arr[i] <= arr[2i+2]`  

堆排序是利用堆这种数据结构而设计的一种排序算法，堆排序是一种选择排序，它的最坏，最好，平均时间复杂度均为O(nlogn)，它也是不稳定排序。

堆排序的基本思想是：

1. 将待排序序列构造成一个大顶堆，此时，整个序列的最大值就是堆顶的根节点。
2. 将其与末尾元素进行交换，此时末尾就为最大值。
3. 然后将剩余n-1个元素重新构造成一个堆，这样会得到n个元素的次小值。
4. 如此反复执行，便能得到一个有序序列了

可以参考这篇文章的图解来理解堆排序的过程：[图解排序算法(三)之堆排序](https://www.cnblogs.com/chengxiao/p/6129630.html)

附：[堆排序动画演示](https://www.cs.usfca.edu/~galles/visualization/HeapSort.html)

## 堆排序 vs 快速排序

1. 10w 数据量两种排序速度基本相当，但是堆排序交换次数明显多于快速排序；10w+数据，随着数据量的增加快速排序效率要高的多，数据交换次数快速排序相比堆排序少的多。
2. 实际应用中，堆排序的时间复杂度要比快速排序稳定，快速排序的最差的时间复杂度是O（n*n）,平均时间复杂度是O(nlogn)。堆排序的时间复杂度稳定在O(nlogn)。但是从综合性能来看，快速排序性能更好。
3. 堆排序数据访问的方式没有快速排序友好。对于快速排序来说，数据是跳着访问的。比如：堆排序中，最重要的一个操作就是数据的堆化。比如下面的例子，对堆顶节点进行堆化，会依次访问数组下标是1.2.4.8的元素，而不是像快速排序那样，局部顺讯访问，所以，这样对cpu缓存是不友好的。
4. 对于同样的数据，在排序过程中，堆排序算法的数据交换次数要多于快速排序。排序有 有序度和逆序度两个概念。对于基于比较的排序算法来说，整个排序过程就是由两个基本的操作组成的，比较和交换（移动）。快速排序数据交换的次数不会比逆序度多。但是堆排序的第一步是建堆，建堆过程会打乱数据原有的相对先后顺序，导致数据的有序度降低。比如，对于一组已经有序的数据来说，经过建堆之后，数据反而变得更无序了。