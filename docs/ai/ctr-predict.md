# CTR 预估模型

---

在这篇文章开始之前建议先读一下知乎上这两篇文章，分别介绍深度学习之前的经典 CTR 模型演进路径，以及进入深度学习时代之后的模型演化图谱及在各大互联网公司的应用，写的很好，非常值得一读！另附上两张演化图供概览。

- [前深度学习时代 CTR 预估模型的演化之路](https://zhuanlan.zhihu.com/p/61154299)
- [深度学习CTR模型最全演化图谱](https://zhuanlan.zhihu.com/p/63186101)

图一，传统 CTR 模型演化图谱

![](https://jverson.oss-cn-beijing.aliyuncs.com/2d69943420e4942fbb3727314ecd821d.jpg)

图二，深度学习 CTR 模型演化图谱

![](https://jverson.oss-cn-beijing.aliyuncs.com/33629db821b9c3a83673a97e05d3b9d7.jpg)

---

学习和预测用户的反馈（CTR/CVR等）对于个性化推荐、信息检索和在线广告等领域都有着极其重要的作用。在这些领域，用户的反馈行为包括点击、收藏、购买等。

## 原始特征

CTR 预估模型的原始特征数据通常包括多个类别，比如 [Weekday=Tuesday,
Gender=Male, City=London, CategoryId=16]，这些原始特征通常以独热编码（one-hot encoding）的方式转化为高维稀疏二值向量，多个域（类别）对应的编码向量链接在一起构成最终的特征向量。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/b585c0c3-7465-41da-8c35-c71dbb1c68ea)

如上，**高维、稀疏、多Field**是输入给CTR预估模型的特征数据的典型特点。

## 特征 Embedding 表示

Embedding 表示也叫做 Distributed representation（分布式表示，与 one-hot 对应），起源于神经网络语言模型（NNLM）对语料库中的 word 的一种表示方法。相对于高维稀疏的 one-hot 编码表示，embedding-based 的方法，学习一个低维稠密实数向量（low-dimensional dense embedding）。有点类似于 hash 方法，embedding 方法把位数较多的稀疏数据压缩到位数较少的空间，当然不可避免会有冲突。然而，embedding 学到的是类似主题的语义表示，对于item的“冲突”是希望发生的（冲突表示其语义相同），这有点像软聚类，这样才能解决稀疏性的问题。

Google 公司开源的 word2vec 工具让 embedding 表示方法广为人知。Embedding 表示通常用神经网络模型来学习，当然也有其他学习方法，比如矩阵分解（MF）、因子分解机（FM)等。这里详细介绍一下基于神经网络的 embedding 学习方法。

通常 Embedding 向量并不是通过一个专门的任务学习得到的，而是其他学习任务的附属产出。如下图所示，网络的输入层是实体ID（categorical特征）的 one-hot 编码向量。与输入层相连的一层就是 Embedding 层，两层之间通过全连接的方式相连。Embedding 层的神经元个数即 Embeeding 向量的维数（m 维）。输入层与 Embedding 层的链接对应的权重矩阵 M(n*m) ，即对应 n 个输入实体的 m 维embedding 向量。由于 one-hot 向量同一时刻只会有一个元素值为 1，其他值都是 0，因此对于当前样本，只有与值为1的输入节点相连的边上的权重会被更新，即不同 ID 的实体所在的样本训练过程中只会影响与该实体对应的 embedding 表示。假设某实体 ID 的 one-hot 向量中下标为 i 的值为1，则该实体的 embedding 向量为权重矩阵 M 的第 i 行。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/03f30521-bc57-4093-84a2-7f282e715bcd)


## CTR 常用模型

### 逻辑回归 LR 模型（Logistic Regression）

LR 模型一直是 CTR 预估问题的 benchmark 模型，由于其简单、易于并行化实现、可解释性强等优点而被广泛使用。然而由于线性模型本身的局限，不能处理特征和目标之间的非线性关系，因此模型效果严重依赖于算法工程师的特征工程经验。

为了让线性模型能够学习到原始特征与拟合目标之间的非线性关系，通常需要对原始特征做一些非线性转换。常用的转换方法包括：连续特征离散化、特征之间的交叉等。

连续特征离散化的方法一般是把原始连续值的值域范围划分为多个区间，比如等频划分或等间距划分，更好的划分方法是利用监督学习的方式训练一个简单的单特征的决策树桩模型，即用信息增益指标来决定分裂点。特征分区间之后，每个区间上目标（y）的分布可能是不同的，从而每个区间对应的新特征在模型训练结束后都能拥有独立的权重系数。特征离散化相当于把线性函数变成了分段线性函数，从而引入了非线性结构。比如不同年龄段的用户的行为模式可能是不同的，但是并不意味着年龄越大就对拟合目标（比如，点击率）的贡献越大，因此直接把年龄作为特征值训练就不合适。而把年龄分段后，模型就能够学习到不同年龄段的用户的不同偏好模式。离散化的其他好处还包括对数据中的噪音有更好的鲁棒性（异常值也落在一个划分区间，异常值本身的大小不会过度影响模型预测结果）；离散化还使得模型更加稳定，特征值本身的微小变化（只有还落在原来的划分区间）不会引起模型预测值的变化。

特征交叉是另一种常用的引入非线性性的特征工程方法。通常 CTR 预估涉及到用户、物品、上下文等几方面的特征，往往单个特征对目标判定的贡献是较弱的，而不同类型的特征组合在一起就能够对目标的判定产生较强的贡献。比如用户性别和商品类目交叉就能够刻画例如“女性用户偏爱美妆类目”，“男性用户喜欢男装类目”的知识。特征交叉是算法工程师把领域知识融入模型的一种方式。

LR 模型的不足在于特征工程耗费了大量的精力，而且即使有经验的工程师也很难穷尽所有的特征交叉组合。

更多 LR 相关细节可以参考：[逻辑回归（LR） 算法模型简介](https://blog.csdn.net/hzwaxx/article/details/83861782)

### LR + GBDT（Gradient Boost Decision Tree，梯度提升决策树）

既然特征工程很难，那能否自动完成呢？模型级联提供了一种思路，典型的例子就是 Facebook 2014 年的论文中介绍的通过 GBDT（Gradient Boost Decision Tree）模型解决 LR 模型的特征组合问题。思路很简单，特征工程分为两部分，一部分特征用于训练一个 GBDT 模型，把 GBDT 模型每颗树的叶子节点编号作为新的特征，加入到原始特征集中，再用 LR 模型训练最终的模型。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/2125fa0c-727e-4ec1-91b2-8913de38cd73)

GBDT 模型能够学习高阶非线性特征组合，对应树的一条路径（用叶子节点来表示）。通常把一些连续值特征、值空间不大的 categorical 特征都丢给 GBDT 模型；空间很大的 ID 特征（比如商品ID）留在 LR 模型中训练，既能做高阶特征组合又能利用线性模型易于处理大规模稀疏数据的优势。

### FM、FFM

因子分解机(Factorization Machines, FM)通过特征对之间的隐变量内积来提取特征组合，其函数形式如下：
![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/9ef23510-9b2b-4c80-9447-558325758bcb)

FM 和基于树的模型（e.g. GBDT）都能够自动学习特征交叉组合。基于树的模型适合连续**中低度稀疏数据**，容易学到高阶组合。但是树模型却不适合学习高度稀疏数据的特征组合，一方面高度稀疏数据的特征维度一般很高，这时基于树的模型学习效率很低，甚至不可行；另一方面树模型也**不能学习到训练数据中很少或没有出现的特征组合**。相反，FM模型因为通过隐向量的内积来提取特征组合，对于训练数据中很少或没有出现的特征组合也能够学习到。例如，特征 i 和特征 j 在训练数据中从来没有成对出现过，但特征 i 经常和特征 p 成对出现，特征 j 也经常和特征 p 成对出现，因而在 FM 模型中特征 i 和特征 j 也会有一定的相关性。毕竟所有包含特征 i 的训练样本都会导致模型更新特征 i 的隐向量 Vi，同理，所有包含特征 j 的样本也会导致模型更新隐向量 Vj ，这样 <Vi, Vj> 就不太可能为0。

在推荐系统中，常用矩阵分解（MF）的方法把 User-Item 评分矩阵分解为两个低秩矩阵的乘积，这两个低秩矩阵分别为 User 和 Item 的隐向量集合。通过 User 和 Item 隐向量的点积来预测用户对未见过的物品的兴趣。矩阵分解也是生成 embedding 表示的一种方法，示例图如下：

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/26263e72-3b17-485f-b177-60a1e4c52f40)


