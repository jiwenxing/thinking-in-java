package sword.to.offer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

/**
 * 二分查找
 */
public class BinarySearch {

    /**
     * while 迭代实现
     *
     * @param arr
     * @param key
     * @return
     */
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


    /**
     * 递归实现
     *
     * @param arr
     * @param key
     * @param low
     * @param hign
     * @return
     */
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

    /**
     * 二分查找的变种
     * 对旋转数组返回最小元素的下标
     * 需要考虑数组中是否有重复数字
     * @param arr
     * @return
     */
public static int searchMinIndex(int[] arr){
    if (arr==null || arr.length == 0) //边界判空
        return -1;
    int low = 0;
    int high = arr.length - 1;
    while (low < high){  // 利用二分查找思路，但需要结合实际情况
        int mid = (low + high) / 2;
        if (arr[mid] > arr[high]){ // 此时说明最小值在 mid 的右半边数组中，并且 arr[mid] 肯定不是最小值，可以排除
            low = mid + 1;
        } else if (arr[mid] < arr[high]){ // 此时说明有半部分是递增的，最小值在 mid 左边数组中，并且 arr[mid] 此时有可能就是最小值，因此保留
            high = mid;
        } else { // 这种情况根本无法判断到底在左边还是在右边，只能遍历
            high = high - 1;
        }
    }
    return low;
}



    public static void main(String[] args) {
        int[] arr = {1, 2, 4, 6, 7, 9};
        System.out.println(binarySearch(arr, 7));
        System.out.println(binarySearchRecursively(arr, 7, 0, arr.length - 1));
        System.out.println(Arrays.binarySearch(arr, 7)); //默认使用了迭代模式
        Collections.synchronizedList(new ArrayList());
    }

}
