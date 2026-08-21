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

CREATE TABLE `xa_dataset` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKg6kp2mt6t11qtjg80j57agkn6` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
);

CREATE TABLE `xa_protocol` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
);

CREATE TABLE `xa_dataset_acquisition` (
  `id` bigint(20) NOT NULL,
  `xa_protocol_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_7ewg4h5ec3h4mjer364hunbk9` (`xa_protocol_id`),
  CONSTRAINT `FK5sv6h57xmxil97cbsk8dwsw1j` FOREIGN KEY (`xa_protocol_id`) REFERENCES `xa_protocol` (`id`),
  CONSTRAINT `FK9n4ys7m737wmmv5l3u4qslcdx` FOREIGN KEY (`id`) REFERENCES `dataset_acquisition` (`id`)
);