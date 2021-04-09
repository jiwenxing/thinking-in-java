package reflect;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class IntList extends ArrayList<Integer> {
    public static void main(String[] args) {
        // 测试
        IntList intList = new IntList();
        intList.add(1);
        intList.add(2);
        System.out.println(intList);

        // 子类获取父泛型类的类型
        Class<IntList> intListClass = IntList.class;
        Type t = intListClass.getGenericSuperclass();
        if (t instanceof ParameterizedType) {
            ParameterizedType pt = (ParameterizedType) t;
            Type[] types = pt.getActualTypeArguments(); // 可能有多个泛型类型
            Type firstType = types[0]; // 取第一个泛型类型
            Class<?> typeClass = (Class<?>) firstType;
            System.out.println(typeClass); // class java.lang.Integer
        }

    }
}
