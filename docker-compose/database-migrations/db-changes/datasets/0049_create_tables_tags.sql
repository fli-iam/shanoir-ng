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

CREATE TABLE `study_tag` (
  `id` bigint(20) NOT NULL,
  `color` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKboew1v3lqqa0afxnigq4fxhf3` (`study_id`),
  CONSTRAINT `FKboew1v3lqqa0afxnigq4fxhf3` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
 );
 CREATE TABLE `dataset_tag` (
  `dataset_id` bigint(20) NOT NULL,
  `study_tag_id` bigint(20) NOT NULL,
  KEY `FKkh92b0ddi9nxrevqkdmvqpcm3` (`study_tag_id`),
  KEY `FKd0dkfmqchgw18bxirml5an8ex` (`dataset_id`),
  CONSTRAINT `FKd0dkfmqchgw18bxirml5an8ex` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`),
  CONSTRAINT `FKkh92b0ddi9nxrevqkdmvqpcm3` FOREIGN KEY (`study_tag_id`) REFERENCES `study_tag` (`id`)
);
