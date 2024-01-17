ALTER TABLE mr_protocol_metadata ADD COLUMN bids_data_type varchar(255);

-- squashed 0013_solr_numerical_double.sql
alter table shanoir_metadata modify magnetic_field_strength double;
alter table shanoir_metadata modify pixel_bandwidth double;
alter table shanoir_metadata modify slice_thickness double;