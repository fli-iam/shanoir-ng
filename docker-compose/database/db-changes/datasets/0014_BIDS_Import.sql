CREATE TABLE `bids_dataset` (
  `bids_data_type` varchar(255) DEFAULT NULL,
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKfyr5qol686n67f24o8wpbubpg` FOREIGN KEY (`id`) REFERENCES `dataset` (`id`)
);

CREATE TABLE `bids_dataset_acquisition` (
  `id` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  CONSTRAINT `FKe78qni1n2gip32q17j6m85qn1` FOREIGN KEY (`id`) REFERENCES `dataset_acquisition` (`id`)
);

SET sql_mode="NO_AUTO_VALUE_ON_ZERO";
INSERT INTO center (id, name) VALUES (0,'Unknown');
SET sql_mode=(SELECT REPLACE(@@sql_mode,'NO_AUTO_VALUE_ON_ZERO',''));