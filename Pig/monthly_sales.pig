raw = LOAD '/retail/input/Retail_Transactions_Dataset.csv'
USING TextLoader()
AS (line:chararray);

data = FILTER raw BY NOT STARTSWITH(line, 'Transaction_ID');

parsed = FOREACH data GENERATE
    SUBSTRING(
        REGEX_EXTRACT(line, '^(\\d+),([^,]+)', 2),
        0, 7
    ) AS month,
    (double)REGEX_EXTRACT(line, '^(\\d+),([^,]+),([^,]+),"(.*?)",(\\d+),([0-9.]+)', 6) AS total_cost;

valid = FILTER parsed BY month IS NOT NULL AND total_cost IS NOT NULL;

grp = GROUP valid BY month;

result = FOREACH grp GENERATE
    group AS month,
    SUM(valid.total_cost) AS total_sales;

ordered = ORDER result BY month ASC;

STORE ordered INTO '/retail/pig/monthly_sales' USING PigStorage(',');