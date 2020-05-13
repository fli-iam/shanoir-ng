use shanoir_ng_preclinical;

CREATE TABLE `reference` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `category` varchar(255) DEFAULT NULL,
  `reftype` varchar(255) DEFAULT NULL,
  `value` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=23 DEFAULT CHARSET=latin1;

INSERT INTO `reference`
VALUES 
	(1,'subject','specie','rat'),
	(2,'subject','specie','mouse'),
	(3,'subject','biotype','wild'),
	(4,'subject','biotype','transgenic'),
	(6,'subject','strain','wistar'),
	(7,'subject','provider','Charles River'),
	(8,'subject','stabulation','grenoble'),
	(9,'subject','strain','xx98'),
	(10,'subject','provider','Janvier'),
	(11,'anatomy','location','Brain'),
	(12,'anatomy','location','Heart'),
	(13,'unit','concentration','%'),
	(14,'unit','concentration','mg/ml'),
	(15,'unit','volume','ml'),
	(16,'contrastagent','name','Gadolinium'),
	(17,'contrastagent','name','Uspio'),
	(18,'anesthetic','ingredient','Isoflurane'),
	(19,'anesthetic','ingredient','Ketamine'),
	(20,'anesthetic','ingredient','Xylamine'),
	(21,'subject','specie','Monkey'),
	(22,'contrastagent','name','fuorane');


CREATE TABLE `anesthetic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `anesthetic_type` varchar(255) DEFAULT NULL,
  `comment` varchar(255) DEFAULT NULL,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=3 DEFAULT CHARSET=latin1;

CREATE TABLE `anesthetic_ingredient` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `concentration` double DEFAULT NULL,
  `anesthetic_id` bigint(20) NOT NULL,
  `concentration_unit_id` bigint(20) DEFAULT NULL,
  `name_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK4v267n4qjaebofv6h8gsjbo3e` (`anesthetic_id`),
  KEY `FKbcj5vnnysqalx5k2vheah1xel` (`concentration_unit_id`),
  KEY `FKmhjc6mojrfuiaycgir7mm2lj1` (`name_id`),
  CONSTRAINT `FK4v267n4qjaebofv6h8gsjbo3e` FOREIGN KEY (`anesthetic_id`) REFERENCES `anesthetic` (`id`),
  CONSTRAINT `FKbcj5vnnysqalx5k2vheah1xel` FOREIGN KEY (`concentration_unit_id`) REFERENCES `reference` (`id`),
  CONSTRAINT `FKmhjc6mojrfuiaycgir7mm2lj1` FOREIGN KEY (`name_id`) REFERENCES `reference` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=latin1;

CREATE TABLE `animal_subject` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `subject_id` bigint(20) NOT NULL,
  `biotype_id` bigint(20) NOT NULL,
  `provider_id` bigint(20) NOT NULL,
  `specie_id` bigint(20) NOT NULL,
  `stabulation_id` bigint(20) NOT NULL,
  `strain_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKa29uyd7uqqyh4nckvqw64i97n` (`biotype_id`),
  KEY `FKba896d5p63casrb2bt1np3gip` (`provider_id`),
  KEY `FKkstwtmssgrhjm80pmucpdtp76` (`specie_id`),
  KEY `FKkycpjmd7jvtunt9n6h3r05cef` (`stabulation_id`),
  KEY `FKt4bw2g4p7p73r3krf3mss6o74` (`strain_id`),
  CONSTRAINT `FKa29uyd7uqqyh4nckvqw64i97n` FOREIGN KEY (`biotype_id`) REFERENCES `reference` (`id`),
  CONSTRAINT `FKba896d5p63casrb2bt1np3gip` FOREIGN KEY (`provider_id`) REFERENCES `reference` (`id`),
  CONSTRAINT `FKkstwtmssgrhjm80pmucpdtp76` FOREIGN KEY (`specie_id`) REFERENCES `reference` (`id`),
  CONSTRAINT `FKkycpjmd7jvtunt9n6h3r05cef` FOREIGN KEY (`stabulation_id`) REFERENCES `reference` (`id`),
  CONSTRAINT `FKt4bw2g4p7p73r3krf3mss6o74` FOREIGN KEY (`strain_id`) REFERENCES `reference` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=latin1;

CREATE TABLE `contrast_agent` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `concentration` double DEFAULT NULL,
  `dose` double DEFAULT NULL,
  `injection_interval` varchar(255) DEFAULT NULL,
  `injection_site` varchar(255) DEFAULT NULL,
  `injection_type` varchar(255) DEFAULT NULL,
  `manufactured_name` varchar(255) DEFAULT NULL,
  `concentration_unit_id` bigint(20) DEFAULT NULL,
  `dose_unit_id` bigint(20) DEFAULT NULL,
  `name_id` bigint(20) DEFAULT NULL,
  `protocol_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKnyuip6ebfraj9y3gqu96h0d9h` (`concentration_unit_id`),
  KEY `FKqfixjs64n2r61c8k28mus6fa3` (`dose_unit_id`),
  KEY `FK7on8j3fka78wg3ar56d6h7rmr` (`name_id`),
  CONSTRAINT `FK7on8j3fka78wg3ar56d6h7rmr` FOREIGN KEY (`name_id`) REFERENCES `reference` (`id`),
  CONSTRAINT `FKnyuip6ebfraj9y3gqu96h0d9h` FOREIGN KEY (`concentration_unit_id`) REFERENCES `reference` (`id`),
  CONSTRAINT `FKqfixjs64n2r61c8k28mus6fa3` FOREIGN KEY (`dose_unit_id`) REFERENCES `reference` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=46 DEFAULT CHARSET=latin1;

CREATE TABLE `examination_anesthetic` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dose` double DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `injection_interval` varchar(255) DEFAULT NULL,
  `injection_site` varchar(255) DEFAULT NULL,
  `injection_type` varchar(255) DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `anesthetic_id` bigint(20) NOT NULL,
  `dose_unit_id` bigint(20) DEFAULT NULL,
  `examination_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKfryx1bp4q88mw3n2xl33v5trk` (`anesthetic_id`),
  KEY `FKc7negpo309oya9atbd4eaob0t` (`dose_unit_id`),
  CONSTRAINT `FKc7negpo309oya9atbd4eaob0t` FOREIGN KEY (`dose_unit_id`) REFERENCES `reference` (`id`),
  CONSTRAINT `FKfryx1bp4q88mw3n2xl33v5trk` FOREIGN KEY (`anesthetic_id`) REFERENCES `anesthetic` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=10 DEFAULT CHARSET=latin1;


