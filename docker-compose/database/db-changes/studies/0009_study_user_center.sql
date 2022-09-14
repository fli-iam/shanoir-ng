CREATE TABLE `study_user_center` (
  `study_user_id` bigint(20) NOT NULL,
  `center_id` bigint(20) NOT NULL,
  KEY `FK8jvoy3dqkninlrimnrb8endp3` (`study_user_id`),
  CONSTRAINT `FK51yprtlva5a0y0kckgtq2phre` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`),
  CONSTRAINT `FK8jvoy3dqkninlrimnrb8endp3` FOREIGN KEY (`study_user_id`) REFERENCES `study_user` (`id`)
)
