CREATE TABLE `study_tag` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `color` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKboew1v3lqqa0afxnigq4fxhf3` (`study_id`),
  CONSTRAINT `FKboew1v3lqqa0afxnigq4fxhf3` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
);

DROP TABLE study_study_flag;