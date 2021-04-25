Java8 Map API
---

注意下面介绍的这几个 api 都很类似，有一些共同的特征

1. 返回值都是 newValue
2. 计算 newValue 的条件不同


## compute

可以简单讲 compute 方法理解为 get，只不过 get 的是 remappingFunction 得到的新值，而不是 oldvalue。

说白了就是把本来需要两步完成的工作封装了一个 api 里了：

1. 通过 key 和 oldvalue 计算得到一个 newValue，然后返回 newValue
2. 同时如果 oldValue 存在则将其更新为 newValue（这里的一个特殊情况就是如果 newValue 为 null，需要将 oldValue remove 掉）


```Java
default V compute(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        V oldValue = this.get(key);
        V newValue = remappingFunction.apply(key, oldValue);
        if (newValue == null) {
            if (oldValue == null && !this.containsKey(key)) {
                return null;
            } else {
                this.remove(key);
                return null;
            }
        } else {
            this.put(key, newValue);
            return newValue;
        }
}
```


## computeIfAbsent

和 compute 类似，区别是只有 oldValue 不存在或为 null 的情况下才会计算 newValue 并返回，否则直接返回 oldValue


```Java
default V computeIfAbsent(K key, Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(mappingFunction);
        Object v;
        Object newValue;
        if ((v = this.get(key)) == null && (newValue = mappingFunction.apply(key)) != null) { // 这里如果 mappingFunction 计算得到是 null 则不会把 null put 进去
            // key 不存在或key对应value为null，则计算mappingFunction 得到一个 newValue，并将 key-newValue 放进map里，同时返回 newValue
            this.put(key, newValue);
            return newValue;
        } else {
            return v;  // key 存在且其 value 不为 null 的情况下直接返回 value
        }
}

// 示例
Map<String, Integer> stringLength = new HashMap<>();
stringLength.put("John", 5);
assertEquals((long)stringLength.computeIfAbsent("John", s -> s.length()), 5); 

Map<String, Integer> stringLength = new HashMap<>();
assertEquals((long)stringLength.computeIfAbsent("John", s -> s.length()), 4); // Since the key “John” is not present, it computes the value by passing the key as a parameter to the mappingFunction.
assertEquals((long)stringLength.get("John"), 4);
```

## computeIfPresent

和 compute 类似，区别是只有 oldvalue 存在且不为 null 时计算 newValue（可以为 null） 并返回，其它情况返回 null

```Java
default V computeIfPresent(K key, BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Object oldValue;
        if ((oldValue = this.get(key)) != null) {
            V newValue = remappingFunction.apply(key, oldValue);
            if (newValue != null) {
                this.put(key, newValue);
                return newValue;
            } else {
                this.remove(key);
                return null;
            }
        } else {
            return null;
        }
}

// 示例

class Main {
    public static void main(String[] args) {
        // 创建一个 HashMap
        HashMap<String, Integer> prices = new HashMap<>();

        // 往HashMap中添加映射关系
        prices.put("Shoes", 200);
        prices.put("Bag", 300);
        prices.put("Pant", 150);
        System.out.println(prices); // {Pant=150, Bag=300, Shoes=200}

        // 重新计算鞋加上10%的增值税后的价值
        int shoesPrice = prices.computeIfPresent("Shoes", (key, value) -> value + value * 10/100);
        System.out.println(shoesPrice); // 220

        // 输出更新后的HashMap
        System.out.println(prices); // {Pant=150, Bag=300, Shoes=220}}
    }
}
```

## merge（computeIfPresentOrDefault）

和 compute 类似，有点像 computeIfPresentOrDefault 的感觉！即如果 oldvalue 不存在则 newValue 为 defaultValue，如果存在则通过 oldvalue 计算得到 newValue。同时会更新 key 的值为 newValue，更新逻辑与其它方法一样，newValue 为 null 则 remove 掉 key，不为 null 直接更新值。


```Java
default V merge(K key, V value, BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        Objects.requireNonNull(remappingFunction);
        Objects.requireNonNull(value);
        V oldValue = this.get(key);
        V newValue = oldValue == null ? value : remappingFunction.apply(oldValue, value);
        if (newValue == null) {
            this.remove(key);
        } else {
            this.put(key, newValue);
        }

        return newValue;
}
```

## Difference between putIfAbsent and computeIfAbsent in Java 8 Map

ref：https://stackoverflow.com/questions/48183999/what-is-the-difference-between-putifabsent-and-computeifabsent-in-java-8-map/48184207

看上去好像功能差不多，主要区别有以下

一、一个是直接给定值（也可以是一个表达式），另一个是一个 mappingfunction，如果 key 的值存在时 mappingfunction 不会执行，但put还是会执行，有些情况下消耗不一样

computeIfAbsent takes a mapping function, that is called to obtain the value if the key is missing.
putIfAbsent takes the value directly.
If the value is expensive to obtain, then putIfAbsent wastes that if the key already exists.

二、返回值在 key 不存在时不一样

computeIfAbsent returns "the current (existing or computed) value associated with the specified key, or null if the computed value is null".

putIfAbsent returns "the previous value associated with the specified key, or null if there was no mapping for the key".

So, if the key already exists, they return the same thing, but if the key is missing, computeIfAbsent returns the computed value, while putIfAbsent return null.

三、putIfAbsent 有可能把 null 塞进 map，但是 computeIfAbsent 则不会

Both method define "absent" as key missing or existing value is null, but:

computeIfAbsent will not put a null value if the key is absent.

putIfAbsent will put the value if the key is absent, even if the value is null.

It makes no difference for future calls to computeIfAbsent, putIfAbsent, and get calls, but it does make a difference to calls like getOrDefault and containsKey.



## 单词统计

我们要对一堆单词进行数量统计，常规的写法如下

```Java
public void countBefore8(){
    Map<String,Integer> wordCount=  new HashMap<>();
    String[] wordArray= new String[]{"we","are","the","world","we"};
    for(String word: wordArray){
        //如果存在则加1，否则将值设置为1
        if(wordCount.containsKey(word)) {
            wordCount.put(word, wordCount.get(word) + 1);
        }else{
            wordCount.put(word, 1);
        }
    }
}
```

前面讲过 JDK8 里新增的 compute 方法是为给定 key 计算一个新值，因此可以换种写法如下，简洁了一些

```Java
public void countAfter8WithCompute(){
    Map<String,Integer> wordCount=  new HashMap<>();
    String[] wordArray= new String[]{"we","are","the","world","we"};
    Arrays.asList(wordArray).forEach(word ->{
        wordCount.putIfAbsent(word,0);
        wordCount.compute(word,(w,count)->count+1);
    });
}
```

前面还讲过一个 merge 方法，如果 key 对应 oldValue 不存在，则返回给定 value（同时放进map）。如果 oldValue 存在，则调用 BiFunction 对 oldValue 和 给定 Value 进行合并。

```Java
public void countAfter8WithMerge(){
    Map<String,Integer> wordCount=  new HashMap<>();
    String[] wordArray= new String[]{"we","are","the","world","we"};
    Arrays.asList(wordArray).forEach(word->wordCount.merge(word, 1, (oldCount, one) -> oldCount + one));
}
```

## 参考

- https://www.baeldung.com/java-map-computeifabsent
