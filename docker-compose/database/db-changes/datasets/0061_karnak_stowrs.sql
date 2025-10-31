ALTER TABLE `dataset_acquisition` ADD COLUMN `acquisition_number` INT(11);

ALTER TABLE `dataset_metadata` ADD COLUMN `image_orientation_patient` varchar(255);
