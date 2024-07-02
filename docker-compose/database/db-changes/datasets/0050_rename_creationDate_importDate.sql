ALTER TABLE shanoir_metadata ADD COLUMN import_date date default null;
ALTER TABLE shanoir_metadata ADD COLUMN username varchar(255) default null;

ALTER TABLE dataset_acquisition CHANGE creation_date import_date date default NULL;
ALTER TABLE dataset_acquisition ADD COLUMN username varchar(255) DEFAULT NULL;