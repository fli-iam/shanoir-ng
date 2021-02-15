CREATE TABLE `data_user_agreement_file` (
  `study_id` bigint(20) NOT NULL,
  `path` varchar(255) DEFAULT NULL,
  KEY `FKnmg0gxlptf2nqktd0jj5hvi64` (`study_id`),
  CONSTRAINT `FKnmg0gxlptf2nqktd0jj5hvi64` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
