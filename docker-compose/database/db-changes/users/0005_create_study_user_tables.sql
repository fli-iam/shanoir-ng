CREATE TABLE `study_user` (
  `id` bigint(20) NOT NULL,
  `confirmed` bit(1) NOT NULL,
  `receive_study_user_report` bit(1) NOT NULL,
  `receive_new_import_report` bit(1) NOT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `user_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `study_user_idx` (`study_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `study_user_center` (
  `study_user_id` bigint(20) NOT NULL,
  `center_id` bigint(20) NOT NULL,
  KEY `FK8jvoy3dqkninlrimnrb8endp3` (`study_user_id`),
  CONSTRAINT `FK8jvoy3dqkninlrimnrb8endp3` FOREIGN KEY (`study_user_id`) REFERENCES `study_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


CREATE TABLE `study_user_study_user_rights` (
  `study_user_id` bigint(20) NOT NULL,
  `study_user_rights` int(11) DEFAULT NULL,
  KEY `FK6ipbom6lji60h38bd3ok2r098` (`study_user_id`),
  CONSTRAINT `FK6ipbom6lji60h38bd3ok2r098` FOREIGN KEY (`study_user_id`) REFERENCES `study_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;


INSERT INTO users.study_user
SELECT id, confirmed, receive_new_import_report, receive_study_user_report, study_id, user_id, COALESCE(user_name, '') 
FROM studies.study_user;

INSERT INTO users.study_user_center
SELECT study_user_id, center_id 
FROM studies.study_user_center;

INSERT INTO users.study_user_study_user_rights
SELECT study_user_id, study_user_rights 
FROM studies.study_user_study_user_rights;