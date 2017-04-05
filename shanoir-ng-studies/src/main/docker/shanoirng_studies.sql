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