MF 方法可以看作是 FM 模型的一种特例，即 MF 可以看作特征只有 userId 和 itemId 的 FM 模型。FM 的优势是能够将更多的特征融入到这个框架中，并且可以同时使用一阶和二阶特征；而 MF 只能使用两个实体的二阶特征。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/7a97cb30-87bd-4ee0-a38c-0166d8a82941)

FM 模型可以看做是 LR 模型和 MF 方法的融合，如下图所示：

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/5806bcea-814d-42ad-9347-acffa7c46e00)

FFM（Field-aware Factorization Machine）模型是对FM模型的扩展，通过引入field的概念，FFM把相同性质的特征归于同一个field。例如，“Day=26/11/15”、 “Day=1/7/14”、 “Day=19/2/15”这三个特征都是代表日期的，可以放到同一个field中。同理，商品的末级品类编码也可以放到同一个field中。简单来说，同一个categorical特征经过One-Hot编码生成的数值特征都可以放到同一个field，包括用户性别、职业、品类偏好等。

### 混合逻辑回归（MLR）

MLR 算法是 alibaba 在2012年提出并使用的广告点击率预估模型，2017 年发表出来。MLR 模型是对线性 LR 模型的推广，它利用分片线性方式对数据进行拟合。基本思路是采用分而治之的策略：如果分类空间本身是非线性的，则按照合适的方式把空间分为多个区域，每个区域里面可以用线性的方式进行拟合，最后 MLR 的输出就变为了多个子区域预测值的加权平均。如下图(C)所示，就是使用 4 个分片的 MLR 模型学到的结果。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/0f1d52e9-f07a-41c4-a516-40c950b93e4f)


