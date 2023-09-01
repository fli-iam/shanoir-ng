CREATE TABLE `dataset_property` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  `dataset_id` bigint(20) DEFAULT NULL,
  `dataset_processing_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1mcyt0h60808jmj3shlvge9c8` (`dataset_id`),
  KEY `FKl09g6qevg8filjycsylycbdxw` (`dataset_processing_id`),
  CONSTRAINT `FK1mcyt0h60808jmj3shlvge9c8` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`),
  CONSTRAINT `FKl09g6qevg8filjycsylycbdxw` FOREIGN KEY (`dataset_processing_id`) REFERENCES `dataset_processing` (`id`)
)

