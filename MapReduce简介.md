# MapReduce简介

#### 概要

MapReduce是处理和生成大数据集合的编程模型和相关实现。用户可以通过使用特定的**Map**和**Reduce**函数来完成对键值对**<key, value>**的处理。

**Map**函数主要是用于对处理键值对进行处理，生成中间键值对。**Reduce**函数主要用户依据中间键（intermediate key），来完成对中间值的归并（merge all intermediate values）。

MapReduce可以认为是一种编程框架，依据这种框架进行设计的程序可以自动完成并行化，并在大型集群上进行处理。运行时系统（run-time system）会自己考虑对输入数据进行划分，调度各部分机器的程序进行执行，处理机器出错。

#### 介绍

在MapReduce之前，也有一些并行化的编程方法。但是这些方法对程序员要求很高：既要求成员能对计算机底层有深刻了解，也需要过硬的编程本领。鉴于对此类方法长久的愤恨，又有感于Lisp和其他多种语言的*Map*与*Reduce*方法，谷歌大佬 Jeffrey Dean 和 Sanjay Ghemawat 发明了MapReduce。

#### 编程模型

在MapReduce中，计算任务的输入是键值对集合<key, value>，最后的结果产生的也是键值对集合。

在Map阶段，map函数有用户自己编写。它将键值对集合作为输入，并产生中间结果。这里的中间结果也是键值对集合。在中间过程中，会有一个Combiner发挥作用，它会将拥有相同关键字的键值进行运算处理，其对应的combiner函数可以是用户编写的。同时，Map函数完成，MapReduce还会对具有相同关键字的结果进行组合。

Combiner作用结果后，Partitioner函数会依据关键字将键值对发送到Reduce函数上，作为输入。MapReduce会有内置的sort函数运行，它会将键值对依据关键字进行排序。

需要明白的一点就是，在Map阶段，我们的输入输出是<key, value>键值对集合，但是在Reduce上，我们的输入是<key, List(value)>。也就是说，Reduce上的输入，是一个关键字对应多个值。这是由MapReduce内置的程序完成的，不需要认为干预，他是Map结束后依据关键字进行组合的结果。

在Reduce阶段，reduce函数也是由用户编写的函数。它接收中间结果作为输入，同一关键字的List，会议迭代器的形式传送给reduce函数。最后产生相应结果。如下图，就是一个简单例子。

  <img src="data:image/png;base64,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" />  

#### 原理简介

  ![1571199705075](C:\Users\Asmoc\AppData\Roaming\Typora\typora-user-images\1571199705075.png)  

##### 运行过程

Map装置分布在多个机器上，他们协同工作，自动将输入数据划分为$M$个划分。这些划分由不同的机器并行执行，Partitioner装置将Map的结果划分为$R$个部分，发送到Reduce装置上。Partitioner一般使用的函数可以是哈希函数。（$hash(key)\ mod\ R$）划分的数量（R）和划分函数（Partitioner）都可以由用户自己设置。Figure 1的执行流程解释如下：

（1）fork：用户程序中的MapReduce库会先将输入数据划分为$M$部分，每一个部分的大小大概是16M-64M，之后MapReduce会启动每个机器上的用户程序副本。

（2）assign map/reduce：在众多的用户程序副本中，有一个会很特别。他是众多程序的头头，称之为Master。其余的被称为worker。可以这样认为，一个worker结点或者master结点就是一个机器。master结点会往worker结点上分配map或者reduce。

（3）read：分配到map的worker结点会读取输入文件，产生中间结果，存储出内存中。

（4）local write：缓存在内存中的数据会被定期写入磁盘，之后会有partitioner函数做划分，数据在磁盘的信息会被告知master结点，再由master结点决定将其分配给哪个reduce结点。

（5）remote read：当一个reduce结点被告知存储于磁盘中的数据的位置之后，reduce会调用远程读取程序读取磁盘中的数据，当一个reduce结点读完存储在磁盘中的中间数据后，就会启动排序函数，依据关键字对数据排序。这样，sort函数是必须的，在apache hadoop中，他是内置函数，默认运行。原因在于，发送到同一个reduce中的数据的关键字可能不同，sort可以让同一关键字的数据都组合在一起。

（6）write：程序运行结束后，reduce结点会依次往同一文件顺次写入结果。

