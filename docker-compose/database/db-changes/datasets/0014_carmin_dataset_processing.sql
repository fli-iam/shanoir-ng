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

ALTER TABLE `dataset_processing`
ADD COLUMN `dtype` varchar(31) NOT NULL,
ADD COLUMN `end_date` bigint(20) DEFAULT NULL,
ADD COLUMN `identifier` varchar(255) DEFAULT NULL,
ADD COLUMN `name` varchar(255) DEFAULT NULL,
ADD COLUMN `pipeline_identifier` varchar(255) DEFAULT NULL,
ADD COLUMN `results_location` varchar(255) DEFAULT NULL,
ADD COLUMN `start_date` bigint(20) DEFAULT NULL,
ADD COLUMN `status` int(11) DEFAULT NULL,
ADD COLUMN `timeout` int(11) DEFAULT NULL;

