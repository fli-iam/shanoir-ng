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

CREATE TABLE `processing_resource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_id` varchar(255) NOT NULL,
  `dataset_id` bigint(20) DEFAULT NULL,
  `processing_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7971ykr4l94b3qgns4do405qe` (`dataset_id`),
  KEY `FKtkm1ww8m49x3jdwdtocijhrha` (`processing_id`),
  CONSTRAINT `FK7971ykr4l94b3qgns4do405qe` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`),
  CONSTRAINT `FKtkm1ww8m49x3jdwdtocijhrha` FOREIGN KEY (`processing_id`) REFERENCES `dataset_processing` (`id`)
)
