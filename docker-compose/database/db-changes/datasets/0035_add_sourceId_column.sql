ALTER TABLE dataset ADD COLUMN source_id bigint(20) DEFAULT NULL;
ALTER TABLE dataset_acquisition ADD COLUMN source_id bigint(20) DEFAULT NULL;
ALTER TABLE examination ADD COLUMN source_id bigint(20) DEFAULT NULL;