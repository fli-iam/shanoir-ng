ALTER TABLE dataset_acquisition CHANGE creation_date import_date date default NULL
ALTER TABLE dataset_acquisition ADD COLUMN username varchar(255) DEFAULT NULL;