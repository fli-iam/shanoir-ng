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

CREATE TABLE `bids_dataset` (
  `bids_data_type` varchar(255) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKfyr5qol686n67f24o8wpbubpg` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
);

CREATE TABLE `bids_dataset_acquisition` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKe78qni1n2gip32q17j6m85qn1` FOREIGN KEY (`id`) REFERENCES `dataset_acquisition` (`id`)
);

SET sql_mode="NO_AUTO_VALUE_ON_ZERO";
INSERT INTO center (id, name) VALUES (0,'Unknown');
SET sql_mode=(SELECT REPLACE(@@sql_mode,'NO_AUTO_VALUE_ON_ZERO',''));