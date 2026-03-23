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

ALTER TABLE account_request_info DROP COLUMN study;
ALTER TABLE account_request_info DROP COLUMN work;
ALTER TABLE account_request_info DROP COLUMN service;
ALTER TABLE account_request_info DROP COLUMN challenge;
ALTER TABLE account_request_info ADD COLUMN study_id bigint(20);
ALTER TABLE account_request_info ADD COLUMN study_name varchar(255);
ALTER TABLE account_request_info MODIFY contact VARCHAR(255);

CREATE TABLE `access_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `motivation` varchar(255) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  `study_name` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKipjw4x52ro7jvnqai3lbqibwh` (`user_id`),
  CONSTRAINT `FKipjw4x52ro7jvnqai3lbqibwh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);
