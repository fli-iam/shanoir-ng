CREATE TABLE examination_last_checked (
    id BIGINT(20) AUTO_INCREMENT PRIMARY KEY,
    examination_id BIGINT(20) NOT NULL
);

ALTER TABLE `examination` ADD COLUMN `study_instance_uid` varchar(255);

ALTER TABLE `dataset_acquisition` ADD COLUMN `series_instance_uid` varchar(255);
