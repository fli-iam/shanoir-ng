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

CREATE TABLE `profile` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `profile_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

ALTER TABLE study ADD profile_id bigint(20);
ALTER TABLE study ADD CONSTRAINT `FK9o4lyhi0i6ocqf1mpd9yaeyij` FOREIGN KEY (`profile_id`) REFERENCES `profile` (`id`);

INSERT INTO profile (id, profile_name) VALUES (1,'Profile Neurinfo');
INSERT INTO profile (id, profile_name) VALUES (2,'Profile OFSEP');