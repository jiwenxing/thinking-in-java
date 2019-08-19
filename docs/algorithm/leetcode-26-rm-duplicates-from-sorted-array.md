# 删除数组中重复元素
---

LeetCode 链接：[删除排序数组中的重复项](https://leetcode-cn.com/problems/remove-duplicates-from-sorted-array/)

给定一个排序数组，你需要在原地删除重复出现的元素，使得每个元素只出现一次，返回移除后数组的新长度。
不要使用额外的数组空间，你必须在原地修改输入数组并在使用 O(1) 额外空间的条件下完成。

示例 1:

给定数组 nums = [1,1,2], 

函数应该返回新的长度 2, 并且原数组 nums 的前两个元素被修改为 1, 2。 

你不需要考虑数组中超出新长度后面的元素。

示例 2:

给定 nums = [0,0,1,1,1,2,2,3,3,4],
函数应该返回新的长度 5, 并且原数组 nums 的前五个元素被修改为 0, 1, 2, 3, 4。
你不需要考虑数组中超出新长度后面的元素。


常规思路：前后比较，相等的时候将后面一个元素取出来放在数组末尾，其它元素向前顺移一位。

```Java
class Solution {
    public int removeDuplicates(int[] nums) {
        if(nums==null || nums.length==0)
            return 0;
        int len = nums.length;
        for(int i=0; i<len-1; ){
            if(nums[i]==nums[i+1]){
                int j=i+1;
                int temp = nums[j]; //相等的后一个元素暂存
                while(j<len-1){ //后面的所有元素向前顺移一位
                    nums[j] = nums[j+1];
                    j++;
                }
                nums[j] = temp; //将暂存的重复元素放在数组末尾
                len--; //同时数组的有效长度（不重复元素）减 1
            }else{
                i++;
            }
        }
        return len;
    }
}
```

这种由于每次比较相等时都需要顺移其余所有元素，效率肯定是高不了，有没有什么更好的方法呢？标准答案提供了一个**双指针**的思路：

1. 数组完成排序后，我们可以放置两个指针 i 和 j，其中 i 是慢指针，而 j 是快指针。只要 nums[i] = nums[j]，我们就增加 j 以跳过重复项。

2. 当我们遇到 nums[j] != nums[i] 时，跳过重复项的运行已经结束，因此我们必须把它 nums[j] 的值复制到 nums[i+1]。然后递增 i，接着我们将再次重复相同的过程，直到 j 到达数组的末尾为止。

```Java
class Solution {
    public int removeDuplicates(int[] nums) {
        if(nums==null || nums.length==0)
            return 0;
        int i=0;
        for(int j=1; j<nums.length; j++){
            if(nums[i] != nums[j]){
                nums[++i] = nums[j];
            }
        }
        return i+1;
    }
}
```

这道题比较偏向于技巧型，但是双指针的思路还是有挺多场景会用到，例如之前的单链表倒数第 k 个元素。因此双指针的技巧也应该放在常规武器库里，随时准备用起来。
