# Shell 脚本编程

---

Shell 是一个用 C 语言编写的程序，它是用户使用 Linux 的桥梁。它本质就是一个 Linux 上的应用程序，也算是一种程序设计语言。当然 Linux 的 Shell 种类也很多，而我们一般指的是大多数 Linux 系统默认 Bourne Again Shell（/bin/bash），即所谓的 Bash Shell。脚本第一行一般标注为 `#!/bin/sh` 或 `#!/bin/bash` 意思就是表示告诉系统其后路径所指定的程序即是解释此脚本文件的 Shell 程序。



## 脚本运行

当我们创建了一个 shell 文件 learn.sh，有两种方式可以执行

1. sh learn.sh 直接将脚本文件作为解释器参数传给 sh 命令，注意这种方式不必事先设置脚本的执行权限
2. ./learn.sh 这种方式需要使脚本具有可执行权限才可以，一般需要提前执行 `chmod +x learn.sh`

## Shell 变量

定义变量等号两边不能有空格，使用变量前面加 `$` ，大括号可选（推荐所有变量都加大括号，养成良好编程习惯）

```shell
## shell 变量
name=jverson
echo $name # jverson
echo ${name} # jverson
readonly name # 将变量设为只读，类似于 Java 中的 final 变量
name=other # error，name: readonly variable

age=18
echo ${age}
unset age # 删除变量
echo ${age} # 输出空

## shell 字符串，可用单引号和双引号，也可以不用引号，注意他们是有区别的
# 单引号中不能使用变量，所有字符原样输出；双引号可以有变量也可以有转义字符
echo '$age ${age} 'age'' # 输出 $age ${age} age
echo "$age ${age} \"$age\"" # 输出 18 18 "18" # 一般场景就使用双引号就行

name=jverson
echo ${#name} # 输出字符串长度 7
echo ${name:0:3} # 输出 jve，字符串截断，参数分别为 index，length


```

