-- index for order status and date
CREATE INDEX IF NOT EXISTS idx_orders_status_order_date
    ON Orders (status, order_date);

-- index on name for product name searches
CREATE INDEX IF NOT EXISTS idx_product_name
    ON Products (name);

-- index on price for faster price filter lookups
CREATE INDEX IF NOT EXISTS idx_product_price
    ON Products (price);