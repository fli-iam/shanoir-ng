-- MySQL dump 10.13  Distrib 8.0.17, for Win64 (x86_64)
--
-- Host: localhost    Database: datasets
-- ------------------------------------------------------
-- Server version	5.7.27-log

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `examination`
--

DROP TABLE IF EXISTS `examination`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `examination` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `center_id` bigint(20) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `examination_date` date NOT NULL,
  `experimental_group_of_subjects_id` bigint(20) DEFAULT NULL,
  `investigator_center_id` bigint(20) DEFAULT NULL,
  `investigator_external` bit(1) NOT NULL,
  `investigator_id` bigint(20) DEFAULT NULL,
  `note` longtext,
  `preclinical` bit(1) NOT NULL DEFAULT b'0',
  `study_id` bigint(20) NOT NULL,
  `subject_id` bigint(20) DEFAULT NULL,
  `subject_weight` double DEFAULT NULL,
  `timepoint_id` bigint(20) DEFAULT NULL,
  `weight_unit_of_measure` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11741 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dataset_acquisition`
--

DROP TABLE IF EXISTS `dataset_acquisition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=183442 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dataset_metadata`
--

DROP TABLE IF EXISTS `dataset_metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dataset_metadata` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cardinality_of_related_subjects` int(11) NOT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `dataset_modality_type` int(11) DEFAULT NULL,
  `explored_entity` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `processed_dataset_type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=321657 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dataset_processing`
--

