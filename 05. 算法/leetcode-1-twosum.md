# LeetCode-1：从数组中找到和为指定值得两个元素
---

题目：给定一个整数数组 nums 和一个目标值 target，请你在该数组中找出和为目标值的那两个整数，并返回他们的数组下标。

思路：暴力法两层遍历可以在 O(n2) 的复杂度下实现，但这显然不是我们想要的答案，肯定有复杂度更优的解法。例如 O(nlogn) 的解法，甚至 O(n) 的解法。

O(nlogn) 我们首先想到快排还有二分查找，确实这样可以实现，首先使用快排对数组排序，然后两层循环嵌套但内侧循环使用二分查找，此时时间复杂度就是 O(nlogn)，实现代码略微有些繁琐

O(n) 有没有可能呢？在这个题目里遍历一次是不可能省掉的，也就是说必须在遍历的过程中有一个 O(1) 复杂度的方法去查找另一个元素，O(1) 复杂度立马联想到 hash，hash 常用的结构就是 HashSet 和 HashMap，这里因为要返回下标，因此使用 HashMap，其 key 为数组中的值，value 为其下标，实现如下。

注意边界条件，另外数组中允许有重复的元素，下面算法都可以实现


```Java
class Solution {
    public int[] twoSum(int[] nums, int target) {
        if(nums==null || nums.length<2)
            return null;
        Map<Integer, Integer> map = new HashMap<>();
        for(int i=0; i<nums.length; i++){
            int other = target - nums[i];
            if(map.containsKey(other)){
                return new int[]{map.get(other), i};
            }
            map.put(nums[i], i);
        }
        return null;
    }
}
```