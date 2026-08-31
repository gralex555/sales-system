--liquibase formatted sql

--changeset alexei:003-add-reserved-until labels:orders
ALTER TABLE orders ADD COLUMN reserved_until TIMESTAMP;
--rollback ALTER TABLE orders DROP COLUMN reserved_until;

--changeset alexei:003-migrate-statuses labels:orders
UPDATE orders SET status = 'CREATED' WHERE status = 'RESERVED';
UPDATE orders SET status = 'PAID' WHERE status = 'CONFIRMED';
--rollback SELECT 1; -- откат данных невозможен: CREATED и RESERVED слились