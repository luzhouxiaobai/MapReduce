# 理解WordCount

Apache Hadoop使用JAVA语言编程，对于使用java语言的程序员来说，这很友好。一般而言，一个hadoop程序主要分为两个部分：Map和Reduce。

我们可以观察一下其执行流：

Map $\to$ Combiner $\to$ Sort $\to$ Partitioner $\to$ Reduce

下面我们以一个例子来阐述wordcount的执行流如何发生。

假设一个输入文本如下：

------

```
ja dj nag 
dkda ng nd 
ddnjg  ndj
```

------

先不考虑mapreduce，若我们需要进行单词计数，本能的，我们希望将每个单词拎出来，一一进行计数。那么我们编程序，自然也希望先将文本中的单词一个个拆出来，然后再进行统计。

那什么是mapreduce呢？简单来说，将这个文本拆成n份（Map阶段，做拆分），然后让n个工人对这n份文本单独进行统计，最后将n个人的结果进行合计（Reduce阶段，进行统计），则得出这样的结果。

有了这样的认识，我们依次来看wordcount的代码：

Mapper: map程序是需要编程实现了。 

- [x] ```java
  public static class TokenizerMapper 
      extends Mapper<Object, Text, Text, IntWritable> {    
      private final static IntWritable one = new IntWritable(1);    
      private Text word = new Text();
      
      public void map(Object key, Text value, Context context) 
          throws IOException, InterruptedException {        
          StringTokenizer itr = new StringTokenizer(value.toString());        
          while (itr.hasMoreTokens()) {            
              word.set(itr.nextToken());            
              context.write(word, one);        
          }    
      }
  }
  ```

  上述代码展示的就是WordCount中的Map阶段。首先，我们建立继承自Mapper类的TokenizerMapper类，尖括号中的四个类型分别表示：输入的Key，输入的Value，输出的Key，输出的Value，的类型。

  在Hadoop中，输入以文件形式输入，一般输入的Key就是行号，故为Object类型。输入的Value为该行的内容，一般为文本，故为Text类型。

  在wordcount中，我们希望中间结果应该统计好的，每小份文本中的单词数量，形如：

  ------

  ja 1

  dj 1

  nag 1

  …..

  ------

  因而，输入的结果的Key应该是单词，类型为Text，输出为正数，类型为IntWritable。

  有人可能纳闷，这个程序和普通的java程序差别也不大嘛。

  确实差别不大。仔细想想，一个人统计单词和n个人统计单词，统计单词的动作难道不应该是一样的嘛？所以思考这个程序的时候，我们应该要想象这个程序是运行在n个机器上的，就像n个人在统计单词一样。

  ------

  ------

  Reducer: reduce程序实现如下：

  ```java
  public static class IntSumReducer        
      extends Reducer<Text, IntWritable, Text, IntWritable> {    
      private IntWritable result = new IntWritable();    
      public void reduce(Text key, Iterable<IntWritable> values,                       Context context    ) throws IOException, InterruptedException {        
          int sum = 0;        
          for (IntWritable val : values) {            
              sum += val.get();        
          }        
          result.set(sum);        
          context.write(key, result);    
      }
  }
  ```

Reducer就是进行一个统计工作。输入来自Mapper的输出，所以Key是单词，即为Text类型。Value为单词数量，即IntWritable。输出的理解和Mapper的理解相同，这里也就不赘述。

有一点需要说明，输入的Value是迭代器，形式上看应该类似于如下形式：

k 1 2 3 4

表示有4个Mapper程序给该Reducer结点发送了k的数量，也就是说有4个工人统计到了k，值分别为1个，2个，3个，4个。Reducer端对这些量进行统计，得到结果。

至此，WordCount的程序代码基本上也就理解结束了，但是从执行流程来看，还有许多没被我们发现的部分：

------

------

首先是Combiner。可以把他看成一个小的Reducer，他仅在当前Mapper结点上进行统计，什么意思？

观察Mapper程序，你会发现，Mapper得到的结果应该是类似于：

------

k 1

k 1

b 1

