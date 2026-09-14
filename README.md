\# Retail Big Data Analysis Using Hadoop MapReduce and Apache Pig



\## Project Overview



This project performs large-scale retail transaction data analysis using Hadoop HDFS, Java MapReduce, and Apache Pig.



The dataset contains approximately 1 million retail transactions.



\## Dataset



The project uses the Retail Transactions Dataset from Kaggle.



Dataset Source:



https://www.kaggle.com/datasets/prasad22/retail-transactions-dataset



The original CSV file is not included in this repository because of its large size.



\## Technologies Used



\- Hadoop 3.2.4

\- HDFS

\- YARN

\- Java

\- MapReduce

\- Apache Pig

\- Windows 11

\- Git and GitHub



\## MapReduce Analyses



\### 1. City-wise Total Sales Analysis



Calculates the total sales for each city.



\### 2. Product Category-wise Sales Analysis



Analyzes sales based on product/category information.



\### 3. Customer-wise Purchase/Sales Analysis



Calculates purchase/sales information for individual customers.



\## Apache Pig Analyses



\### 1. City-wise Sales Analysis



Analyzes total sales for each city.



\### 2. Customer Category-wise Sales Analysis



Analyzes sales according to customer category.



\### 3. Payment Method-wise Transaction Analysis



Analyzes transaction count and total sales for each payment method.



\### 4. Monthly Sales Trend Analysis



Analyzes total sales month by month.



\### 5. Top Products Analysis



Identifies the most frequently occurring products.



\## Project Structure



```text

Retail-BigData-Analysis/

│

├── README.md

│

├── dataset/

│   └── README.md

│

├── MapReduce/

│   ├── Java source files

│   └── README.md

│

├── Pig/

│   ├── city\_sales.pig

│   ├── category\_sales.pig

│   ├── payment\_method.pig

│   ├── monthly\_sales.pig

│   └── top\_products.pig

│

├── Results/

│   ├── MapReduce/

│   └── Pig/

│

├── Screenshots/

│   ├── MapReduce/

│   └── Pig/

│

└── Report/

&#x20;   └── Retail\_BigData\_Analysis\_Report.pdf