MLR 模型在大规模稀疏数据上探索和实现了非线性拟合能力，在分片数足够多时，有较强的非线性能力；同时模型复杂度可控，有较好泛化能力；同时保留了 LR 模型的自动特征选择能力。

MLR 模型可以看作带有一个隐层的神经网络。如下图，x 是大规模的稀疏输入数据，MLR 模型第一步是做了一个 Embedding 操作，分为两个部分，一种叫聚类 Embedding（绿色），另一种是分类 Embedding（红色）。两个投影都投到低维的空间，维度为 m ，是 MLR 模型中的分片数。完成投影之后，通过很简单的内积（Inner Product）操作便可以进行预测，得到输出 y 。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/f4270a0f-3bac-4e55-910f-0b556aefe972)

###  WDL（Wide & Deep Learning）

像 LR 这样的 wide 模型学习特征与目标之间的直接相关关系，偏重记忆（memorization），如在推荐系统中，wide 模型产生的推荐是与用户历史行为的物品直接相关的物品。这样的模型缺乏刻画特征之间的关系的能力，比如模型无法感知到“土豆”和“马铃薯”是相同的实体，在训练样本中没有出现的特征组合自然就无法使用，因此可能模型学习到某种类型的用户喜欢“土豆”，但却会判定该类型的用户不喜欢“马铃薯”。

WDL 是 Google 在2016年的 paper 中提出的模型，其巧妙地将传统的特征工程与深度模型进行了强强联合。模型结构如下:

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/b0c1f8df-6e26-4d4e-a0ff-9df9b7931b02)

WDL 分为 wide 和 deep 两部分联合训练，单看 wide 部分与 LR 模型并没有什么区别；deep 部分则是先对不同的 ID 类型特征做embedding，在 embedding 层接一个全连接的 MLP（多层感知机），用于学习特征之间的**高阶交叉组合关系**。由于 Embedding 机制的引入，WDL 相对于单纯的 wide 模型有更强的泛化能力。

### FNN (Factorization-machine supported Neural Network)

通过上面对 FM 的介绍我们知道除了神经网络模型，FM 模型也可以用来学习到特征的隐向量（embedding 表示），因此一个自然的想法就是先用 FM 模型学习到特征的 embedding 表示，再用学到的 embedding 向量代替原始特征作为最终模型的特征。这个思路类似于LR+GBDT，整个学习过程分为两个阶段：第一个阶段先用一个模型做特征工程；第二个阶段用第一个阶段学习到新特征训练最终的模型。

