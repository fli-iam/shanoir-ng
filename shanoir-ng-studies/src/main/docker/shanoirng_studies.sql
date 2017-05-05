use shanoir_ng_studies;

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
) ENGINE=InnoDB AUTO_INCREMENT=52 DEFAULT CHARSET=utf8;

INSERT INTO center(id, country, name, phone_number, postal_code, street, city, website)
VALUES
(1,'France','CHU Rennes','','','','Rennes',''),(2,'France','CHU Reims','','','','Reims',''),(3,'France','LPS - CENIR','0157274007','75013','Bâtiment ICM (niv -1) , 47 Bd de l''Hôpital','Paris','www.cenir.org'),(4,'France','CHU Marseille','','','','Marseille',''),(5,'France','HCL - NeuroCardio','','','','Lyon',''),(7,'France','Hôpitaux Universitaires de Strasbourg','','','','Strasbourg',''),(8,'France','Centre Hospitalier Yves Le Foll','','','','Saint Brieuc',''),(9,'France','CHU Nîmes','','','','Nîmes',''),(10,'France','CHU Toulouse','','','','Toulouse',''),(11,'France','CHU Nantes','','','','Nantes',''),(12,'France','CHU Brabois','','','','Nancy',''),(13,'France','CHU de Fort-de-France','','','','Fort-de-France',''),(14,'France','CHU Michallon','','38700','Site Santé','Grenoble',''),(15,'France','GIN','','38706','','Grenoble','http://neurosciences.ujf-grenoble.fr/main-home-1-sub-home-0-lang-en.html'),(16,'France','CHU Brest','','','','Brest',''),(17,'France','CHGR','','','','Rennes',''),(18,'France','HCL - NeuroCardio to delete','','','','Lyon',''),(19,'France','HCL - Hopital Edouard Herriot','','','','Lyon',''),(20,'France','HCL - CHLS RMN','','','','Lyon',''),(21,'France','HCL - NeuroCardio HFME','','','','Lyon',''),(22,'France','HCL - GIE Lyon Nord','','','','Lyon',''),(23,'France','INSERM_825','','','','Toulouse',''),(24,'Belgique','UCL','','','','Bruxelles',''),(25,'France','CH Bretagne Atlantique Vannes','','','','Vannes',''),(27,'France','ICM','','','','Paris',''),(28,'France','CHU Bordeaux','','','','Bordeaux',''),(29,'France','HCL - CHLS','','','','Lyon',''),(30,'France','CHRU Clermont-Ferrand Gabriel Montpied','','','','Clermont-Ferrand',''),(31,'France','CHRU Montpellier Gui de Chauliac','','','','Montpellier',''),(33,'France','IRM des sources','','','','Lyon',''),(35,'France','IPB Strasbourg','','','','Strasbourg',''),(36,'France','Hôpital de la Timone - Marseille','','13005','264 rue St Pierre','Marseille','http://fr.ap-hm.fr/nos-hopitaux/hopital-de-la-timone'),(37,'France','HCL - Hôpital Pierre Wertheimer','','69677','59 bvd Pinel','Lyon','http://www.chu-lyon.fr/web/Hopital_Pierre-w_2346.html'),(38,'France','CHU - Charles-Nicolle- Rouen','','76031','1 rue de Germont','Rouen','http://www3.chu-rouen.fr/internet/connaitreCHU/reperes/soins/charles_nicolle/'),(39,'France','CHU - Dijon','','21079','14 rue Gaffarel','Dijon','http://www.chu-dijon.fr/'),(40,'France','CHU - Hôpital Central - Nancy','','54035','29 avenue du Maréchal de Lattre de Tassigny','Nancy','http://www.chu-nancy.fr/'),(41,'France','CHRU - Lille','','59037','2 avenue Oscar Lambret','Lille','http://www.chru-lille.fr/'),(43,'France','Hôpital Beaujon -Paris','','92110','100 boulevard du général Leclerc','Clichy','http://www.aphp.fr/hopital/beaujon/'),(44,'France','Hôpital Bicêtre - Paris','','94275','78 rue du général Leclerc','Le Kremlin Bicetre',''),(45,'France','Hopital d''Instruction des Armées Sainte Anne','0483162014','83800','2 bvd Sainte Anne','Toulon','http://www.sainteanne.org/'),(46,'France','CHU St-Etienne','0477829226','42277','Hôpital Nord- avenue albert raimond','St Priest en Jarez - St Etienne','http://www.chu-st-etienne.fr/'),(47,'France','CH Annecy Genevois','','74374','1avenue de l''hôpital','Metz - Tessy','http://www.ch-annecygenevois.fr/fr'),(48,'France','Hopital Nord - Marseille','0491380000','13915','Chemin des Bourrely','Marseille','http://fr.ap-hm.fr/nos-hopitaux/hopital-nord'),(49,'France','Hopital Pitié Salpétrière','0157274007','75013','47-83 bd de l’hôpital','Paris',''),(50,'France','CHU St Roch - Nice','','06006','5 rue Pierre Devoluy','Nice',''),(51,'France','CHU Poitiers','','86000','rue de la miletrie','Poitiers','');