（7）当所有map、reduce任务结束后，master程序会唤醒用户程序，返回到用户代码。

##### Master数据结构

对于每一个map、reduce程序，master会存储其状态（空间、进行、完成），以及每一个正在工作的worker节点的识别码。

master是中间结果从map任务传输到reduce任务的管道。对于每一次map任务，master会存储其中间结果被划分后的位置和大小，并将此信息发送给reduce任务。

##### 容错

容错可以看作是mapreduce的天才之处。在大规模集群中，机器数量大，每一台机器都可能会出现故障，这是无法避免的，因而出错在mapreduce中被认为是常态。

##### 	Worker出错

master结点会周期性ping每一个worker结点，倘若结点没有回应则认为该结点出错。出错结点和已经完成任务的结点一样，会被初始化为空间状态，他们依然可以被其他MapReduce程序调用。

结点出错后，他们所执行的任务需要重新执行，因为他们的执行结果存储在其本地磁盘，一旦出错，其磁盘无法访问，因而需要重新执行。但是，reduce结点完成任务后出错则没有重新执行的必要，因为他们的数据存储在全局文件系统中。一旦一个节点的任务重新执行了，所有reduce结点都会收到消息。

其实这里可以引入一个问题：

###### 		为什么map执行后不直接把结果传送到reduce结点上进行处理，而是先存储在本地磁盘处理？

A：倘若，map直接将结果传送到Reduce节点上，一旦该结点出错需要重新执行，先前执行了一半数据已经传输出去了，无法收回，每个Reduce节点上都会有出错数据，这次的作业就会出现问题，整个作业都需要重新启动。但是，如果存储在本地，我们只需要重新执行出错任务即可。

##### 	Master出错

重新启动

##### 程序正确性保证

MapReduce的并行化执行，和程序顺序执行得到的结果是相同。MapReduce内部依赖map和reduce任务的原子提交。

##### 位置考量

MapReduce一个重要做法就是将计算向数据迁移，这样可以节省网络带宽。分布式文件系统会将每个文件划分为若干个64MB的块，并且为每个块保存3（一般为3）个副本在不同的机器上。master会优先安排文件所在的机器进行任务处理，其次才是靠近的机器。

##### 任务粒度

Map的数量M和Reduce的数量R应当远远大于Worker节点的数量。

##### 后备任务

在一个MapReduce任务快要完成的时候，master会调度正在运行的人物的后备任务进行执行。当后备任务或者原任务完成时，该任务被标记为已完成。

#### 技巧

##### Partitioning函数

默认是哈希函数，用户也可以手动更改。

##### 顺序保证

在Partitioner之后，会有内置sort函数发挥作用。

##### Combiner函数

map结果有大量重复的时候，combiner函数可以起到很好的节省带宽的作用。例如：一个map任务产生<a,1><a,1><a,1>，combiner函数可以将其变成<a,3>。一般而言，combiner函数可以和reduce函数拥有相同的实现，两者的不同就是combiner函数的输出为中间结果，reduce的输出为最终结果。combiner函数提高了执行的速度。同时，可以看到combiner是一个可选动作，因而，combiner和map的输出是同种类型的。

##### 输入输出格式

支持多种格式的输入输出

##### 副作用

会产生临时文件夹。不支持两阶段原子提交产生多个输出文件。

##### 跳过bad records

这是一个可选模式，若master发现某一条记录不断出错，就会跳过该记录。

##### 本地执行

帮助解决debug的问题

##### 状态信息

在特定网页上能够看到任务的执行状况

##### 计数器



#### Apache Hadoop

在hadoop中，主控节点不是master，被称之为**JobTracker**，它与分布式文件系统的主控结点**NameNode**一般位于同一个结点上，但也可以分开。worker结点被称之为**TaskTracker**，与分布式文件系统的**DataNode**位于同一个结点上。

在hadoop中，TaskTracker向JobTracker发送心跳信息。JobTracker不再主动问讯。

#### Reference

 [1] Dean J, Ghemawat S. MapReduce: simplified data processing on large clusters[J]. Communications of the ACM, 2008, 51(1): 107-113. 

[2] 黄宜华, 苗凯翔. 深入理解大数据-大数据处理与变成实践. 机械工业出版社