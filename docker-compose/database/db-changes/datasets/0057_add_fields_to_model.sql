ALTER TABLE dataset_acquisition ADD COLUMN acquisition_start_time datetime default null;

ALTER TABLE ct_protocol ADD COLUMN slice_thickness double default null;
ALTER TABLE pet_protocol ADD COLUMN slice_thickness double default null;
ALTER TABLE xa_protocol ADD COLUMN slice_thickness double default null;

ALTER TABLE ct_protocol ADD COLUMN number_of_slices int default null;
ALTER TABLE mr_protocol ADD COLUMN number_of_slices int default null;
ALTER TABLE xa_protocol ADD COLUMN number_of_slices int default null;
