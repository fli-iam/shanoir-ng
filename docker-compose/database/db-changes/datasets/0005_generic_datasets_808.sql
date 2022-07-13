 CREATE TABLE `generic_dataset_acquisition` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKbc4bfdipqkw24d9c1itk5umkk` FOREIGN KEY (`id`) REFERENCES `dataset_acquisition` (`id`)
)

CREATE TABLE `generic_dataset` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK5lswb4d1wgnrwti4l9lvi3a8v` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
)
