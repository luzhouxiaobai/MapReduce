import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;

public class Matrix {
    public static class MatrixMapper extends Mapper <Object, Text, Text, Text> {
        //定义私有变量，作为后续的写入内容
        private Text map_key = new Text();
        private Text map_value = new Text();
        int columnN;
        int rowM;

        public void setup (Context context) throws IOException {
            Configuration conf = context.getConfiguration();
            columnN = conf.getInt("columnN", 4);//N的列数
            rowM = conf.getInt("rowM", 4);
        }
        public void map (Object key, Text value, Context context) throws IOException, InterruptedException {
            //首先要得到文件名，因为文件名中包含矩阵的大小
            FileSplit fileSplit = (FileSplit) context.getInputSplit();
            String fileName = fileSplit.getPath().getName(); //得到文件名

            if (fileName.contains("M")) {
                String[] tuple = value.toString().split(",");
                int i = Integer.parseInt(tuple[0]);
                String[] tuples = tuple[1].split(" ");
                int j = Integer.parseInt(tuples[0]);
                int Mij = Integer.parseInt(tuples[1]);
                for (int k = 1; k <columnN+1; k++) {
                    map_key.set(i + "," + k);
                    map_value.set("M" + "," + j + "," + Mij); //一个Mij要与N中的很多元素相乘
                    context.write(map_key, map_value);
                }
            }
            else if (fileName.contains("N")) {
                String[] tuple = value.toString().split(",");
                int j = Integer.parseInt(tuple[0]);
                String[] tuples = tuple[1].split(" ");
                int k = Integer.parseInt(tuples[0]);
                int Njk = Integer.parseInt(tuples[1]);
                for (int t = 1; t <rowM+1; t++) {
                    map_key.set(t + "," + k);
                    map_value.set("N" + "," + j + "," + Njk); //一个Mij要与N中的很多元素相乘
                    context.write(map_key, map_value);
                }
            }
        }
    }

    public static class MatrixReducer extends Reducer<Text, Text, Text, Text> {
        int columnM;
        private int sum = 0;
        public void setup (Context context) throws IOException {
            Configuration conf = context.getConfiguration();
            columnM = conf.getInt("columnM", 4);//M的列
        }

        public void reduce (Text key, Iterable<Text> value, Context context) throws IOException, InterruptedException {
            int[] M = new int[columnM+1];
            int[] N = new int[columnM+1];
            //value为： M,1,34
            for (Text val: value) {
                String[] tuples = val.toString().split(",");
                if (tuples[0].equals("M")) {
                    M[Integer.parseInt(tuples[1])] = Integer.parseInt(tuples[2]);
                }
                else {
                    N[Integer.parseInt(tuples[1])] = Integer.parseInt(tuples[2]);
                }
            }
            for (int j=1; j<columnM+1; j++) {
                sum += M[j] * N[j];
            }
            context.write(key, new Text(Integer.toString(sum)));
            sum = 0;
        }
    }

    public static void main (String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Matrix Computation");
        conf.setInt("columnN", 4);
        conf.setInt("rowM", 4);
        conf.setInt("columnM", 4);
        job.setJarByClass(Matrix.class);
        job.setMapperClass(MatrixMapper.class);
        job.setReducerClass(MatrixReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);
        FileInputFormat.addInputPath(job, new Path("input"));
        FileOutputFormat.setOutputPath(job, new Path("output"));
        System.exit(job.waitForCompletion(true) ? 0:1);
    }
}
