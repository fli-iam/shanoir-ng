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

﻿use shanoir_ng_users;

DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `account_request_info`;
DROP TABLE IF EXISTS `role`;

CREATE TABLE `role` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `access_level` int(11) NOT NULL,
  `display_name` varchar(255) NOT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_ac56dul55smn5it0ce76jswg3` (`display_name`),
  UNIQUE KEY `UK_8sewwnpamngi6b1dwaa88askk` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

CREATE TABLE `account_request_info` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `contact` varchar(255) NOT NULL,
  `function` varchar(255) NOT NULL,
  `institution` varchar(255) NOT NULL,
  `service` varchar(255) NOT NULL,
  `study` varchar(255) NOT NULL,
  `work` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `users` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `account_request_demand` bit(1),
  `can_access_to_dicom_association` bit(1),
  `creation_date` datetime DEFAULT NULL,
  `email` varchar(255) NOT NULL,
  `expiration_date` datetime DEFAULT NULL,
  `extension_request_demand` bit(1) DEFAULT false,
  `extension_date` datetime DEFAULT NULL,
  `extension_motivation` varchar(255) DEFAULT NULL,
  `first_expiration_notification_sent` bit(1),
  `first_name` varchar(255) NOT NULL,
  `keycloak_id` varchar(255) DEFAULT NULL,
  `last_login` datetime DEFAULT NULL,
  `last_name` varchar(255) NOT NULL,
  `second_expiration_notification_sent` bit(1),
  `team_name` varchar(255) DEFAULT NULL,
  `username` varchar(255) NOT NULL,
  `account_request_info_id` bigint(20) DEFAULT NULL,
  `role_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_6dotkott2kjsp8vw4d0m25fb7` (`email`),
  UNIQUE KEY `UK_r43af9ap4edm43mmtq01oddj6` (`username`),
  KEY `FKnujey376reejcmp77iuc48ja7` (`account_request_info_id`),
  KEY `FK4qu1gr772nnf6ve5af002rwya` (`role_id`),
  CONSTRAINT `FK4qu1gr772nnf6ve5af002rwya` FOREIGN KEY (`role_id`) REFERENCES `role` (`id`),
  CONSTRAINT `FKnujey376reejcmp77iuc48ja7` FOREIGN KEY (`account_request_info_id`) REFERENCES `account_request_info` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=259 DEFAULT CHARSET=utf8;
