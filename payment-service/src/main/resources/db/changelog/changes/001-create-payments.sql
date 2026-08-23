--liquibase formatted sql

--changeset alexei:001-create-payments labels:payments
CREATE TABLE payments (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    order_id BIGINT NOT NULL,
    amount DECIMAL(19,2) NOT NULL,
    status VARCHAR(50) NOT NULL,
    failure_reason VARCHAR(500),
    created_at TIMESTAMP NOT NULL
);
--rollback DROP TABLE payments;