FNN 模型就是用 FM 模型学习到的 embedding 向量初始化 MLP，再由 MLP 完成最终学习，其模型结构如下：

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/ab2749d0-0d5c-4488-bdaf-5bd0d830c90f)

### PNN（Product-based Neural Networks）

MLP 中的节点 add 操作可能不能有效探索到不同类别数据之间的交互关系，虽然 MLP 理论上可以以任意精度逼近任意函数，但越泛化的表达，拟合到具体数据的特定模式越不容易。PNN 主要是在深度学习网络中增加了一个 inner/outer product layer，用来建模特征之间的关系。

### DeepFM

深度神经网络对于学习复杂的特征关系非常有潜力。目前也有很多基于 CNN（卷积神经网络） 与 RNN（循环神经网络） 的用于CTR预估的模型。但是基于 CNN 的模型比较偏向于相邻的特征组合关系提取，基于 RNN 的模型更适合有序列依赖的点击数据。

FNN 模型首先预训练 FM，再将训练好的 FM 应用到 DNN 中。PNN 网络的 embedding 层与全连接层之间加了一层 Product Layer 来完成特征组合。PNN 和 FNN 与其他已有的深度学习模型类似，都很难有效地提取出低阶特征组合。WDL 模型混合了宽度模型与深度模型，但是宽度模型的输入依旧依赖于特征工程。

上述模型要不然偏向于低阶特征或者高阶特征的提取，要不然依赖于特征工程。而 DeepFM 模型可以以端对端的方式来学习不同阶的组合特征关系，并且不需要其他特征工程。

DeepFM 的结构中包含了因子分解机部分以及深度神经网络部分，分别负责低阶特征的提取和高阶特征的提取。其结构如下：

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/9ae1765a-3178-4dd4-a4ba-a90e1803655d)

与 Wide&Deep Model 不同，DeepFM 共享相同的输入与 embedding 向量。在 Wide&Deep Model 中，因为在 Wide 部分包含了人工设计的成对特征组，所以输入向量的长度也会显著增加，这也增加了复杂性。

### DIN

DIN 是阿里 2017 年的论文中提出的深度学习模型，该模型基于对用户历史行为数据的两个观察：1、多样性，一个用户可能对多种品类的东西感兴趣；2、部分对应，只有一部分的历史数据对目前的点击预测有帮助，比如系统向用户推荐泳镜时会与用户点击过的泳衣产生关联，但是跟用户买的书就关系不大。于是，DIN 设计了一个 attention 结构，对用户的历史数据和待估算的广告之间部分匹配，从而得到一个权重值，用来进行 embedding 间的加权求和。

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/23290927-637d-4792-b113-450fd98877d8)

DIN 模型的输入分为 2 个部分：用户特征和广告(商品)特征。用户特征由用户历史行为的不同实体 ID 序列组成。在对用户的表示计算上引入了 attention network (也即图中的 Activation Unit) 。DIN 把用户特征、用户历史行为特征进行 embedding 操作，视为对用户兴趣的表示，之后通过 attention network，对每个兴趣表示赋予不同的权值。这个权值是由用户的兴趣和待估算的广告进行匹配计算得到的，如此模型结构符合了之前的两个观察：用户兴趣的多峰分布以及部分对应。

下面图片来自：[CTR预估模型发展过程与关系图谱
](https://zhuanlan.zhihu.com/p/104307718)

## CTR预估模型关系图谱

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/f7ad0905-1ec6-4299-b344-f9b958d60098)

从上往下，代表了整个 CTR 预估的发展趋势：

LR 的主要限制在于需要大量手动特征工程来间接提高模型表达，此时出现了两个发展方向：

- 以 FM 为代表的端到端的隐向量学习方式，通过embedding来学习二阶交叉特征
- 以 GBDT+LR 为代表的两阶段模型，第一阶段利用树模型优势自动化提取高阶特征交叉，第二阶段交由 LR 进行最终的学习

以 FM 为结点，出现了两个方向：

