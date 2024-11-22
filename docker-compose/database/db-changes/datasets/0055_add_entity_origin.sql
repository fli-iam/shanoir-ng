ALTER TABLE dataset ADD COLUMN origin int(11) DEFAULT NULL;
ALTER TABLE dataset_acquisition ADD COLUMN origin int(11) DEFAULT NULL;
ALTER TABLE examination ADD COLUMN origin int(11) DEFAULT NULL;