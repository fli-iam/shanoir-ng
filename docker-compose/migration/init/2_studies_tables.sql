-- MySQL dump 10.13  Distrib 8.0.17, for Win64 (x86_64)
--
-- Host: localhost    Database: studies
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
-- Table structure for table `acquisition_equipment`
--

DROP TABLE IF EXISTS `acquisition_equipment`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `acquisition_equipment` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `serial_number` varchar(255) DEFAULT NULL,
  `center_id` bigint(20) NOT NULL,
  `manufacturer_model_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `model_number_idx` (`manufacturer_model_id`,`serial_number`),
  KEY `FKnfu3vqdsoj1y339uq7alaltjv` (`center_id`),
  CONSTRAINT `FKbvbig13gxsu8gxaw9h6uemhk4` FOREIGN KEY (`manufacturer_model_id`) REFERENCES `manufacturer_model` (`id`),
  CONSTRAINT `FKnfu3vqdsoj1y339uq7alaltjv` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `center`
--

DROP TABLE IF EXISTS `center`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `center` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `city` varchar(255) DEFAULT NULL,
  `country` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `phone_number` varchar(255) DEFAULT NULL,
  `postal_code` varchar(255) DEFAULT NULL,
  `street` varchar(255) DEFAULT NULL,
  `website` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_9ntt4q0n3w4lywq1k9xveiyo8` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `coil`
--

DROP TABLE IF EXISTS `coil`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `coil` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `coil_type` int(11) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `number_of_channels` bigint(20) DEFAULT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `center_id` bigint(20) NOT NULL,
  `manufacturer_model_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK2c4pj1yt8xsha1dn6r8p1bhtu` (`center_id`),
  KEY `FKhkauya7hqlh56r9fd9c9mgh39` (`manufacturer_model_id`),
  CONSTRAINT `FK2c4pj1yt8xsha1dn6r8p1bhtu` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`),
  CONSTRAINT `FKhkauya7hqlh56r9fd9c9mgh39` FOREIGN KEY (`manufacturer_model_id`) REFERENCES `manufacturer_model` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `group_of_subjects`
--

DROP TABLE IF EXISTS `group_of_subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `group_of_subjects` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_name` varchar(255) NOT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK6fmx594ux0memkouiu5ygirjo` (`study_id`),
  CONSTRAINT `FK6fmx594ux0memkouiu5ygirjo` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `manufacturer`
--

DROP TABLE IF EXISTS `manufacturer`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `manufacturer` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_fvhf6l0xkf8hnay7lvwimnwu1` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `manufacturer_model`
--

DROP TABLE IF EXISTS `manufacturer_model`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `manufacturer_model` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dataset_modality_type` int(11) NOT NULL,
  `magnetic_field` double DEFAULT NULL,
  `name` varchar(200) NOT NULL,
  `manufacturer_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdaf3kbh73wqquhyggxyhaadq7` (`manufacturer_id`),
  CONSTRAINT `FKdaf3kbh73wqquhyggxyhaadq7` FOREIGN KEY (`manufacturer_id`) REFERENCES `manufacturer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `protocol_file_path`
--

DROP TABLE IF EXISTS `protocol_file_path`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `protocol_file_path` (
  `study_id` bigint(20) NOT NULL,
  `path` varchar(255) DEFAULT NULL,
  KEY `FKfl6gbp6934yyeur6sb94w6x94` (`study_id`),
  CONSTRAINT `FKfl6gbp6934yyeur6sb94w6x94` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `pseudonymus_hash_values`
--

DROP TABLE IF EXISTS `pseudonymus_hash_values`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `pseudonymus_hash_values` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `birth_date_hash` varchar(255) DEFAULT NULL,
  `birth_name_hash1` varchar(255) DEFAULT NULL,
  `birth_name_hash2` varchar(255) DEFAULT NULL,
  `birth_name_hash3` varchar(255) DEFAULT NULL,
  `first_name_hash1` varchar(255) DEFAULT NULL,
  `first_name_hash2` varchar(255) DEFAULT NULL,
  `first_name_hash3` varchar(255) DEFAULT NULL,
  `last_name_hash1` varchar(255) DEFAULT NULL,
  `last_name_hash2` varchar(255) DEFAULT NULL,
  `last_name_hash3` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study`
--

DROP TABLE IF EXISTS `study`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `study` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `clinical` bit(1) NOT NULL,
  `coordinator_id` bigint(20) DEFAULT NULL,
  `downloadable_by_default` bit(1) NOT NULL,
  `end_date` date DEFAULT NULL,
  `mono_center` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `start_date` date DEFAULT NULL,
  `study_status` int(11) NOT NULL,
  `study_type` int(11) DEFAULT NULL,
  `visible_by_default` bit(1) NOT NULL,
  `with_examination` bit(1) NOT NULL,
  `challenge` bit(1) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_q5qxcb7ermaxmp5f2wx2rj28n` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=179 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_center`
