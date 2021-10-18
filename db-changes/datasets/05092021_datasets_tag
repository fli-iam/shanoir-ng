CREATE TABLE `tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `color` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKc1j2e615fytq8hsd0usqf49ia` (`study_id`),
  CONSTRAINT `FKc1j2e615fytq8hsd0usqf49ia` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
);


CREATE TABLE `subject_study` (
  `id` bigint(20) NOT NULL,
  `study_id` bigint(20) NOT NULL,
  `subject_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `study_subject_idx` (`study_id`,`subject_id`),
  KEY `FK6iuhtwq9ujtyfywgjfct2m0jf` (`subject_id`),
  CONSTRAINT `FK6iuhtwq9ujtyfywgjfct2m0jf` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`),
  CONSTRAINT `FKr4fo2f7o9ggrr6b06qmq6h373` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
);

CREATE TABLE `subject_study_tag` (
  `subject_study_id` bigint(20) NOT NULL,
  `tags_id` bigint(20) NOT NULL,
  KEY `FKcc51xo3yrp74v2yp7df540a03` (`tags_id`),
  KEY `FKe638djnyhwckgsoa7qxnvcayd` (`subject_study_id`),
  CONSTRAINT `FKcc51xo3yrp74v2yp7df540a03` FOREIGN KEY (`tags_id`) REFERENCES `tag` (`id`),
  CONSTRAINT `FKe638djnyhwckgsoa7qxnvcayd` FOREIGN KEY (`subject_study_id`) REFERENCES `subject_study` (`id`)
);

ALTER TABLE `shanoir_metadata` ADD COLUMN `subject_id` bigint(20) DEFAULT NULL;