import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;

import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;

import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class Task1_CitySales {

    public static class CitySalesMapper
            extends Mapper<Object, Text, Text, DoubleWritable> {

        private Text city = new Text();
        private DoubleWritable cost = new DoubleWritable();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            // Skip header
            if (line.startsWith("Transaction_ID")) {
                return;
            }

            String[] fields = line.split(",");

            if (fields.length >= 13) {

                try {
                    String cityName = fields[7].trim();
                    String totalCost = fields[5].trim();

                    double costValue = Double.parseDouble(totalCost);

                    city.set(cityName);
                    cost.set(costValue);

                    context.write(city, cost);

                } catch (NumberFormatException e) {
                    // Ignore invalid records
                }
            }
        }
    }

    public static class CitySalesReducer
            extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

        private DoubleWritable result = new DoubleWritable();

        public void reduce(Text key, Iterable<DoubleWritable> values,
                           Context context)
                throws IOException, InterruptedException {

            double sum = 0.0;

            for (DoubleWritable value : values) {
                sum += value.get();
            }

            result.set(sum);

            context.write(key, result);
        }
    }

    public static void main(String[] args)
            throws Exception {

        if (args.length != 2) {
            System.err.println(
                    "Usage: Task1_CitySales <input> <output>");
            System.exit(2);
        }

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "City Wise Total Sales");

        job.setJarByClass(Task1_CitySales.class);

        job.setMapperClass(CitySalesMapper.class);
        job.setReducerClass(CitySalesReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(
                job, new Path(args[0]));

        FileOutputFormat.setOutputPath(
                job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}