--

DROP TABLE IF EXISTS `study_center`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `study_center` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `center_id` bigint(20) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKi5ioon66o30h52tyxdqubfpdp` (`center_id`),
  KEY `FK2hmmh3c0w1tk8npi76hpvf09i` (`study_id`),
  CONSTRAINT `FK2hmmh3c0w1tk8npi76hpvf09i` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`),
  CONSTRAINT `FKi5ioon66o30h52tyxdqubfpdp` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=367 DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_examination`
--

DROP TABLE IF EXISTS `study_examination`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `study_examination` (
  `study_id` bigint(20) NOT NULL,
  `examination_id` bigint(20) DEFAULT NULL,
  KEY `FKlbokvx0u8921ujhfyh1751ssl` (`study_id`),
  CONSTRAINT `FKlbokvx0u8921ujhfyh1751ssl` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `study_user`
--

DROP TABLE IF EXISTS `study_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `study_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `receive_anonymization_report` bit(1) NOT NULL,
  `receive_new_import_report` bit(1) NOT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `user_name` varchar(255) NOT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `study_user_idx` (`study_id`,`user_id`),
  CONSTRAINT `FKc4ftmuoc0u0ghw43dxth2m8we` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
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
-- Table structure for table `subject`
--

DROP TABLE IF EXISTS `subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subject` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `birth_date` date DEFAULT NULL,
  `identifier` varchar(255) DEFAULT NULL,
  `imaged_object_category` int(11) DEFAULT NULL,
  `language_hemispheric_dominance` int(11) DEFAULT NULL,
  `manual_hemispheric_dominance` int(11) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `preclinical` bit(1) NOT NULL DEFAULT b'0',
  `sex` int(11) DEFAULT NULL,
  `pseudonymus_hash_values_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKq6wqw3ly73aat738iyo667yd4` (`pseudonymus_hash_values_id`),
  CONSTRAINT `FKq6wqw3ly73aat738iyo667yd4` FOREIGN KEY (`pseudonymus_hash_values_id`) REFERENCES `pseudonymus_hash_values` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subject_group_of_subjects`
--

DROP TABLE IF EXISTS `subject_group_of_subjects`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subject_group_of_subjects` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `group_of_subjects_id` bigint(20) DEFAULT NULL,
  `subject_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKdw1w3b6n98k0obbnyif6hnyb7` (`group_of_subjects_id`),
  KEY `FKbuuj437rygsa1u24tfbwicxnh` (`subject_id`),
  CONSTRAINT `FKbuuj437rygsa1u24tfbwicxnh` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`),
  CONSTRAINT `FKdw1w3b6n98k0obbnyif6hnyb7` FOREIGN KEY (`group_of_subjects_id`) REFERENCES `group_of_subjects` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `subject_study`
--

DROP TABLE IF EXISTS `subject_study`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `subject_study` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `physically_involved` bit(1) NOT NULL,
  `subject_study_identifier` varchar(255) DEFAULT NULL,
  `subject_type` int(11) DEFAULT NULL,
  `study_id` bigint(20) NOT NULL,
  `subject_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `study_subject_idx` (`study_id`,`subject_id`),
  KEY `FK6iuhtwq9ujtyfywgjfct2m0jf` (`subject_id`),
  CONSTRAINT `FK6iuhtwq9ujtyfywgjfct2m0jf` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`),
  CONSTRAINT `FKr4fo2f7o9ggrr6b06qmq6h373` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `timepoint`
--

DROP TABLE IF EXISTS `timepoint`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `timepoint` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment` varchar(255) DEFAULT NULL,
  `days` bigint(20) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  `rank` bigint(20) NOT NULL,
  `study_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKo2x3e9dhemdt9alu8okyx4eq8` (`study_id`),
  CONSTRAINT `FKo2x3e9dhemdt9alu8okyx4eq8` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=latin1;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Table structure for table `user_personal_comment_subject`
--

DROP TABLE IF EXISTS `user_personal_comment_subject`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_personal_comment_subject` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment` varchar(255) DEFAULT NULL,
  `subject_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKsv5esm96mpp66iei2ei32jado` (`subject_id`),
  CONSTRAINT `FKsv5esm96mpp66iei2ei32jado` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`)
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

-- Dump completed on 2019-09-05 17:20:38
