--liquibase formatted sql

--changeset alexei:002-create-order-items labels:order-items
CREATE TABLE order_items (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    product_id BIGINT NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    order_id BIGINT NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id)
        REFERENCES orders(id)
        ON DELETE CASCADE
);
--rollback DROP TABLE orders_items;