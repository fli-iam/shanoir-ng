CREATE TABLE `study_user_center` (
  `study_user_id` bigint(20) NOT NULL,
  `centers_id` bigint(20) NOT NULL,
  UNIQUE KEY `UK_sd736idk992j4iimveitdi5j3` (`centers_id`),
  KEY `FK8jvoy3dqkninlrimnrb8endp3` (`study_user_id`),
  CONSTRAINT `FK8jvoy3dqkninlrimnrb8endp3` FOREIGN KEY (`study_user_id`) REFERENCES `study_user` (`id`)
)
