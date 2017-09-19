use shanoir_ng_studycards;

DROP TABLE IF EXISTS `study_cards`;

CREATE TABLE `study_cards` (
  `id` bigint(20) NOT NULL,
  `acquisition_equipment_id` bigint(20) DEFAULT NULL,
  `center_id` bigint(20) NOT NULL,
  `disabled` bit(1) NOT NULL,
  `name` varchar(255) NOT NULL,
  `nifti_converter_id` bigint(20) NOT NULL,
  `study_id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UK_innrdxbwjv64hhx7o58oxaj58` (`name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
