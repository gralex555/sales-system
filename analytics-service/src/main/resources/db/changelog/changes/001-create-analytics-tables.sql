--liquibase formatted sql

--changeset alexei:001-create-sales-order labels:analytics
CREATE TABLE sales_order (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT NOT NULL UNIQUE,
    customer_id BIGINT NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    paid_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_sales_order_paid_at ON sales_order (paid_at);
CREATE INDEX idx_sales_order_customer ON sales_order (customer_id);
--rollback DROP TABLE sales_order;

--changeset alexei:001-create-sales-item labels:analytics
CREATE TABLE sales_item (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    sales_order_id BIGINT NOT NULL REFERENCES sales_order(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    price DECIMAL(19,2) NOT NULL
);

CREATE INDEX idx_sales_item_product ON sales_item (product_id);
--rollback DROP TABLE sales_item;

--changeset alexei:001-create-processed-event labels:analytics
CREATE TABLE processed_event (
    event_id VARCHAR(100) PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_processed_event_processed_at ON processed_event (processed_at);
--rollback DROP TABLE processed_event;