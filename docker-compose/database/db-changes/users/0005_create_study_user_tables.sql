CREATE TABLE study_user
(
  confirmed bit NOT NULL,
  receive_new_import_report bit NOT NULL,
  receive_study_user_report bit NOT NULL,
  id bigint PRIMARY KEY NOT NULL,
  study_id bigint,
  user_id bigint,
  user_name varchar(255)
);

ALTER TABLE study_user
ADD CONSTRAINT FKc4ftmuoc0u0ghw43dxth2m8we
FOREIGN KEY (study_id)
REFERENCES study(id);

CREATE UNIQUE INDEX study_user_idx ON study_user
(
 study_id,
 user_id
);


CREATE TABLE `study_user_center` (
  `center_id` bigint(20) NOT NULL,
  `study_user_id` bigint(20) NOT NULL,
  KEY `FKbwq3j3rtbtndcifv9l6otdjtt` (`center_id`),
  KEY `FK8jvoy3dqkninlrimnrb8endp3` (`study_user_id`),  
  CONSTRAINT `FKbwq3j3rtbtndcifv9l6otdjtt` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`),
  CONSTRAINT `FK8jvoy3dqkninlrimnrb8endp3` FOREIGN KEY (`study_user_id`) REFERENCES `study_user` (`id`)
);


CREATE TABLE `study_user_study_user_rights` (
  `study_user_rights` int(11) DEFAULT NULL,
  `study_user_id` bigint(20) NOT NULL,
  KEY `FK6ipbom6lji60h38bd3ok2r098` (`study_user_id`),
  CONSTRAINT `FK6ipbom6lji60h38bd3ok2r098` FOREIGN KEY (`study_user_id`) REFERENCES `study_user` (`id`)
);