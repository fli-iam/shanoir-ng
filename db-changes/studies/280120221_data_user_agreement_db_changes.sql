CREATE TABLE `data_user_agreement_file` (
  `study_id` bigint(20) NOT NULL,
  `path` varchar(255) DEFAULT NULL,
  KEY `FKnmg0gxlptf2nqktd0jj5hvi64` (`study_id`),
  CONSTRAINT `FKnmg0gxlptf2nqktd0jj5hvi64` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `data_user_agreement` (
  id bigint(20) PRIMARY KEY NOT NULL,
  timestamp_of_accepted timestamp,
  timestamp_of_new timestamp,
  `user_id` bigint(20) NOT NULL,
  `study_id` bigint(20) NOT NULL,
  KEY `FKrt509nksblm8s9f7f9ehfjxd` (`study_id`),
  CONSTRAINT `FKrt509nksblm8s9f7f9ehfjxd` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE study_user ADD COLUMN confirmed bit NOT NULL DEFAULT TRUE;