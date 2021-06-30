CREATE TABLE `related_datasets` (
  `study_id` bigint(20) NOT NULL,
  `dataset_id` bigint(20) NOT NULL,
  KEY `FKdj59kuldlkr2ufy5j5dy33akv` (`dataset_id`),
  KEY `FKb0atgqxasc1nctk59hb632j93` (`study_id`),
  CONSTRAINT `FKb0atgqxasc1nctk59hb632j93` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`),
  CONSTRAINT `FKdj59kuldlkr2ufy5j5dy33akv` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`)
)
