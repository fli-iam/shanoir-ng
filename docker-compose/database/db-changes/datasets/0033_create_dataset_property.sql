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

CREATE TABLE `dataset_property` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `dataset_id` bigint(20) DEFAULT NULL,
  `dataset_processing_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1mcyt0h60808jmj3shlvge9c8` (`dataset_id`),
  KEY `FKl09g6qevg8filjycsylycbdxw` (`dataset_processing_id`),
  CONSTRAINT `FK1mcyt0h60808jmj3shlvge9c8` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`),
  CONSTRAINT `FKl09g6qevg8filjycsylycbdxw` FOREIGN KEY (`dataset_processing_id`) REFERENCES `dataset_processing` (`id`)
)

