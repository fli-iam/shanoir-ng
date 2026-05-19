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

 CREATE TABLE `generic_dataset_acquisition` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKbc4bfdipqkw24d9c1itk5umkk` FOREIGN KEY (`id`) REFERENCES `dataset_acquisition` (`id`)
)

CREATE TABLE `generic_dataset` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK5lswb4d1wgnrwti4l9lvi3a8v` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
)
