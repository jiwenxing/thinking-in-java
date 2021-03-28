# show-busy-java-threads 脚本使用
---

更多的使用姿势参考 https://github.com/superhj1987/awesome-scripts

```bash
# 注意已经将脚本上传到 sftp 了，因此使用的时候只需要在有问题的机器上登录 sftp get 一下即可
wget --no-check-certificate https://raw.githubusercontent.com/superhj1987/awesome-scripts/master/java/bin/show-busy-java-threads
# 线上机器直接 wget 是访问不了的
sftp jiwenxing@jumper.sankuai.com # 按照提示输入密码
sftp > get show-busy-java-threads # 完事执行 exit 退出 sftp
chmod u+x show-busy-java-threads
./show-busy-java-threads
```

使用示例，可以看到 hashmap 的 resize 占用了大量的 cpu

![](https://jverson.oss-cn-beijing.aliyuncs.com/db3735a1150042e172666e7eb85fe039.jpg)


我们来看看这段代码，重点关注一下添加注释的三行代码，我们知道 hashmap 是非线程安全的，主要体现在 resize 的过程中如果多线程并发可能会导致产生循环链从而造成 cpu 飙升，此类事故还比较常见。因此这里要么使用 ConcurrentHashMap，要么不要使用 parallelStream 进行并发操作。

```java
public static Map<Long, Map<String, Double>> postFeatureLoaderCosineCalc(Map<Long, List<Double>> poiEmbedVec,
                                                                             Map<String, Set<Long>> itemSessFeatureSet,
                                                                             Set<Long> predictItemList) {
        Transaction t = Cat.newTransaction("userFeature", "parseCosine");
        Map<Long, Map<String, Double>> itemParsedFeature = Maps.newHashMap(); // 创建了一个普通的 hashmap
        try {
            if (poiEmbedVec != null && MapUtils.isNotEmpty(poiEmbedVec) && MapUtils.isNotEmpty(itemSessFeatureSet)) {
                // ... ...
                predictItemList.parallelStream().forEach(poiId -> {  // 这里用了 parallelStream
                    Map<String, Double> sessFeatMap = Maps.newHashMap();
                    List<Double> poiIdEmbedding = poiEmbedVec.getOrDefault(poiId, null);
                    if (poiIdEmbedding != null) {
                        for (String featName : sessFeatNames) {
                            List<Double> poolEmbedding = poolEmbedMap.getOrDefault(featName, null);
                            if (poolEmbedding != null) {
                                Double cosineV = EmbeddingUtils.cosineSimilarity(poolEmbedding, poiIdEmbedding);
                                sessFeatMap.put("COS_" + featName, cosineV);
                            }
                        }
                        itemParsedFeature.put(poiId, sessFeatMap); // 这里进行 put
                    }
                });
                t.setSuccessStatus();
            }
        } catch (Exception e) {
            LOGGER.error("calc.cosine.error", e);
            t.setStatus(e);
        } finally {
            t.complete();
        }
        return itemParsedFeature;
    }
```
