use shanoir_ng_studycards;

CREATE TABLE `study_cards` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `isDisabled` bit(1) NOT NULL default 0,
  `name` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

INSERT INTO study_cards
VALUES 
	(1 ,0 ,'StudyCard1'),
	(2 ,0 ,'StudyCard2'),
	(3 ,0 ,'StudyCard3'),
	(4 ,0 ,'StudyCard4');

