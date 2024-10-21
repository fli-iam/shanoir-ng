CREATE TABLE `study_tag` (
  `id` bigint(20) NOT NULL,
  `color` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKboew1v3lqqa0afxnigq4fxhf3` (`study_id`),
  CONSTRAINT `FKboew1v3lqqa0afxnigq4fxhf3` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
 );
 CREATE TABLE `dataset_tag` (
  `dataset_id` bigint(20) NOT NULL,
  `study_tag_id` bigint(20) NOT NULL,
  KEY `FKkh92b0ddi9nxrevqkdmvqpcm3` (`study_tag_id`),
  KEY `FKd0dkfmqchgw18bxirml5an8ex` (`dataset_id`),
  CONSTRAINT `FKd0dkfmqchgw18bxirml5an8ex` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`),
  CONSTRAINT `FKkh92b0ddi9nxrevqkdmvqpcm3` FOREIGN KEY (`study_tag_id`) REFERENCES `study_tag` (`id`)
);
