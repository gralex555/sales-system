--liquibase formatted sql

--changeset alexei:005-add-product-name labels:orders
ALTER TABLE order_items ADD COLUMN product_name VARCHAR(255);
--rollback ALTER TABLE order_items DROP COLUMN product_name;