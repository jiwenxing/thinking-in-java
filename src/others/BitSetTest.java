package others;

import org.junit.jupiter.api.Test;

import java.util.BitSet;

public class BitSetTest {

@Test
public void test1() {
    BitSet bitSet = new BitSet(); // 初始化了一个长度为 1 的 long 数组，即 long[1], 值为 [0]
    bitSet.set(4); // 将 long[] 第一个 long 64bit 的第四位置为 1，这时候 long[1] 十进制就是 [16]
    bitSet.set(129); // long[1] 只有 64 位已经放不了 129，因此数组扩容到 long[3] 有 192 位就可以设置 129 了，显然 129 会被放在 long[] 第三个元素的第 2bit 位
    // 因此这个时候 long[3] 变成了 [16,0,2]

    BitSet bitSet1 = bitSet.get(0, 6); // 截取一定区间 bits 的 bitSet，相当于 list 的 subList(from, to)

    bitSet.set(65, 67); // [16,6,2]

    int length = bitSet.length(); // 130，logical size = the index of the highest set bit plus one，感觉就是最大的有值 bit 位下标加 1， 此时 bitSet = {4, 65, 66, 129}，words=[16,6,2]

    bitSet1.set(1);
    bitSet1.and(bitSet); // (a & b) 与运算，会改变 bitSet1 的值
    bitSet1.andNot(bitSet);  // (a & !b)，同理还支持 or、xor 异或等位运算

    bitSet1.intersects(bitSet); // 判断是否有交集，即是否有相同的 bitIndex 同时为 true

    int cardinality = bitSet.cardinality(); // 元素个数，bit 位为 1 的个数，the number of bits set to true

    BitSet bitSet2 = new BitSet();
    bitSet2.set(1); // {1}
    bitSet2.set(3, true); // {1, 3}, 等同于 bitSet2.set(3), 如果是 false 就等同于 bitSet2.clear(3)
    bitSet2.flip(1); // {3} 反转第一位
    bitSet2.flip(0, 8); // {0, 1, 2, 4, 5, 6, 7} 反转 0 到 8 bit 位

    // 使用 nextSetBit 遍历 bitSet，同理还有 nextClearBit、previousClearBit、previousSetBit
    for (int i = bitSet2.nextSetBit(0); i >= 0; i = bitSet2.nextSetBit(i+1)) {
          // operate on index i here
          if (i == Integer.MAX_VALUE) {
              break; // or (i+1) would overflow
          }
    }
}

}
