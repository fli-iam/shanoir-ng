use shanoir_ng_studies;

ALTER TABLE `study` CHANGE `Clinical` `clinical` bit(1) NOT NULL;
ALTER TABLE `study` MODIFY `study_type` int(11) DEFAULT NULL;

ALTER TABLE `study_user` DROP PRIMARY KEY;
ALTER TABLE `study_user` DROP COLUMN `id`;
ALTER TABLE `study_user` ADD CONSTRAINT `UC_User_Study` UNIQUE (`user_id`,`study_id`);

ALTER TABLE `subject_study` DROP FOREIGN KEY `FKelblgk1bc448k1jrlaq0lue5d`;
ALTER TABLE `subject_study` DROP FOREIGN KEY `FKft8kki1chfo5h4q8l64slqb4c`;
ALTER TABLE `subject_study` DROP COLUMN `study`;
ALTER TABLE `subject_study` DROP COLUMN `subject`;
ALTER TABLE `subject_study` ADD COLUMN `study_id` bigint(20) DEFAULT NULL;
ALTER TABLE `subject_study` ADD COLUMN `subject_id` bigint(20) DEFAULT NULL;
ALTER TABLE `subject_study` ADD CONSTRAINT `FKr4fo2f7o9ggrr6b06qmq6h373` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`);
ALTER TABLE `subject_study` ADD CONSTRAINT `FK6iuhtwq9ujtyfywgjfct2m0jf` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`);

CREATE TABLE `protocol_file_path` (
  `study_id` bigint(20) NOT NULL,
  `path` varchar(255) DEFAULT NULL,
  KEY `FK1k7xvi02wcbuvj5cs8xp9c6h4` (`study_id`),
  CONSTRAINT `FK1k7xvi02wcbuvj5cs8xp9c6h4` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `study_examination` (
  `study_id` bigint(20) NOT NULL,
  `examination_id` bigint(20) DEFAULT NULL,
  KEY `FKlbokvx0u8921ujhfyh1751ssl` (`study_id`),
  CONSTRAINT `FKlbokvx0u8921ujhfyh1751ssl` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `group_of_subjects` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL,
  `group_name` varchar(255) NOT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6fmx594ux0memkouiu5ygirjo` (`study_id`),
  CONSTRAINT `FK6fmx594ux0memkouiu5ygirjo` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `subject_group_of_subjects` (
  `id` bigint(20) NOT NULL,
  `group_of_subjects_id` bigint(20) DEFAULT NULL,
  `subject_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdw1w3b6n98k0obbnyif6hnyb7` (`group_of_subjects_id`),
  KEY `FKbuuj437rygsa1u24tfbwicxnh` (`subject_id`),
  CONSTRAINT `FKbuuj437rygsa1u24tfbwicxnh` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`),
  CONSTRAINT `FKdw1w3b6n98k0obbnyif6hnyb7` FOREIGN KEY (`group_of_subjects_id`) REFERENCES `group_of_subjects` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `coil` (
  `id` bigint(20) NOT NULL,
  `coil_type` int(11) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `number_of_channels` bigint(20) DEFAULT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `center_id` bigint(20) NOT NULL,
  `manufacturer_model_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2c4pj1yt8xsha1dn6r8p1bhtu` (`center_id`),
  KEY `FKhkauya7hqlh56r9fd9c9mgh39` (`manufacturer_model_id`),
  CONSTRAINT `FK2c4pj1yt8xsha1dn6r8p1bhtu` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`),
  CONSTRAINT `FKhkauya7hqlh56r9fd9c9mgh39` FOREIGN KEY (`manufacturer_model_id`) REFERENCES `manufacturer_model` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `timepoint` (
  `id` bigint(20) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `days` bigint(20) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `rank` bigint(20) NOT NULL,
  `study_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo2x3e9dhemdt9alu8okyx4eq8` (`study_id`),
  CONSTRAINT `FKo2x3e9dhemdt9alu8okyx4eq8` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
