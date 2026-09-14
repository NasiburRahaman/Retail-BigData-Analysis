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

public class CustomerSales {

    public static class SalesMapper
            extends Mapper<Object, Text, Text, DoubleWritable> {

        private Text customer = new Text();
        private DoubleWritable cost = new DoubleWritable();

        private String[] parseCSV(String line) {
            return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        }

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            if (line.startsWith("Transaction_ID")) {
                return;
            }

            String[] fields = parseCSV(line);

            if (fields.length < 13) {
                return;
            }

            try {

                String customerName = fields[2];
                double totalCost = Double.parseDouble(fields[5]);

                customer.set(customerName);
                cost.set(totalCost);

                context.write(customer, cost);

            } catch (Exception e) {
                // Ignore invalid records
            }
        }
    }

    public static class SalesReducer
            extends Reducer<Text, DoubleWritable, Text, DoubleWritable> {

        private DoubleWritable result = new DoubleWritable();

        public void reduce(Text key, Iterable<DoubleWritable> values,
                           Context context)
                throws IOException, InterruptedException {

            double total = 0.0;

            for (DoubleWritable value : values) {
                total += value.get();
            }

            result.set(total);

            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf,
                "Customer-wise Purchase Sales Analysis");

        job.setJarByClass(CustomerSales.class);

        job.setMapperClass(SalesMapper.class);
        job.setReducerClass(SalesReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}