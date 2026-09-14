import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.DoubleWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class CityWiseSales {

    public static class SalesMapper
            extends Mapper<Object, Text, Text, DoubleWritable> {

        private Text city = new Text();
        private DoubleWritable sales = new DoubleWritable();

        public void map(Object key, Text value, Context context)
                throws IOException, InterruptedException {

            String line = value.toString();

            // Skip header
            if (line.startsWith("Transaction_ID")) {
                return;
            }

            String[] fields = parseCSVLine(line);

            // Total_Cost = index 5
            // City = index 7

            if (fields.length >= 8) {

                try {

                    String cityName = fields[7].trim();

                    double totalCost =
                            Double.parseDouble(fields[5].trim());

                    city.set(cityName);
                    sales.set(totalCost);

                    context.write(city, sales);

                } catch (NumberFormatException e) {
                    // Ignore invalid rows
                }
            }
        }

        // CSV parser that handles commas inside quoted fields
        private String[] parseCSVLine(String line) {

            List<String> fields = new ArrayList<String>();

            StringBuilder field = new StringBuilder();

            boolean inQuotes = false;

            for (int i = 0; i < line.length(); i++) {

                char c = line.charAt(i);

                if (c == '"') {

                    inQuotes = !inQuotes;

                } else if (c == ',' && !inQuotes) {

                    fields.add(field.toString());

                    field.setLength(0);

                } else {

                    field.append(c);
                }
            }

            fields.add(field.toString());

            return fields.toArray(
                    new String[fields.size()]);
        }
    }

    public static class SalesReducer
            extends Reducer<Text, DoubleWritable,
                            Text, DoubleWritable> {

        private DoubleWritable result =
                new DoubleWritable();

        public void reduce(
                Text key,
                Iterable<DoubleWritable> values,
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

        Configuration conf =
                new Configuration();

        Job job = Job.getInstance(
                conf,
                "City Wise Total Sales");

        job.setJarByClass(
                CityWiseSales.class);

        job.setMapperClass(
                SalesMapper.class);

        job.setReducerClass(
                SalesReducer.class);

        job.setOutputKeyClass(
                Text.class);

        job.setOutputValueClass(
                DoubleWritable.class);

        FileInputFormat.addInputPath(
                job,
                new Path(args[0]));

        FileOutputFormat.setOutputPath(
                job,
                new Path(args[1]));

        System.exit(
                job.waitForCompletion(true)
                        ? 0
                        : 1);
    }
}