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
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.partition.HashPartitioner;
import org.apache.hadoop.util.GenericOptionsParser;

public class InvertedIndex {
    public static class InvertedMapper extends Mapper<Object, Text, Text, IntWritable> {
        private IntWritable one = new IntWritable(1);
        private Text word = new Text();

        public void map(Object key, Text values, Context context) throws IOException, InterruptedException {
            FileSplit filesplit = (FileSplit) context.getInputSplit();
            String fileName = filesplit.getPath().getName();
            int pos1 = fileName.indexOf(".");
            int pos2 = fileName.indexOf(".", pos1 + 1);
            if (pos1 > 0) {
                fileName = fileName.substring(0, pos1) + fileName.substring(pos1 + 1, pos2);
            }
            StringTokenizer itr = new StringTokenizer(values.toString());
            for (; itr.hasMoreTokens(); ) {
                word.set(itr.nextToken() + "#" + fileName);
                context.write(word, one);
            }
        }
    }

    public static class InvertedCombiner extends Reducer<Text, IntWritable, Text, IntWritable> {
        IntWritable result = new IntWritable();

        public void combine(Text key, Iterable<IntWritable> value, Context context) throws IOException, InterruptedException {
            int sum = 0;
            for(IntWritable val: value) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static class InvertedPartitioner extends HashPartitioner<Text, IntWritable> {
        public int  getPartition(Text key, IntWritable value, int numReduceTasks) {
            String term = value.toString().split("#")[0];
            return super.getPartition(new Text(term), value, numReduceTasks);
        }
    }

    public static class InvertedReducer extends Reducer<Text, IntWritable, Text, Text> {
        private String term = new String();
        private String last = "None";
        private int result;
        private int countDoc;
        private int countItem;
        private float f;
        private StringBuilder out = new StringBuilder();

        public void reduce(Text key, Iterable<IntWritable> value, Context context) throws IOException, InterruptedException {
            term = key.toString().split("#")[0];
            if (!term.equals(last)) {
                if(!last.equals("None")) {
                    out.setLength(out.length() - 1);
                    f = (float)countItem / countDoc;
                    context.write(new Text(last), new Text(String.format("%.2f %s", f, out.toString())));
                    countDoc = 0;
                    countItem = 0;
                    out = new StringBuilder();
                }
                last = term;
            }
            result = 0;
            for(IntWritable val : value) {
                result += val.get();
            }
            out.append(key.toString().split("#")[1] + ":" + result + ";");
            countDoc += 1;
            countItem += result;
        }

        public void cleanup(Context context) throws IOException, InterruptedException {
            out.setLength(out.length());
            f = (float)countItem / countDoc;
            context.write(new Text(last), new Text(String.format("%.2f %s", f, out.toString())));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = new Job(conf, "Job");
        job.setJarByClass(InvertedIndex.class);
        job.setMapperClass(InvertedMapper.class);
        job.setCombinerClass(InvertedCombiner.class);
        job.setNumReduceTasks(2);
        job.setPartitionerClass(InvertedPartitioner.class);
        job.setReducerClass(InvertedReducer.class);
        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(IntWritable.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));
        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