DROP TABLE IF EXISTS `dataset_processing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dataset_processing` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment` varchar(255) DEFAULT NULL,
  `dataset_processing_type` int(11) DEFAULT NULL,
  `processing_date` tinyblob,
  `study_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=337 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dataset`
--

DROP TABLE IF EXISTS `dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dataset` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `creation_date` date DEFAULT NULL,
  `group_of_subjects_id` bigint(20) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  `subject_id` bigint(20) DEFAULT NULL,
  `dataset_acquisition_id` bigint(20) DEFAULT NULL,
  `dataset_processing_id` bigint(20) DEFAULT NULL,
  `origin_metadata_id` bigint(20) DEFAULT NULL,
  `referenced_dataset_for_superimposition_id` bigint(20) DEFAULT NULL,
  `updated_metadata_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKjkg5k0bq6iwsbti7gk8qs45n9` (`dataset_acquisition_id`),
  KEY `FK4vib3rui8pme1oq505fcosjfl` (`dataset_processing_id`),
  KEY `FK6mnu5quxanbou4iew3a8t2mwc` (`origin_metadata_id`),
  KEY `FKnxwkicqfi0bhxcf4vhxy6ter1` (`referenced_dataset_for_superimposition_id`),
  KEY `FKrp3auy5ka4dxe8up1pltmub41` (`updated_metadata_id`),
  CONSTRAINT `FK4vib3rui8pme1oq505fcosjfl` FOREIGN KEY (`dataset_processing_id`) REFERENCES `dataset_processing` (`id`),
  CONSTRAINT `FK6mnu5quxanbou4iew3a8t2mwc` FOREIGN KEY (`origin_metadata_id`) REFERENCES `dataset_metadata` (`id`),
  CONSTRAINT `FKjkg5k0bq6iwsbti7gk8qs45n9` FOREIGN KEY (`dataset_acquisition_id`) REFERENCES `dataset_acquisition` (`id`),
  CONSTRAINT `FKnxwkicqfi0bhxcf4vhxy6ter1` FOREIGN KEY (`referenced_dataset_for_superimposition_id`) REFERENCES `dataset` (`id`),
  CONSTRAINT `FKrp3auy5ka4dxe8up1pltmub41` FOREIGN KEY (`updated_metadata_id`) REFERENCES `dataset_metadata` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=321657 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `calibration_dataset`
--

DROP TABLE IF EXISTS `calibration_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `calibration_dataset` (
  `calibration_dataset_type` int(11) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKdlyq1k6loyh9hwl564tfyx2w5` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ct_dataset`
--

DROP TABLE IF EXISTS `ct_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_dataset` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKa543uue55g1k7hkw0un1dip9u` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ct_protocol`
--

DROP TABLE IF EXISTS `ct_protocol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_protocol` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `ct_dataset_acquisition`
--

DROP TABLE IF EXISTS `ct_dataset_acquisition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `ct_dataset_acquisition` (
  `id` bigint(20) NOT NULL,
  `ct_protocol_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKrf86wblh9s84tt1d6b9cqhbik` (`ct_protocol_id`),
  CONSTRAINT `FKnbcl0katei6jrf0fuvtnbf5qh` FOREIGN KEY (`id`) REFERENCES `dataset_acquisition` (`id`),
  CONSTRAINT `FKrf86wblh9s84tt1d6b9cqhbik` FOREIGN KEY (`ct_protocol_id`) REFERENCES `ct_protocol` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dataset_expression`
--

DROP TABLE IF EXISTS `dataset_expression`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `dataset_file`
--

DROP TABLE IF EXISTS `dataset_file`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `dataset_file` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `pacs` bit(1) NOT NULL,
  `path` text,
  `dataset_expression_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKab9ewwdtyfj9cv5senm315hfd` (`dataset_expression_id`),
  CONSTRAINT `FKab9ewwdtyfj9cv5senm315hfd` FOREIGN KEY (`dataset_expression_id`) REFERENCES `dataset_expression` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mr_dataset_metadata`
--

DROP TABLE IF EXISTS `mr_dataset_metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mr_dataset_metadata` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `mr_dataset_nature` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=321657 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mr_dataset`
--

DROP TABLE IF EXISTS `mr_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mr_dataset` (
  `mr_quality_procedure_type` int(11) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  `origin_mr_metadata_id` bigint(20) DEFAULT NULL,
  `updated_mr_metadata_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnpbr2fl5d8gkm6cmu3l0vga5w` (`origin_mr_metadata_id`),
  KEY `FKlnqv7esnc1gtg6jg1k0qgdd5p` (`updated_mr_metadata_id`),
  CONSTRAINT `FKdnipc0t8no2h5u9hg7p62cmyf` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`),
  CONSTRAINT `FKlnqv7esnc1gtg6jg1k0qgdd5p` FOREIGN KEY (`updated_mr_metadata_id`) REFERENCES `mr_dataset_metadata` (`id`),
  CONSTRAINT `FKnpbr2fl5d8gkm6cmu3l0vga5w` FOREIGN KEY (`origin_mr_metadata_id`) REFERENCES `mr_dataset_metadata` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mr_protocol_metadata`
--

DROP TABLE IF EXISTS `mr_protocol_metadata`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mr_protocol_metadata` (
  `dtype` int(11) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `acquisition_contrast` int(11) DEFAULT NULL,
  `contrast_agent_concentration` double DEFAULT NULL,
  `contrast_agent_used` int(11) DEFAULT NULL,
  `injected_volume` double DEFAULT NULL,
  `magnetization_transfer` bit(1) DEFAULT NULL,
  `mr_sequencekspace_fill` int(11) DEFAULT NULL,
  `mr_sequence_name` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `parallel_acquisition` bit(1) DEFAULT NULL,
  `parallel_acquisition_technique` int(11) DEFAULT NULL,
  `receiving_coil_id` bigint(20) DEFAULT NULL,
  `slice_order` int(11) DEFAULT NULL,
  `slice_orientation_at_acquisition` int(11) DEFAULT NULL,
  `time_reduction_factor_for_the_in_plane_direction` double DEFAULT NULL,
  `time_reduction_factor_for_the_out_of_plane_direction` double DEFAULT NULL,
  `transmitting_coil_id` bigint(20) DEFAULT NULL,
  `axis_orientation_at_acquisition` int(11) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `contrast_agent_product` varchar(255) DEFAULT NULL,
  `mr_sequence_application` int(11) DEFAULT NULL,
  `mr_sequence_physics` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=183442 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mr_protocol`
--

DROP TABLE IF EXISTS `mr_protocol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mr_protocol` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `acquisition_duration` double DEFAULT NULL,
  `acquisition_resolutionx` int(11) DEFAULT NULL,
  `acquisition_resolutiony` int(11) DEFAULT NULL,
  `echo_train_length` int(11) DEFAULT NULL,
  `filters` varchar(255) DEFAULT NULL,
  `fovx` double DEFAULT NULL,
  `fovy` double DEFAULT NULL,
  `imaged_nucleus` int(11) DEFAULT NULL,
  `imaging_frequency` double DEFAULT NULL,
  `number_of_averages` int(11) DEFAULT NULL,
  `number_of_phase_encoding_steps` int(11) DEFAULT NULL,
  `number_of_temporal_positions` int(11) DEFAULT NULL,
  `patient_position` int(11) DEFAULT NULL,
  `percent_phase_fov` double DEFAULT NULL,
  `percent_sampling` double DEFAULT NULL,
  `pixel_bandwidth` double DEFAULT NULL,
  `pixel_spacingx` double DEFAULT NULL,
  `pixel_spacingy` double DEFAULT NULL,
  `slice_spacing` double DEFAULT NULL,
  `slice_thickness` double DEFAULT NULL,
  `temporal_resolution` double DEFAULT NULL,
  `origin_metadata_id` bigint(20) DEFAULT NULL,
  `updated_metadata_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4gm6lnao8ysuok2u1r2vyughy` (`origin_metadata_id`),
  KEY `FKopgo8t6om1xcyberh5evka7j2` (`updated_metadata_id`),
  CONSTRAINT `FK4gm6lnao8ysuok2u1r2vyughy` FOREIGN KEY (`origin_metadata_id`) REFERENCES `mr_protocol_metadata` (`id`),
  CONSTRAINT `FKopgo8t6om1xcyberh5evka7j2` FOREIGN KEY (`updated_metadata_id`) REFERENCES `mr_protocol_metadata` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=183442 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `diffusion_gradient`
--

DROP TABLE IF EXISTS `diffusion_gradient`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `diffusion_gradient` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `diffusion_gradientbvalue` double NOT NULL,
  `diffusion_gradient_orientationx` double NOT NULL,
  `diffusion_gradient_orientationy` double NOT NULL,
  `diffusion_gradient_orientationz` double NOT NULL,
  `mr_dataset_id` bigint(20) DEFAULT NULL,
  `mr_protocol_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6bx7fuj68kun7qt6b1iwbn4tp` (`mr_dataset_id`),
  KEY `FK1s2racr65xpajre4hcoeemova` (`mr_protocol_id`),
  CONSTRAINT `FK1s2racr65xpajre4hcoeemova` FOREIGN KEY (`mr_protocol_id`) REFERENCES `mr_protocol` (`id`),
  CONSTRAINT `FK6bx7fuj68kun7qt6b1iwbn4tp` FOREIGN KEY (`mr_dataset_id`) REFERENCES `mr_dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `echo_time`
--

DROP TABLE IF EXISTS `echo_time`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `echo_time` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `echo_number` int(11) DEFAULT NULL,
  `echo_time_value` double NOT NULL,
  `mr_dataset_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKhhwvyuw0tvt17m47w6dg1u7b8` (`mr_dataset_id`),
  CONSTRAINT `FKhhwvyuw0tvt17m47w6dg1u7b8` FOREIGN KEY (`mr_dataset_id`) REFERENCES `mr_dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `eeg_dataset`
--

DROP TABLE IF EXISTS `eeg_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `eeg_dataset` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKo2jjh4q0t5mvn9y1rh0alqf2b` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `extra_data_file_path`
--

DROP TABLE IF EXISTS `extra_data_file_path`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `extra_data_file_path` (
  `examination_id` bigint(20) NOT NULL,
  `path` varchar(255) DEFAULT NULL,
  KEY `FK6a8yu6r8kvfy3cqpyrujwicko` (`examination_id`),
  CONSTRAINT `FK6a8yu6r8kvfy3cqpyrujwicko` FOREIGN KEY (`examination_id`) REFERENCES `examination` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `flip_angle`
--

DROP TABLE IF EXISTS `flip_angle`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `flip_angle` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `flip_angle_value` varchar(255) NOT NULL,
  `mr_dataset_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKgt1f6jso11pcan2vg23vb4w3i` (`mr_dataset_id`),
  CONSTRAINT `FKgt1f6jso11pcan2vg23vb4w3i` FOREIGN KEY (`mr_dataset_id`) REFERENCES `mr_dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `input_of_dataset_processing`
--

DROP TABLE IF EXISTS `input_of_dataset_processing`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `input_of_dataset_processing` (
  `processing_id` bigint(20) NOT NULL,
  `dataset_id` bigint(20) NOT NULL,
  KEY `FKkx7tyqxgturnph56eamy8b2j` (`dataset_id`),
  KEY `FKrdquad6yq49nna537jo8css6t` (`processing_id`),
  CONSTRAINT `FKkx7tyqxgturnph56eamy8b2j` FOREIGN KEY (`dataset_id`) REFERENCES `dataset` (`id`),
  CONSTRAINT `FKrdquad6yq49nna537jo8css6t` FOREIGN KEY (`processing_id`) REFERENCES `dataset_processing` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `scientific_article`
--

DROP TABLE IF EXISTS `scientific_article`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `scientific_article` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `scientific_article_reference` varchar(255) NOT NULL,
  `scientific_article_type` int(11) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument`
--

DROP TABLE IF EXISTS `instrument`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=30 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument_based_assessment`
--

DROP TABLE IF EXISTS `instrument_based_assessment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `instrument_based_assessment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `examination_id` bigint(20) NOT NULL,
  `instrument_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK3rdh4s9m39qlspmv28ue58g3s` (`examination_id`),
  KEY `FKe06v6fp5eds8dt0kitecb867u` (`instrument_id`),
  CONSTRAINT `FK3rdh4s9m39qlspmv28ue58g3s` FOREIGN KEY (`examination_id`) REFERENCES `examination` (`id`),
  CONSTRAINT `FKe06v6fp5eds8dt0kitecb867u` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=262 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument_domains`
--

DROP TABLE IF EXISTS `instrument_domains`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `instrument_domains` (
  `instrument_id` bigint(20) NOT NULL,
  `domain` int(11) DEFAULT NULL,
  KEY `FKonob8bf37gfchgj8lnca1gtbr` (`instrument_id`),
  CONSTRAINT `FKonob8bf37gfchgj8lnca1gtbr` FOREIGN KEY (`instrument_id`) REFERENCES `instrument` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `instrument_variable`
--

DROP TABLE IF EXISTS `instrument_variable`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
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
) ENGINE=InnoDB AUTO_INCREMENT=38 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `inversion_time`
--

DROP TABLE IF EXISTS `inversion_time`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `inversion_time` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `inversion_time_value` double NOT NULL,
  `mr_dataset_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKcjvq50pwaj8paqtcc0u5qw58r` (`mr_dataset_id`),
  CONSTRAINT `FKcjvq50pwaj8paqtcc0u5qw58r` FOREIGN KEY (`mr_dataset_id`) REFERENCES `mr_dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `meg_dataset`
--

DROP TABLE IF EXISTS `meg_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `meg_dataset` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKq2w8q5r8esvf6i49gul9cy1mu` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mesh_dataset`
--

DROP TABLE IF EXISTS `mesh_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mesh_dataset` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKqmi93jnftco7l78yumd8xtngr` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mr_dataset_acquisition`
--

DROP TABLE IF EXISTS `mr_dataset_acquisition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mr_dataset_acquisition` (
  `id` bigint(20) NOT NULL,
  `mr_protocol_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKmmqwm4sxqkg7s28w4ijtbd74w` (`mr_protocol_id`),
  CONSTRAINT `FK94fbnym1k5qoab3wb1opnqm7n` FOREIGN KEY (`id`) REFERENCES `dataset_acquisition` (`id`),
  CONSTRAINT `FKmmqwm4sxqkg7s28w4ijtbd74w` FOREIGN KEY (`mr_protocol_id`) REFERENCES `mr_protocol` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mr_protocol_metadata_mr_scanning_sequence`
--

DROP TABLE IF EXISTS `mr_protocol_metadata_mr_scanning_sequence`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mr_protocol_metadata_mr_scanning_sequence` (
  `mr_protocol_metadata_id` bigint(20) NOT NULL,
  `mr_scanning_sequence` int(11) DEFAULT NULL,
  KEY `FK3ohkholvlnole9cgvbqa6g5ar` (`mr_protocol_metadata_id`),
  CONSTRAINT `FK3ohkholvlnole9cgvbqa6g5ar` FOREIGN KEY (`mr_protocol_metadata_id`) REFERENCES `mr_protocol_metadata` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `mr_protocol_metadata_mr_sequence_variant`
--

DROP TABLE IF EXISTS `mr_protocol_metadata_mr_sequence_variant`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `mr_protocol_metadata_mr_sequence_variant` (
  `mr_protocol_metadata_id` bigint(20) NOT NULL,
  `mr_sequence_variant` int(11) DEFAULT NULL,
  KEY `FKpyd0e82kiktkr4fu8bxj2h5n2` (`mr_protocol_metadata_id`),
  CONSTRAINT `FKpyd0e82kiktkr4fu8bxj2h5n2` FOREIGN KEY (`mr_protocol_metadata_id`) REFERENCES `mr_protocol_metadata` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `parameter_quantification_dataset`
--

DROP TABLE IF EXISTS `parameter_quantification_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `parameter_quantification_dataset` (
  `parameter_quantification_dataset_nature` int(11) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK8nehljhf7cu8gp775ucoqio18` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pet_dataset`
--

DROP TABLE IF EXISTS `pet_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_dataset` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FK7ggwaa6nb4810nrt8skv73hsh` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pet_protocol`
--

DROP TABLE IF EXISTS `pet_protocol`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_protocol` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `attenuation_correction_method` varchar(255) DEFAULT NULL,
  `convolution_kernel` varchar(255) DEFAULT NULL,
  `decay_correction` varchar(255) DEFAULT NULL,
  `decay_factor` int(11) DEFAULT NULL,
  `dimensionx` int(11) NOT NULL,
  `dimensiony` int(11) NOT NULL,
  `dose_calibration_factor` int(11) DEFAULT NULL,
  `energy_window_lower_limit` int(11) DEFAULT NULL,
  `energy_window_upper_limit` int(11) DEFAULT NULL,
  `number_of_iterations` varchar(255) DEFAULT NULL,
  `number_of_slices` int(11) NOT NULL,
  `number_of_subsets` varchar(255) DEFAULT NULL,
  `radionuclide_half_life` double DEFAULT NULL,
  `radionuclide_total_dose` int(11) DEFAULT NULL,
  `radiopharmaceutical_code` varchar(255) DEFAULT NULL,
  `randoms_correction_method` varchar(255) DEFAULT NULL,
  `reconstruction_method` varchar(255) DEFAULT NULL,
  `rescale_slope` bigint(20) DEFAULT NULL,
  `rescale_type` varchar(255) DEFAULT NULL,
  `scatter_correction_method` varchar(255) DEFAULT NULL,
  `scatter_fraction_factor` int(11) DEFAULT NULL,
  `units` varchar(255) DEFAULT NULL,
  `voxel_sizex` varchar(255) NOT NULL,
  `voxel_sizey` varchar(255) NOT NULL,
  `voxel_sizez` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pet_dataset_acquisition`
--

DROP TABLE IF EXISTS `pet_dataset_acquisition`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pet_dataset_acquisition` (
  `id` bigint(20) NOT NULL,
  `pet_protocol_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2m5gqjwoxg4sj85iy703ovda5` (`pet_protocol_id`),
  CONSTRAINT `FK2m5gqjwoxg4sj85iy703ovda5` FOREIGN KEY (`pet_protocol_id`) REFERENCES `pet_protocol` (`id`),
  CONSTRAINT `FKk6ntqflihbyffrt4s4jxn4y8g` FOREIGN KEY (`id`) REFERENCES `dataset_acquisition` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `registration_dataset`
--

DROP TABLE IF EXISTS `registration_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `registration_dataset` (
  `registration_dataset_type` int(11) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKk937pga0vh36jl9ffua00vi6v` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `repetition_time`
--

DROP TABLE IF EXISTS `repetition_time`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `repetition_time` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `repetition_time_value` double NOT NULL,
  `mr_dataset_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKl164j80t5u7qy6cs4rjh8eyed` (`mr_dataset_id`),
  CONSTRAINT `FKl164j80t5u7qy6cs4rjh8eyed` FOREIGN KEY (`mr_dataset_id`) REFERENCES `mr_dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `variable_assessment`
--

DROP TABLE IF EXISTS `variable_assessment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `variable_assessment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `instrument_based_assessment_id` bigint(20) NOT NULL,
  `instrument_variable_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKao37t99o9ybo9jm0w4p0ava40` (`instrument_based_assessment_id`),
  KEY `FK7lkkmgy4eamnl7n0iv1y587r9` (`instrument_variable_id`),
  CONSTRAINT `FK7lkkmgy4eamnl7n0iv1y587r9` FOREIGN KEY (`instrument_variable_id`) REFERENCES `instrument_variable` (`id`),
  CONSTRAINT `FKao37t99o9ybo9jm0w4p0ava40` FOREIGN KEY (`instrument_based_assessment_id`) REFERENCES `instrument_based_assessment` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=517 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `score`
--

DROP TABLE IF EXISTS `score`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `score` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `variable_assessment_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKax4en8sb6r08ocu5tjqxxnke5` (`variable_assessment_id`),
  CONSTRAINT `FKax4en8sb6r08ocu5tjqxxnke5` FOREIGN KEY (`variable_assessment_id`) REFERENCES `variable_assessment` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=525 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `segmentation_dataset`
--

DROP TABLE IF EXISTS `segmentation_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `segmentation_dataset` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKikr8fbhbvqmher21kp7ib514w` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `spect_dataset`
--

DROP TABLE IF EXISTS `spect_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `spect_dataset` (
  `spect_dataset_nature` int(11) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKnjm68t9hsgkknv5qo97tjofwq` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `statistical_dataset`
--

DROP TABLE IF EXISTS `statistical_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `statistical_dataset` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKg7n0qrdhkh90hudunih7ho9hl` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_cards`
--

DROP TABLE IF EXISTS `study_cards`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `study_cards` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `acquisition_equipment_id` bigint(20) DEFAULT NULL,
  `center_id` bigint(20) DEFAULT NULL,
  `disabled` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `nifti_converter_id` bigint(20) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_innrdxbwjv64hhx7o58oxaj58` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=419 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_user`
--

DROP TABLE IF EXISTS `study_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `study_user` (
  `id` bigint(20) NOT NULL,
  `receive_anonymization_report` bit(1) NOT NULL,
  `receive_new_import_report` bit(1) NOT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `user_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `study_user_idx` (`study_id`,`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_user_study_user_rights`
--

DROP TABLE IF EXISTS `study_user_study_user_rights`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `study_user_study_user_rights` (
  `study_user_id` bigint(20) NOT NULL,
  `study_user_rights` int(11) DEFAULT NULL,
  KEY `FK6ipbom6lji60h38bd3ok2r098` (`study_user_id`),
  CONSTRAINT `FK6ipbom6lji60h38bd3ok2r098` FOREIGN KEY (`study_user_id`) REFERENCES `study_user` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `template_dataset`
--

DROP TABLE IF EXISTS `template_dataset`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `template_dataset` (
  `template_dataset_nature` int(11) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKi2u82rhpqh1nr05lnb14mpkij` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2019-09-05 17:18:59
