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


-- Local copy of the study members, kept up to date from ms studies (study-user-exchange),
-- to check the study rights in ms preclinical. Same tables as in ms datasets, import and users.

CREATE TABLE `study_user` (
  `id` bigint(20) NOT NULL,
  `confirmed` bit(1) NOT NULL,
  `receive_study_user_report` bit(1) NOT NULL,
  `receive_new_import_report` bit(1) NOT NULL,
  `expiration_date` date DEFAULT NULL,
  `received_expiration_notification` bit(1) NOT NULL DEFAULT b'0',
  `study_id` bigint(20) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `user_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `study_user_idx` (`study_id`,`user_id`)
) ENGINE=InnoDB;

CREATE TABLE `study_user_center` (
  `study_user_id` bigint(20) NOT NULL,
  `center_id` bigint(20) NOT NULL,
  KEY `FK8jvoy3dqkninlrimnrb8endp3` (`study_user_id`),
  CONSTRAINT `FK8jvoy3dqkninlrimnrb8endp3` FOREIGN KEY (`study_user_id`) REFERENCES `study_user` (`id`)
) ENGINE=InnoDB;

CREATE TABLE `study_user_study_user_rights` (
  `study_user_id` bigint(20) NOT NULL,
  `study_user_rights` int(11) DEFAULT NULL,
  KEY `FK6ipbom6lji60h38bd3ok2r098` (`study_user_id`),
  CONSTRAINT `FK6ipbom6lji60h38bd3ok2r098` FOREIGN KEY (`study_user_id`) REFERENCES `study_user` (`id`)
) ENGINE=InnoDB;


-- Copy the existing study members from ms studies, the source of truth

INSERT INTO study_user (id, confirmed, receive_study_user_report, receive_new_import_report,
    expiration_date, received_expiration_notification, study_id, user_id, user_name)
SELECT id, confirmed, receive_study_user_report, receive_new_import_report,
    expiration_date, received_expiration_notification, study_id, user_id, COALESCE(user_name, '')
FROM studies.study_user;

INSERT INTO study_user_center (study_user_id, center_id)
SELECT study_user_id, center_id
FROM studies.study_user_center;

INSERT INTO study_user_study_user_rights (study_user_id, study_user_rights)
SELECT study_user_id, study_user_rights
FROM studies.study_user_study_user_rights;
