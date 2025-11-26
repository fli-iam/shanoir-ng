ALTER TABLE `dataset_acquisition` ADD COLUMN `acquisition_number` INT(11);

ALTER TABLE `dataset_metadata` ADD COLUMN `image_orientation_patient` VARCHAR(255);

CREATE INDEX idx_dataset_acquisition_series_exam 
ON dataset_acquisition(series_instance_uid, examination_id);

CREATE TABLE study_center (
  id BIGINT(20) NOT NULL,
  study_id BIGINT(20) NOT NULL,
  center_id BIGINT(20) NOT NULL,
  PRIMARY KEY (id),
  KEY `FK2hmmh3c0w1tk8npi87hpvf10i` (study_id),
  KEY `FKi5ioon77o40h52tyxdqubfpdp` (center_id),
  CONSTRAINT `FK2hmmh3c0w1tk8npi87hpvf10i` FOREIGN KEY (study_id) REFERENCES `study` (id),
  CONSTRAINT `FKi5ioon77o40h52tyxdqubfpdp` FOREIGN KEY (center_id) REFERENCES `center` (id)
);

INSERT IGNORE INTO `center` (id)
SELECT DISTINCT center_id 
FROM studies.study_center 
WHERE center_id NOT IN (SELECT id FROM `center`);

# Replicate study_center into MS Datasets
INSERT INTO study_center (id, study_id, center_id)
SELECT
  id, study_id, center_id
FROM 
  studies.study_center ssc;
