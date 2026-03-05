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

ALTER TABLE study_user ADD COLUMN confirmed bit NOT NULL DEFAULT TRUE;

CREATE TABLE data_user_agreement_file (
  study_id bigint(20) NOT NULL,
  path varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE data_user_agreement_file ADD CONSTRAINT FKnmg0gxlptf2nqktd0jj5hvi64 FOREIGN KEY (study_id) REFERENCES study(id);

CREATE TABLE data_user_agreement (
  id bigint(20) PRIMARY KEY NOT NULL AUTO_INCREMENT,
  timestamp_of_accepted timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  timestamp_of_new timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  user_id bigint(20) NOT NULL,
  study_id bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE data_user_agreement ADD CONSTRAINT FKrt509nksblm8s9f7f9ehfjxd FOREIGN KEY (study_id) REFERENCES study(id);