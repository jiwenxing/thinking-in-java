package leetcode;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

/**
 * 判断括号是否正确闭合
 * leetcode：https://leetcode-cn.com/problems/valid-parentheses/
 */
public class ValidParentheses {
    private static Map<Character, Character> map = new HashMap<>();
    static {
        map.put('(', ')');
        map.put('[', ']');
        map.put('{', '}');
    }
    public static boolean isValid(String s){
        if (s==null || s=="")
            return false;
        int len = s.length();
        if (len%2 != 0)
            return false;
        Stack<Character> stack = new Stack<>();
        char[] chars = s.toCharArray();
        for (int i = 0; i < len; i++) {
            if (map.keySet().contains(chars[i])){
                stack.push(chars[i]);
            }else {
                if (i>0 && chars[i] == map.get(stack.pop()))
                    continue;
                return false;
            }
        }
        return stack.isEmpty();
    }

    public static void main(String[] args) {
//        System.out.println(isValid("[]{}()"));
//        System.out.println(isValid("[]{[}()"));
//        System.out.println(isValid("{[({})]}"));
//        System.out.println(isValid("})]}"));
        int[] nums = new int[]{1,1,2};
        System.out.println(removeDuplicates(nums));
    }

    public static int removeDuplicates(int[] nums) {
        if(nums==null || nums.length==0)
            return 0;
        int len = nums.length;
        for(int i=0; i<len-1; ){
            if(nums[i]==nums[i+1]){
                int j=i+1;
                int temp = nums[j];
                while(j<len-1){
                    nums[j] = nums[j+1];
                    j++;
                }
                nums[j] = temp;
                len--;
            }else{
                i++;
            }
        }
        return len;
    }

}
