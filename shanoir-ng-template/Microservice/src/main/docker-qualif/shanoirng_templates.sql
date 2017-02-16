use shanoir_ng_templates;

CREATE TABLE `template` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `data` varchar(255) NOT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8;

INSERT INTO template 
VALUES 
(1,'Data1'),
(2,'Data2'),
(3,'Data3'),
(4,'Data4');
