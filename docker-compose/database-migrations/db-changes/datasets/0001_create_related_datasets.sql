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

CREATE TABLE `related_datasets` (
  `study_id` bigint(20) NOT NULL,
  `dataset_id` bigint(20) NOT NULL,
  KEY `FKdj59kuldlkr2ufy5j5dy33akv` (`dataset_id`),
  KEY `FKb0atgqxasc1nctk59hb632j93` (`study_id`),
  CONSTRAINT `FKb0atgqxasc1nctk59hb632j93` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`),
  CONSTRAINT `FKdj59kuldlkr2ufy5j5dy33akv` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`)
)
