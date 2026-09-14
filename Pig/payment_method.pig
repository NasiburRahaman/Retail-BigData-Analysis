raw = LOAD '/retail/input/Retail_Transactions_Dataset.csv'
USING TextLoader()
AS (line:chararray);

data = FILTER raw BY NOT STARTSWITH(line, 'Transaction_ID');

parsed = FOREACH data GENERATE
    REGEX_EXTRACT(line, '^(\\d+),(.*?),([^,]+),"(.*?)",(\\d+),([0-9.]+),([^,]+)', 7) AS payment_method,
    (double)REGEX_EXTRACT(line, '^(\\d+),(.*?),([^,]+),"(.*?)",(\\d+),([0-9.]+)', 6) AS total_cost;

valid = FILTER parsed BY payment_method IS NOT NULL AND total_cost IS NOT NULL;

grp = GROUP valid BY payment_method;

result = FOREACH grp GENERATE
    group AS payment_method,
    COUNT(valid) AS transactions,
    SUM(valid.total_cost) AS total_sales;

ordered = ORDER result BY transactions DESC;

STORE ordered INTO '/retail/pig/payment_method' USING PigStorage(',');