CREATE TABLE `study_status` (
  `id` bigint(20) NOT NULL,
  `label_name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_l7kodam6g756cg07p4yw0wog4` (`label_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `study` (
  `id` bigint(20) NOT NULL,
  `clinical` bit(1) NOT NULL,
  `end_date` date DEFAULT NULL,
  `is_downloadable_by_default` bit(1) NOT NULL,
  `is_visible_by_default` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `start_date` date DEFAULT NULL,
  `with_examination` bit(1) NOT NULL,
  `study_status_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_q5qxcb7ermaxmp5f2wx2rj28n` (`name`),
  KEY `FK602whnuqnamogethyglafvqjx` (`study_status_id`),
  CONSTRAINT `FK602whnuqnamogethyglafvqjx` FOREIGN KEY (`study_status_id`) REFERENCES `study_status` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO study_status
	(id, label_name)
VALUES
	(1,'finished'),
	(2,'in_progress');

INSERT INTO study
	(id, name, start_date, end_date, clinical, with_examination, is_visible_by_default, is_downloadable_by_default, study_status_id)
VALUES
	(1,'Study 1', '2013/01/01', null, 0, 1, 0, 0, null),
	(2,'Study 2', '2009/12/01', null, 0, 1, 0, 0, null),
	(3,'Study 3', '2010/02/21', null, 0, 1, 0, 0, null),
	(4,'Study 4', '2015/10/03', null, 0, 1, 0, 0, null);
	
CREATE TABLE `pseudonymus_hash_values` (
  `id` bigint(20) NOT NULL,
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
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
CREATE TABLE `subject_type` (
  `id` bigint(20) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
	
	CREATE TABLE `subject` (
  `id` bigint(20) NOT NULL,
  `birth_date` datetime DEFAULT NULL,
  `identifier` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `sex` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKtduy53un9gt43fbb3fx6nokfn` (`sex`),
  CONSTRAINT `FKtduy53un9gt43fbb3fx6nokfn` FOREIGN KEY (`sex`) REFERENCES `sex` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `subject` VALUES (1,'2013-01-01 00:00:00','sub1','subject1',NULL),(2,'2001-02-01 00:00:00','sub2','subject2',NULL);

CREATE TABLE `subject_study` (
  `id` bigint(20) NOT NULL,
  `physically_involved` bit(1) NOT NULL,
  `subject_study_identifier` varchar(255) DEFAULT NULL,
  `study` bigint(20) DEFAULT NULL,
  `subject` bigint(20) DEFAULT NULL,
  `type` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKelblgk1bc448k1jrlaq0lue5d` (`study`),
  KEY `FKft8kki1chfo5h4q8l64slqb4c` (`subject`),
  KEY `FKh9recsf0nwxjoxx3qfjoudtom` (`type`),
  CONSTRAINT `FKelblgk1bc448k1jrlaq0lue5d` FOREIGN KEY (`study`) REFERENCES `study` (`id`),
  CONSTRAINT `FKft8kki1chfo5h4q8l64slqb4c` FOREIGN KEY (`subject`) REFERENCES `subject` (`id`),
  CONSTRAINT `FKh9recsf0nwxjoxx3qfjoudtom` FOREIGN KEY (`type`) REFERENCES `subject_type` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `subject_study` VALUES (1,'\0',NULL,1,1,NULL),(2,'\0',NULL,1,2,NULL),(3,'\0',NULL,2,1,NULL);

CREATE TABLE `acquisition_equipment` (
  `id` bigint(20) NOT NULL,
  `serial_number` varchar(255) DEFAULT NULL,
  `center_id` bigint(20) NOT NULL,
  `manufacturer_model_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `model_number_idx` (`manufacturer_model_id`,`serial_number`),
  KEY `FKnfu3vqdsoj1y339uq7alaltjv` (`center_id`),
  CONSTRAINT `FKbvbig13gxsu8gxaw9h6uemhk4` FOREIGN KEY (`manufacturer_model_id`) REFERENCES `manufacturer_model` (`id`),
  CONSTRAINT `FKnfu3vqdsoj1y339uq7alaltjv` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `acquisition_equipment` VALUES (1,'123456789',1,1),(2,'234567891',2,1),(3,'345678912',1,2);

CREATE TABLE `manufacturer` (
  `id` bigint(20) NOT NULL,
  `name` varchar(200) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_fvhf6l0xkf8hnay7lvwimnwu1` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `manufacturer` VALUES (1,'GE Healthcare'),(2,'GE Medical Systems'),(3,'Philips Healthcare');

CREATE TABLE `manufacturer_model` (
  `dataset_modality_type` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL,
  `name` varchar(200) NOT NULL,
  `magnetic_field` double DEFAULT NULL,
  `manufacturer_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_i9hga82t5tie3gsf9pvkxkb0m` (`name`),
  KEY `FKdaf3kbh73wqquhyggxyhaadq7` (`manufacturer_id`),
  CONSTRAINT `FKdaf3kbh73wqquhyggxyhaadq7` FOREIGN KEY (`manufacturer_id`) REFERENCES `manufacturer` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO `manufacturer_model` VALUES ('MR_DATASET',1,'DISCOVERY MR750',3,2),('PET_DATASET',2,'DISCOVERY MR750w',NULL,2),('MR_DATASET',3,'Ingenia',1.5,3);



