--liquibase formatted sql

--changeset alexei:001-create-processed-event labels:processed-event
CREATE TABLE processed_event (
    event_id VARCHAR(100) PRIMARY KEY,
    event_type VARCHAR(50) NOT NULL,
    processed_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_processed_event_processed_at ON processed_event (processed_at);
--rollback DROP TABLE processed_event;