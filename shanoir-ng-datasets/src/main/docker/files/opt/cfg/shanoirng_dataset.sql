use shanoir_ng_datasets;

CREATE TABLE `dataset_acquisition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `acquisition_equipment_id` bigint(20) NOT NULL,
  `examination_id` bigint(20) DEFAULT NULL,
  `rank` int(11) DEFAULT NULL,
  `software_release` varchar(255) DEFAULT NULL,
  `sorting_index` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=utf8;

CREATE TABLE `dataset` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cardinality_of_related_subjects` int(11) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `creation_date` datetime DEFAULT NULL,
  `dataset_modality_type` int(11) DEFAULT NULL,
  `explored_entity` int(11) DEFAULT NULL,
  `group_of_subjects_id` bigint(20) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `processed_dataset_type` int(11) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  `subject_id` bigint(20) DEFAULT NULL,
  `dataset_acquisition_id` bigint(20) DEFAULT NULL,
  `dataset_aprocessing_id` bigint(20) DEFAULT NULL,
  `referenced_dataset_for_superimposition_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjkg5k0bq6iwsbti7gk8qs45n9` (`dataset_acquisition_id`),
  KEY `FKe9f1d0hhxgaae17g24s4ma4if` (`dataset_aprocessing_id`),
  KEY `FKnxwkicqfi0bhxcf4vhxy6ter1` (`referenced_dataset_for_superimposition_id`),
  CONSTRAINT `FKe9f1d0hhxgaae17g24s4ma4if` FOREIGN KEY (`dataset_aprocessing_id`) REFERENCES `dataset_processing` (`id`),
  CONSTRAINT `FKjkg5k0bq6iwsbti7gk8qs45n9` FOREIGN KEY (`dataset_acquisition_id`) REFERENCES `dataset_acquisition` (`id`),
  CONSTRAINT `FKnxwkicqfi0bhxcf4vhxy6ter1` FOREIGN KEY (`referenced_dataset_for_superimposition_id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

CREATE TABLE `dataset_expression` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` datetime DEFAULT NULL,
  `dataset_expression_format` int(11) DEFAULT NULL,
  `dataset_processing_type` int(11) DEFAULT NULL,
  `frame_count` int(11) DEFAULT NULL,
  `multi_frame` bit(1) NOT NULL,
  `nifti_converter_id` bigint(20) DEFAULT NULL,
  `nifti_converter_version` varchar(255) DEFAULT NULL,
  `original_nifti_conversion` bit(1) DEFAULT NULL,
  `dataset_id` bigint(20) DEFAULT NULL,
  `original_dataset_expression_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkghumgqet57oieemystcltu89` (`dataset_id`),
  KEY `FKmhd3h6fq16j3fmije68s3wr5` (`original_dataset_expression_id`),
  CONSTRAINT `FKkghumgqet57oieemystcltu89` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`),
  CONSTRAINT `FKmhd3h6fq16j3fmije68s3wr5` FOREIGN KEY (`original_dataset_expression_id`) REFERENCES `dataset_expression` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dataset_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `path` varchar(255) DEFAULT NULL,
  `dataset_expression_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKab9ewwdtyfj9cv5senm315hfd` (`dataset_expression_id`),
  CONSTRAINT `FKab9ewwdtyfj9cv5senm315hfd` FOREIGN KEY (`dataset_expression_id`) REFERENCES `dataset_expression` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dataset_processing` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment` varchar(255) DEFAULT NULL,
  `dataset_processing_type` int(11) DEFAULT NULL,
  `processing_date` tinyblob,
  `study_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `input_of_dataset_processing` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dataset_id` bigint(20) DEFAULT NULL,
  `dataset_processing_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKkx7tyqxgturnph56eamy8b2j` (`dataset_id`),
  KEY `FKdbsm0t582hy51ji3997t1q530` (`dataset_processing_id`),
  CONSTRAINT `FKdbsm0t582hy51ji3997t1q530` FOREIGN KEY (`dataset_processing_id`) REFERENCES `dataset_processing` (`id`),
  CONSTRAINT `FKkx7tyqxgturnph56eamy8b2j` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