- 以 FFM 与 AFM 为代表的浅层模型改进。这两个模型本质上还是学习低阶交叉特征，只是在 FM 基础上为不同的交叉特征赋予的不同重要度
- 深度学习时代到来，依附于 DNN 高阶交叉特征能力的 Embedding+MLP 结构开始流行

以 Embedding+MLP 为结点：

- Embedding 层的改造 + DNN 进行高阶隐式学习，出现了以 PNN、NFM 为代表的 product layer、bi-interaction layer 等浅层改进，这一类模型都是对 embedding 层进行改造来提高模型在浅层表达，减轻后续 DNN 的学习负担
- 以 W&D 和 DeepFM 为代表的双路模型结构，将各个子模块算法的优势进行互补，例如 DeepFM 结合了 FM 的低阶交叉信息和 DNN 的高阶交叉信息学习能力
- 显式高阶特征交叉网络的提出，这一阶段以更复杂的网络方式来进行显式交叉特征的学习，例如 DCN 的 CrossNet、xDeepFM 的 CIN、AutoInt 的 Multi-head Self-attention 结构

从整个宏观趋势来看，每一阶段新算法的提出都是在不断去提升模型的表达能力，从二阶交叉，到高阶隐式交叉，再到如今的高阶显示交叉，模型对于原始信息的学习方式越来越复杂的同时，也越来越准确。


图中右侧红色字体提取了部分模型之间的共性：

- Hand-crafted features：LR与W&D都需要进行手动的特征工程
- Non-end-to-end：GBDT+LR通过树模型提取特征+LR建模的两阶段，FNN则是FM预训练embedding+DNN建模的两阶段方式，这两者都是非端到端的模型
- Multi-embeddings：这里是指对于同一个特征，使用多个embedding来提升信息表达。包括FFM的Field-aware，ONN的Operation-aware
- Attention：Attention机制为CTR预估中的交叉特征赋予了不同的重要性，也增加了一定的可解释性。AFM中采用单个隐藏层的神经网络构建attention层，AutoInt在Interacting Layer中采用NLP中QKV形式学习multi-head self-attention
- Explicitly Interactions：DNN本身学习的是隐式特征交叉，DCN、xDeepFM、AutoInt则都提出了显式特征交叉的网络结构
- ResNet：ResNet的引入是为了保留历史的学习到的信息，CrossNet与AutoInt中都采用了ResNet结构

## 总结

主流的 CTR 预估模型已经从传统的宽度模型向深度模型转变，与之相应的人工特征工程的工作量也逐渐减少。上文提到的深度学习模型，除了 DIN 对输入数据的处理比较特殊之外，其他几个模型还是比较类似的，它们之间的区别主要在于网络结构的不同，常见 CTR 预估模型特性对比见下表：

![image.png](https://images.zenhubusercontent.com/5b83aeb622e474383b984d11/1091d6f1-2bfd-4bde-a7ba-8406efa06e52)

- No Pretraining：是否需要预训练
- Automatic Feature Engineering：是否自动进行特征组合与特征工程
- End-To-End：是否是端到端的模型
- Low-Order Features：是否包含低阶特征信息
- High-Order Features：是否包含高阶特征信息
- Explicitly High-Order Crossing：是否包含显式特征交叉

另外深度学习技术主要有三点优势。
- 第一点，模型设计组件化。组件化是指在构建模型时，可以更多的关注idea和motivation本身，在真正数学化实现时可以像搭积木一样进行网络结构的设计和搭建。
- 第二点，优化方法标准化。在2010年以前，Machine Learning还是一个要求较高的领域。它要求不仅了解问题、能定义出数学化的formulation，而且需要掌握很好的优化技巧，针对对应的问题设计具体的优化方法。但是在现在，深度学习因为模型结构上的变化，使得工业界可以用标准的SGD或SGD变种，很轻松的得到很好的优化解。
- 第三点，深度学习可以帮助我们实现设计与优化的解耦，将设计和优化分阶段进行。对于工业界的同学来说，可以更加关注从问题本身出发，抽象和拟合领域知识。然后用一些标准的优化方法和框架来进行求解。

备注：全篇文章来自文章 [主流CTR预估模型的演化及对比
](https://zhuanlan.zhihu.com/p/35465875)