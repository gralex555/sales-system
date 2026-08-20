--liquibase formatted sql

--changeset alexei:001-create-products labels:products
CREATE TABLE products (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price DECIMAL(19,2) NOT NULL,
    quantity_available INTEGER NOT NULL,
    quantity_reserved INTEGER NOT NULL
);
--rollback DROP TABLE products;