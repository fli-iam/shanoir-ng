-- Shanoir NG - Import, manage and share neuroimaging data
-- Copyright (C) 2009-2019 Inria - https://www.inria.fr/
-- Contact us on https://project.inria.fr/shanoir/
-- 
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
-- 
-- You should have received a copy of the GNU General Public License
-- along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

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
  ADD CONSTRAINT `FKsvnjfuhqge7y2j9dnucv5e01i` FOREIGN KEY (`parent_id`) REFERENCES `dataset_processing` (`id`),
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
