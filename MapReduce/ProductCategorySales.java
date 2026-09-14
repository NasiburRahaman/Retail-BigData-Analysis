import java.io.IOException;
import java.util.regex.Pattern;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class ProductCategorySales {

    public static class SalesMapper
            extends Mapper<Object, Text, Text, DoubleWritable> {

        private Text category = new Text();
        private DoubleWritable cost = new DoubleWritable();

        private String[] parseCSV(String line) {
            return line.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);
        }

        private String getCategory(String product) {

            String p = product.toLowerCase();

            if (p.contains("milk") || p.contains("cheese") ||
                p.contains("yogurt") || p.contains("butter") ||
                p.contains("bread") || p.contains("egg")) {
                return "Food & Grocery";
            }

            if (p.contains("shampoo") || p.contains("soap") ||
                p.contains("toothpaste") || p.contains("cream") ||
                p.contains("shaving")) {
                return "Personal Care";
            }

            if (p.contains("bulb") || p.contains("battery") ||
                p.contains("charger") || p.contains("cable")) {
                return "Electronics";
            }

            if (p.contains("shirt") || p.contains("jeans") ||
                p.contains("dress") || p.contains("jacket") ||
                p.contains("shoe")) {
                return "Clothing";
            }

            if (p.contains("pen") || p.contains("notebook") ||
                p.contains("book") || p.contains("pencil")) {
                return "Stationery";
            }

            return "Other";
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

            String product = fields[3];
            String totalCost = fields[5];

            try {
                double sales = Double.parseDouble(totalCost);
                String cat = getCategory(product);

                category.set(cat);
                cost.set(sales);

                context.write(category, cost);

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

            double sum = 0.0;

            for (DoubleWritable value : values) {
                sum += value.get();
            }

            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length != 2) {
            System.err.println("Usage: ProductCategorySales <input> <output>");
            System.exit(2);
        }

        Configuration conf = new Configuration();

        Job job = Job.getInstance(conf, "Product Category-wise Sales Analysis");

        job.setJarByClass(ProductCategorySales.class);

        job.setMapperClass(SalesMapper.class);
        job.setReducerClass(SalesReducer.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(DoubleWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}