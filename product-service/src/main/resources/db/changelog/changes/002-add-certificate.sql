--liquibase formatted sql

--changeset alexei:002-add-certificate labels:products
ALTER TABLE products
    ADD COLUMN certificate_key VARCHAR(1024),
    ADD COLUMN certificate_filename VARCHAR(255);
--rollback ALTER TABLE products DROP COLUMN certificate_key, DROP COLUMN certificate_filename;