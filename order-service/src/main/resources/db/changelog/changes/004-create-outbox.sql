--liquibase formatted sql

--changeset alexei:004-create-outbox labels:order-outbox
CREATE TABLE order_outbox (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    aggregate_id BIGINT NOT NULL,
    event_type VARCHAR(50) NOT NULL,
    topic VARCHAR(100) NOT NULL,
    payload TEXT NOT NULL,
    published BOOLEAN NOT NULL DEFAULT FALSE,
    attempts INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    published_at TIMESTAMP
);
CREATE INDEX idx_order_outbox_unpublished ON order_outbox (published, created_at);
--rollback DROP TABLE order_outbox;