b 1

…

------

可以看到，Map仅作拆分，并不会做统计。但是想想，如果我们直接将10000个甚至更多的 k 1 发送到Reducer上，是极其带宽的，如果我们只发一个 k 100000 传输速度会快很多。基于这样的想法，我们利用了一个Combiner程序。在WordCount中，Combiner和Reducer的程序应该是相同的，所以不用重新写。当然，有的程序中可能并不需要Combiner，因而Combiner的加入不能改变Map的输出格式。

------

------

Prititioner部分，他是默认执行的部分。有的人会疑惑，服务器是怎么保证将相同关键字的<key value>对发送到一个服务器上的。这个就是由Partitioner完成。他有默认的程序，就是按照key进行分类，但是也可以人工定制。它这部分完成的工作，称为Shuffle。

------

------

Sort部分也是默认执行的。Combiner(如果有该过程)或者Mapper(没有Combiner过程)结束后，系统就会自动启动Sort函数，按关键字排序，我们也可以通过在Partitioner部分改变key来改变排序的依据。

------

------

在主函数部分：

首先完成配置，给Job命名。

```java
Configuration conf = new Configuration();
Job job = Job.getInstance(conf, "word count");
```

之后，先指定该工程的类，再依次指出各执行流的流（Sort不用，其他若有则要设置）：

```java
job.setJarByClass(WordCount.class);
job.setMapperClass(TokenizerMapper.class);
job.setCombinerClass(IntSumReducer.class);
job.setReducerClass(IntSumReducer.class);
```

最后设置输出格式，以及输入输出路径：

```java
job.setOutputKeyClass(Text.class);
job.setOutputValueClass(IntWritable.class);
FileInputFormat.addInputPath(job, new Path(args[0]));
FileOutputFormat.setOutputPath(job, new Path(args[1]));
System.exit(job.waitForCompletion(true) ? 0 : 1);
```

需要提醒：

输出文件夹实现不能存在（这个没有原因，工程经验得出的结果）。

整个过程变量变化为：

Text $\to$ <Key Value> $\to$ key list(value) $\to$ results

给出完整的WordCount代码：

```java
import java.io.IOException;
import java.util.StringTokenizer;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
public class WordCount {    
    public static class TokenizerMapper            
        extends Mapper<Object, Text, Text, IntWritable> {        
        private final static IntWritable one = new IntWritable(1);        
        private Text word = new Text();        
        public void map(Object key, Text value, Context context        
                       ) throws IOException, InterruptedException {            
            StringTokenizer itr = new StringTokenizer(value.toString());            
            while (itr.hasMoreTokens()) {                
                word.set(itr.nextToken());                
                context.write(word, one);            
            }        
        }    
    }    
    public static class IntSumReducer            
        extends Reducer<Text, IntWritable, Text, IntWritable> {        
        private IntWritable result = new IntWritable();        
        public void reduce(Text key, Iterable<IntWritable> values,                           Context context        
                          ) throws IOException, InterruptedException {            
            int sum = 0;            
            for (IntWritable val : values) {                
                sum += val.get();            
            }            
            result.set(sum);            
            context.write(key, result);        
        }    
    }    
    public static void main(String[] args) throws Exception {        
        Configuration conf = new Configuration();        
        Job job = Job.getInstance(conf, "word count");        
        job.setJarByClass(WordCount.class);        
        job.setMapperClass(TokenizerMapper.class);        
        job.setCombinerClass(IntSumReducer.class);        
        job.setReducerClass(IntSumReducer.class);        
        job.setOutputKeyClass(Text.class);        
        job.setOutputValueClass(IntWritable.class);        
        FileInputFormat.addInputPath(job, new Path(args[0]));        
        FileOutputFormat.setOutputPath(job, new Path(args[1]));        
        System.exit(job.waitForCompletion(true) ? 0 : 1);    
    }
}
```

对应我上述的输入，得出的输出为：

```
d   1
da 1
dj 1
dk 1
dnjg 1
ja 1
nag  1
nd 1
ndj  1
ng 1
```