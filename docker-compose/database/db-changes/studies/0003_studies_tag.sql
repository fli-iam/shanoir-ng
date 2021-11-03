CREATE TABLE `tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `color` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc1j2e615fytq8hsd0usqf49ia` (`study_id`),
  CONSTRAINT `FKc1j2e615fytq8hsd0usqf49ia` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
);

CREATE TABLE `subject_study_tag` (
  `subject_study_id` bigint(20) NOT NULL,
  `tags_id` bigint(20) NOT NULL,
  KEY `FKcc51xo3yrp74v2yp7df540a03` (`tags_id`),
  KEY `FKe638djnyhwckgsoa7qxnvcayd` (`subject_study_id`),
  CONSTRAINT `FKcc51xo3yrp74v2yp7df540a03` FOREIGN KEY (`tags_id`) REFERENCES `tag` (`id`),
  CONSTRAINT `FKe638djnyhwckgsoa7qxnvcayd` FOREIGN KEY (`subject_study_id`) REFERENCES `subject_study` (`id`)
);