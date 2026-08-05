--liquibase formatted sql

--changeset alexei:001-create-orders labels:orders
CREATE TABLE orders (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    status VARCHAR(50) NOT NULL,
    total_amount DECIMAL(19,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP
);
--rollback DROP TABLE orders;