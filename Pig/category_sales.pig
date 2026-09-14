raw = LOAD '/retail/input/Retail_Transactions_Dataset.csv'
USING TextLoader()
AS (line:chararray);

data = FILTER raw BY NOT STARTSWITH(line, 'Transaction_ID');

parsed = FOREACH data GENERATE
    REGEX_EXTRACT(line, '^(\\d+),(.*?),([^,]+),"(.*?)",(\\d+),([0-9.]+),([^,]+),([^,]+),([^,]+),([^,]+),([^,]+)', 11) AS category,
    (double)REGEX_EXTRACT(line, '^(\\d+),(.*?),([^,]+),"(.*?)",(\\d+),([0-9.]+)', 6) AS total_cost;

valid = FILTER parsed BY category IS NOT NULL AND total_cost IS NOT NULL;

grp = GROUP valid BY category;

result = FOREACH grp GENERATE
    group AS customer_category,
    SUM(valid.total_cost) AS total_sales;

ordered = ORDER result BY total_sales DESC;

STORE ordered INTO '/retail/pig/category_sales' USING PigStorage(',');