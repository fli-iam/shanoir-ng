ALTER TABLE `dataset_acquisition` ADD COLUMN `acquisition_number` INT(11);

ALTER TABLE `dataset_metadata` ADD COLUMN `image_orientation_patient` varchar(255);

CREATE INDEX idx_dataset_acquisition_series_exam 
ON dataset_acquisition(series_instance_uid, examination_id);
