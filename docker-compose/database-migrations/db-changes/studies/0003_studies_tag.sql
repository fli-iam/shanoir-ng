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

CREATE TABLE `tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `color` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc1j2e615fytq8hsd0usqf49ia` (`study_id`),
  CONSTRAINT `FKc1j2e615fytq8hsd0usqf49ia` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
);

CREATE TABLE `subject_study_tag` (
  `subject_study_id` bigint(20) NOT NULL,
  `tags_id` bigint(20) NOT NULL,
  KEY `FKcc51xo3yrp74v2yp7df540a03` (`tags_id`),
  KEY `FKe638djnyhwckgsoa7qxnvcayd` (`subject_study_id`),
  CONSTRAINT `FKcc51xo3yrp74v2yp7df540a03` FOREIGN KEY (`tags_id`) REFERENCES `tag` (`id`),
  CONSTRAINT `FKe638djnyhwckgsoa7qxnvcayd` FOREIGN KEY (`subject_study_id`) REFERENCES `subject_study` (`id`)
);