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

CREATE TABLE `study_extra_details` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `expected_nb_of_subjects` bigint(20) NOT NULL,
  `average_examination_size` float(20) DEFAULT NULL,
  `estimated_total_volume` float(20) DEFAULT NULL,
  `expected_nb_of_centers` bigint(20) NOT NULL,
  `inclusion_rate` bigint(20) DEFAULT NULL,
  `inclusion_rate_unit` int(11) DEFAULT NULL,
  `sponsor` varchar(255) NOT NULL,
  `principal_investigator` varchar(255) NOT NULL,
  `scientific_advisor` varchar(255) DEFAULT NULL,
  `study_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKg6kp2mt8r8964g80j57agkn1` (`study_id`),
  CONSTRAINT `FKg6kp2mt8r8964g80j57agkn1` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`) ON DELETE CASCADE
);