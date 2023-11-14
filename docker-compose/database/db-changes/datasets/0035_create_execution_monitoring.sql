CREATE TABLE `execution_monitoring` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `pipeline_identifier` varchar(255) DEFAULT NULL,
  `identifier` varchar(255) DEFAULT NULL,
  `status` tinyint(4) DEFAULT NULL,
  `start_date` bigint(20) DEFAULT NULL,
  `end_date` bigint(20) DEFAULT NULL,
  `output_processing` varchar(255) DEFAULT NULL,
  `results_location` varchar(255) DEFAULT NULL,
  `timeout` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

DELETE FROM input_of_dataset_processing 
WHERE processing_id IN (
  SELECT id FROM `dataset_processing`
  WHERE dtype = 'CarminDatasetProcessing');
  
DELETE FROM processing_resource 
WHERE processing_id IN (
  SELECT id FROM `dataset_processing`
  WHERE dtype = 'CarminDatasetProcessing');

DELETE FROM `dataset_processing`
WHERE dtype = 'CarminDatasetProcessing';

ALTER TABLE `dataset_processing` 
  ADD COLUMN `parent_id` bigint(20) DEFAULT NULL,
  DROP COLUMN `name`,
  DROP COLUMN `pipeline_identifier`, 
  DROP COLUMN `identifier`, 
  DROP COLUMN `status`, 
  DROP COLUMN `start_date`, 
  DROP COLUMN `end_date`, 
  DROP COLUMN `output_processing`, 
  DROP COLUMN `results_location`, 
  DROP COLUMN `timeout`,
  DROP COLUMN `dtype`;
