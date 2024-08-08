CREATE TABLE `study_user` (
  `confirmed` bit(1) NOT NULL,
  `receive_new_import_report` bit(1) NOT NULL,
  `receive_study_user_report` bit(1) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `study_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `user_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `study_user_idx` (`study_id`,`user_id`),
  CONSTRAINT `FKc4ftmuoc0u0ghw43dxth2m8we` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
);


CREATE TABLE `study_user_center` (
  `center_id` bigint(20) NOT NULL,
  `study_user_id` bigint(20) NOT NULL,
  KEY `FK8jvoy3dqkninlrimnrb8endp3` (`study_user_id`),
  CONSTRAINT `FK8jvoy3dqkninlrimnrb8endp3` FOREIGN KEY (`study_user_id`) REFERENCES `study_user` (`id`)
);


CREATE TABLE `study_user_study_user_rights` (
  `study_user_rights` int(11) DEFAULT NULL,
  `study_user_id` bigint(20) NOT NULL,
  KEY `FK6ipbom6lji60h38bd3ok2r098` (`study_user_id`),
  CONSTRAINT `FK6ipbom6lji60h38bd3ok2r098` FOREIGN KEY (`study_user_id`) REFERENCES `study_user` (`id`)
);