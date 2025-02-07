CREATE TABLE latest_checked_examination (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    examination_id BIGINT NOT NULL
);

ALTER TABLE `examination` ADD COLUMN `study_instance_uid` varchar(255);

CREATE UNIQUE INDEX UK_15imoe7p5ks3na50kltrhvw6r ON examination(study_instance_uid);

ALTER TABLE `dataset_acquisition` ADD COLUMN `series_instance_uid` varchar(255);

CREATE UNIQUE INDEX UK_13imoe4p5ks3na77kltrhvw9r ON dataset_acquisition(series_instance_uid);