CREATE TABLE `examination_extradata` (
  `dtype` varchar(31) NOT NULL,
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `examination_id` bigint(20) NOT NULL,
  `extradatatype` varchar(255) NOT NULL,
  `filename` varchar(255) DEFAULT NULL,
  `filepath` varchar(255) DEFAULT NULL,
  `has_heart_rate` bit(1) DEFAULT NULL,
  `has_respiratory_rate` bit(1) DEFAULT NULL,
  `has_sao2` bit(1) DEFAULT NULL,
  `has_temperature` bit(1) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=36 DEFAULT CHARSET=latin1;

CREATE TABLE `pathology` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

INSERT INTO `pathology` VALUES (1,'Alzheimer'),(2,'Bone'),(3,'Cancer'),(4,'Stroke');

CREATE TABLE `pathology_model` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `pathology_id` bigint(20) NOT NULL,
  `filename` varchar(255) DEFAULT NULL,
  `filepath` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FK1fj6p1qfx1hpkc92eedqf3db3` (`pathology_id`),
  CONSTRAINT `FK1fj6p1qfx1hpkc92eedqf3db3` FOREIGN KEY (`pathology_id`) REFERENCES `pathology` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;


CREATE TABLE `therapy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `comment` varchar(255) DEFAULT NULL,
  `name` varchar(255) DEFAULT NULL,
  `therapy_type` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=latin1;



CREATE TABLE `subject_pathology` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `end_date` datetime DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `location_id` bigint(20) NOT NULL,
  `pathology_id` bigint(20) NOT NULL,
  `pathology_model_id` bigint(20) NOT NULL,
  `animal_subject_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FK9e06p429yvsxhn79e8gjv9www` (`location_id`),
  KEY `FKfqrfjqqjrgvhqexnewqpcm0c3` (`pathology_id`),
  KEY `FKio39cpt2y16oovfuhej7g5xsf` (`pathology_model_id`),
  KEY `FKpccpmanqqjnmfnu749tynx90m` (`animal_subject_id`),
  CONSTRAINT `FK9e06p429yvsxhn79e8gjv9www` FOREIGN KEY (`location_id`) REFERENCES `reference` (`id`),
  CONSTRAINT `FKfqrfjqqjrgvhqexnewqpcm0c3` FOREIGN KEY (`pathology_id`) REFERENCES `pathology` (`id`),
  CONSTRAINT `FKio39cpt2y16oovfuhej7g5xsf` FOREIGN KEY (`pathology_model_id`) REFERENCES `pathology_model` (`id`),
  CONSTRAINT `FKpccpmanqqjnmfnu749tynx90m` FOREIGN KEY (`animal_subject_id`) REFERENCES `animal_subject` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=8 DEFAULT CHARSET=latin1;

CREATE TABLE `subject_therapy` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `dose` double DEFAULT NULL,
  `end_date` datetime DEFAULT NULL,
  `frequency` varchar(255) DEFAULT NULL,
  `start_date` datetime DEFAULT NULL,
  `dose_unit_id` bigint(20) DEFAULT NULL,
  `animal_subject_id` bigint(20) NOT NULL,
  `therapy_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  KEY `FKap9ciqq1jocgjbbigo7imbgko` (`dose_unit_id`),
  KEY `FKcp0ppo5a1ifkob6ulk7mt57lf` (`animal_subject_id`),
  KEY `FK1ysgkdrdqhu5auyrqom59tcre` (`therapy_id`),
  CONSTRAINT `FK1ysgkdrdqhu5auyrqom59tcre` FOREIGN KEY (`therapy_id`) REFERENCES `therapy` (`id`),
  CONSTRAINT `FKap9ciqq1jocgjbbigo7imbgko` FOREIGN KEY (`dose_unit_id`) REFERENCES `reference` (`id`),
  CONSTRAINT `FKcp0ppo5a1ifkob6ulk7mt57lf` FOREIGN KEY (`animal_subject_id`) REFERENCES `animal_subject` (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=latin1;

