use shanoir_ng_studies;

DROP TABLE IF EXISTS `acquisition_equipment`;
DROP TABLE IF EXISTS `manufacturer_model`;
DROP TABLE IF EXISTS `manufacturer`;
DROP TABLE IF EXISTS `subject_study`;
DROP TABLE IF EXISTS `user_personal_comment_subject`;
DROP TABLE IF EXISTS `subject`;
DROP TABLE IF EXISTS `pseudonymus_hash_values`;
DROP TABLE IF EXISTS `study_user`;
DROP TABLE IF EXISTS `study_study_card`;
DROP TABLE IF EXISTS `study_center`;
DROP TABLE IF EXISTS `study`;
DROP TABLE IF EXISTS `center`;

CREATE TABLE `center` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `postal_code` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_9ntt4q0n3w4lywq1k9xveiyo8` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=69 DEFAULT CHARSET=utf8;

CREATE TABLE `study` (
  `id` bigint(20) NOT NULL,
  `Clinical` bit(1) NOT NULL,
  `coordinator_id` bigint(20) DEFAULT NULL,
  `downloadable_by_default` bit(1) NOT NULL,
  `end_date` date DEFAULT NULL,
  `mono_center` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `start_date` date DEFAULT NULL,
  `study_status` int(11) NOT NULL,
  `study_type` int(11) NOT NULL,
  `visible_by_default` bit(1) NOT NULL,
  `with_examination` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_q5qxcb7ermaxmp5f2wx2rj28n` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `study_center` (
  `id` bigint(20) NOT NULL,
  `center_id` bigint(20) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi5ioon66o30h52tyxdqubfpdp` (`center_id`),
  KEY `FK2hmmh3c0w1tk8npi76hpvf09i` (`study_id`),
  CONSTRAINT `FK2hmmh3c0w1tk8npi76hpvf09i` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`),
  CONSTRAINT `FKi5ioon66o30h52tyxdqubfpdp` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=289 DEFAULT CHARSET=utf8;

CREATE TABLE `study_study_card` (
  `study_id` bigint(20) NOT NULL,
  `study_card_id` bigint(20) DEFAULT NULL,
  KEY `FKeutuw850j87x31io2brvsoedn` (`study_id`),
  CONSTRAINT `FKeutuw850j87x31io2brvsoedn` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `study_user` (
  `id` bigint(20) NOT NULL,
  `receive_anonymization_report` bit(1) NOT NULL,
  `receive_new_import_report` bit(1) NOT NULL,
  `study_user_right` int(11) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc4ftmuoc0u0ghw43dxth2m8we` (`study_id`),
  CONSTRAINT `FKc4ftmuoc0u0ghw43dxth2m8we` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `pseudonymus_hash_values` (
  `id` bigint(20) NOT NULL,
  `birth_date_hash` varchar(255) DEFAULT NULL,
  `birth_name_hash1` varchar(255) DEFAULT NULL,
  `birth_name_hash2` varchar(255) DEFAULT NULL,
  `birth_name_hash3` varchar(255) DEFAULT NULL,
  `first_name_hash1` varchar(255) DEFAULT NULL,
  `first_name_hash2` varchar(255) DEFAULT NULL,
  `first_name_hash3` varchar(255) DEFAULT NULL,
  `last_name_hash1` varchar(255) DEFAULT NULL,
  `last_name_hash2` varchar(255) DEFAULT NULL,
  `last_name_hash3` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
CREATE TABLE `subject` (
  `id` bigint(20) NOT NULL,
  `birth_date` datetime DEFAULT NULL,
  `identifier` varchar(255) DEFAULT NULL,
  `imaged_object_category` int(11) DEFAULT NULL,
  `language_hemispheric_dominance` int(11) DEFAULT NULL,
  `manual_hemispheric_dominance` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `sex` int(11) DEFAULT NULL,
  `pseudonymus_hash_values_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq6wqw3ly73aat738iyo667yd4` (`pseudonymus_hash_values_id`),
  CONSTRAINT `FKq6wqw3ly73aat738iyo667yd4` FOREIGN KEY (`pseudonymus_hash_values_id`) REFERENCES `pseudonymus_hash_values` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `user_personal_comment_subject` (
  `id` bigint(20) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `subject_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsv5esm96mpp66iei2ei32jado` (`subject_id`),
  CONSTRAINT `FKsv5esm96mpp66iei2ei32jado` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `subject_study` (
  `id` bigint(20) NOT NULL,
  `physically_involved` bit(1) NOT NULL,
  `subject_study_identifier` varchar(255) DEFAULT NULL,
  `subject_type` int(11) DEFAULT NULL,
  `study` bigint(20) DEFAULT NULL,
  `subject` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKelblgk1bc448k1jrlaq0lue5d` (`study`),
  KEY `FKft8kki1chfo5h4q8l64slqb4c` (`subject`),
  CONSTRAINT `FKelblgk1bc448k1jrlaq0lue5d` FOREIGN KEY (`study`) REFERENCES `study` (`id`),
  CONSTRAINT `FKft8kki1chfo5h4q8l64slqb4c` FOREIGN KEY (`subject`) REFERENCES `subject` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `manufacturer` (
  `id` bigint(20) NOT NULL,
  `name` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_fvhf6l0xkf8hnay7lvwimnwu1` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8;

CREATE TABLE `manufacturer_model` (
  `id` bigint(20) NOT NULL,
  `dataset_modality_type` int(11) NOT NULL,
  `magnetic_field` double DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `manufacturer_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdaf3kbh73wqquhyggxyhaadq7` (`manufacturer_id`),
  CONSTRAINT `FKdaf3kbh73wqquhyggxyhaadq7` FOREIGN KEY (`manufacturer_id`) REFERENCES `manufacturer` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=59 DEFAULT CHARSET=utf8;

CREATE TABLE `acquisition_equipment` (
  `id` bigint(20) NOT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `center_id` bigint(20) NOT NULL,
  `manufacturer_model_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `model_number_idx` (`manufacturer_model_id`,`serial_number`),
  KEY `FKnfu3vqdsoj1y339uq7alaltjv` (`center_id`),
  CONSTRAINT `FKbvbig13gxsu8gxaw9h6uemhk4` FOREIGN KEY (`manufacturer_model_id`) REFERENCES `manufacturer_model` (`id`),
  CONSTRAINT `FKnfu3vqdsoj1y339uq7alaltjv` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
