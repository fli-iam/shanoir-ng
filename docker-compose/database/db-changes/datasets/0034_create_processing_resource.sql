CREATE TABLE `processing_resource` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_id` varchar(255) NOT NULL,
  `dataset_id` bigint(20) DEFAULT NULL,
  `processing_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK7971ykr4l94b3qgns4do405qe` (`dataset_id`),
  KEY `FKtkm1ww8m49x3jdwdtocijhrha` (`processing_id`),
  CONSTRAINT `FK7971ykr4l94b3qgns4do405qe` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`),
  CONSTRAINT `FKtkm1ww8m49x3jdwdtocijhrha` FOREIGN KEY (`processing_id`) REFERENCES `dataset_processing` (`id`)
)
