raw = LOAD '/retail/input/Retail_Transactions_Dataset.csv'
USING TextLoader()
AS (line:chararray);

data = FILTER raw BY NOT STARTSWITH(line, 'Transaction_ID');

parsed = FOREACH data GENERATE
    REGEX_EXTRACT(
        line,
        '^[^,]*,[^,]*,[^,]*,"?\\[(.*?)\\]"?,',
        1
    ) AS product_list;

valid = FILTER parsed BY product_list IS NOT NULL;

tokens = FOREACH valid GENERATE
    FLATTEN(TOKENIZE(product_list, ',')) AS product;

clean_products = FOREACH tokens GENERATE
    TRIM(product) AS product;

valid_products = FILTER clean_products
    BY product IS NOT NULL AND product != '';

grp = GROUP valid_products BY product;

result = FOREACH grp GENERATE
    group AS product,
    COUNT(valid_products) AS transaction_count;

ordered = ORDER result BY transaction_count DESC;

top = LIMIT ordered 10;

STORE top INTO '/retail/pig/top_products' USING PigStorage(',');