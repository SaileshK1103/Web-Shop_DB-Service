DROP VIEW IF EXISTS OrderSummaries;
DROP VIEW IF EXISTS ProductSales;
DROP VIEW IF EXISTS ProductHistory;

-- one row per order with its customer, number of different products and total amount
CREATE OR REPLACE VIEW OrderSummaries AS
SELECT orders.id AS order_id,
       orders.order_date,
       orders.status,
       customers.id AS customer_id,
       CONCAT(customers.first_name, ' ', customers.last_name) AS customer_name,
       COUNT(oi.product_id) AS item_count,
       COALESCE(SUM(oi.quantity * oi.unit_price), 0) AS total_amount
FROM Orders
JOIN Customers ON Customers.id = Orders.customer_id
LEFT JOIN OrderItems oi ON oi.order_id = Orders.id
GROUP BY Orders.id, Orders.order_date, Orders.status, Customers.id, Customers.first_name, Customers.last_name;

-- all versions of all products, valid_to is NULL for the version that is current
CREATE OR REPLACE VIEW ProductHistory AS
SELECT id AS product_id,
       name,
       price,
       stock_quantity,
       ROW_START AS valid_from,
       CASE WHEN ROW_END > CURRENT_TIMESTAMP THEN NULL ELSE ROW_END END AS valid_to
FROM Products FOR SYSTEM_TIME ALL;