use shanoir_ng_datasets;

CREATE TABLE `scientific_article` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `scientific_article_reference` varchar(255) NOT NULL,
  `scientific_article_type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `timepoint` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment` varchar(255) DEFAULT NULL,
  `days` bigint(20) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `rank` bigint(20) NOT NULL,
  `study_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `instrument` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `acronym` varchar(255) NOT NULL,
  `instrument_type` int(11) NOT NULL,
  `mono_domain` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `passation_mode` int(11) NOT NULL,
  `instrument_definition_article_id` bigint(20) DEFAULT NULL,
  `parent_instrument_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKldjpl37ywpdpg374wsiiibg3o` (`instrument_definition_article_id`),
  KEY `FK9v8avc60hd7jlrpmb8c2qsxgp` (`parent_instrument_id`),
  CONSTRAINT `FK9v8avc60hd7jlrpmb8c2qsxgp` FOREIGN KEY (`parent_instrument_id`) REFERENCES `instrument` (`id`),
  CONSTRAINT `FKldjpl37ywpdpg374wsiiibg3o` FOREIGN KEY (`instrument_definition_article_id`) REFERENCES `scientific_article` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `instrument_variable` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `age_dependent` bit(1) NOT NULL,
  `cultural_skill_dependent` bit(1) NOT NULL,
  `domain` int(11) NOT NULL,
  `main` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `quality` int(11) NOT NULL,
  `sex_dependent` bit(1) NOT NULL,
  `standardized` bit(1) NOT NULL,
  `instrument_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK8fnx6rwx1mom80u31e4rnr4t2` (`instrument_id`),
  CONSTRAINT `FK8fnx6rwx1mom80u31e4rnr4t2` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `instrument_domains` (
  `instrument_id` bigint(20) NOT NULL,
  `domain` int(11) DEFAULT NULL,
  KEY `FKonob8bf37gfchgj8lnca1gtbr` (`instrument_id`),
  CONSTRAINT `FKonob8bf37gfchgj8lnca1gtbr` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `examination` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `center_id` bigint(20) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `examination_date` datetime NOT NULL,
  `experimental_group_of_subjects_id` bigint(20) DEFAULT NULL,
  `investigator_center_id` bigint(20) DEFAULT NULL,
  `investigator_external` bit(1) NOT NULL,
  `investigator_id` bigint(20) NOT NULL,
  `note` longtext,
  `study_id` bigint(20) NOT NULL,
  `subject_id` bigint(20) DEFAULT NULL,
  `subject_weight` double DEFAULT NULL,
  `weight_unit_of_measure` int(11) DEFAULT NULL,
  `timepoint_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr6tia0a2qg6r44r8cp6a8x771` (`timepoint_id`),
  CONSTRAINT `FKr6tia0a2qg6r44r8cp6a8x771` FOREIGN KEY (`timepoint_id`) REFERENCES `timepoint` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `instrument_based_assessment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `examination_id` bigint(20) NOT NULL,
  `instrument_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3rdh4s9m39qlspmv28ue58g3s` (`examination_id`),
  KEY `FKe06v6fp5eds8dt0kitecb867u` (`instrument_id`),
  CONSTRAINT `FK3rdh4s9m39qlspmv28ue58g3s` FOREIGN KEY (`examination_id`) REFERENCES `examination` (`id`),
  CONSTRAINT `FKe06v6fp5eds8dt0kitecb867u` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `variable_assessment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instrument_based_assessment_id` bigint(20) NOT NULL,
  `instrument_variable_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKao37t99o9ybo9jm0w4p0ava40` (`instrument_based_assessment_id`),
  KEY `FK7lkkmgy4eamnl7n0iv1y587r9` (`instrument_variable_id`),
  CONSTRAINT `FK7lkkmgy4eamnl7n0iv1y587r9` FOREIGN KEY (`instrument_variable_id`) REFERENCES `instrument_variable` (`id`),
  CONSTRAINT `FKao37t99o9ybo9jm0w4p0ava40` FOREIGN KEY (`instrument_based_assessment_id`) REFERENCES `instrument_based_assessment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `variable_assessment_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKax4en8sb6r08ocu5tjqxxnke5` (`variable_assessment_id`),
  CONSTRAINT `FKax4en8sb6r08ocu5tjqxxnke5` FOREIGN KEY (`variable_assessment_id`) REFERENCES `variable_assessment` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `extra_data_file_pathlist_table` (
  `examination_id` bigint(20) NOT NULL,
  `extra_data_file_pathlist` varchar(255) DEFAULT NULL,
  KEY `FKsa2t6lt66decp890btytoscaw` (`examination_id`),
  CONSTRAINT `FKsa2t6lt66decp890btytoscaw` FOREIGN KEY (`examination_id`) REFERENCES `examination` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dataset_processing` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment` varchar(255) DEFAULT NULL,
  `dataset_processing_type` int(11) DEFAULT NULL,
  `processing_date` tinyblob,
  `study_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `dataset_acquisition` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `acquisition_equipment_id` bigint(20) NOT NULL,
  `rank` int(11) DEFAULT NULL,
  `software_release` varchar(255) DEFAULT NULL,
  `sorting_index` int(11) DEFAULT NULL,
  `examination_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjlkt90yo04ld9stkmr4k1pam` (`examination_id`),
  CONSTRAINT `FKjlkt90yo04ld9stkmr4k1pam` FOREIGN KEY (`examination_id`) REFERENCES `examination` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

