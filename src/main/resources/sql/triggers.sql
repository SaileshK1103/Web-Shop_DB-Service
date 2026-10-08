-- audit table filled by the trigger below

CREATE TABLE IF NOT EXISTS OrderStatusHistory (
    id INT AUTO_INCREMENT PRIMARY KEY,
    order_id INT NOT NULL,
    previous_status VARCHAR(50),
    updated_status VARCHAR(50) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_orderstatushistory_order
        FOREIGN KEY (order_id) REFERENCES Orders(id)
);

-- log every status change of an order
DELIMITER //

CREATE OR REPLACE TRIGGER trg_orders_log_status_change
AFTER UPDATE ON Orders
FOR EACH ROW
BEGIN
  IF NOT (OLD.status <=> NEW.status) THEN
    INSERT INTO OrderStatusHistory (order_id, previous_status, updated_status, created_at)
    VALUES (NEW.id, OLD.status, NEW.status, NOW());
  END IF;
END//


-- reject a negative stock quantity
CREATE OR REPLACE PROCEDURE check_product_stock_quantity(IN product_stock_quantity INT)
BEGIN
    IF product_stock_quantity < 0 THEN
        SIGNAL SQLSTATE '45000' SET MESSAGE_TEXT = 'Stock quantity cannot be negative';
    END IF;
END//

-- reject a negative stock on insert
CREATE OR REPLACE TRIGGER trg_products_check_stock_insert
BEFORE INSERT ON Products
FOR EACH ROW CALL check_product_stock_quantity(NEW.stock_quantity)//

-- reject a negative stock on update
CREATE OR REPLACE TRIGGER trg_products_check_stock_update
BEFORE UPDATE ON Products
FOR EACH ROW CALL check_product_stock_quantity(NEW.stock_quantity)//

DELIMITER ;
