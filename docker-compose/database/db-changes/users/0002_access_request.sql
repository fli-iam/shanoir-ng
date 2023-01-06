ALTER TABLE account_request_info DROP COLUMN study;
ALTER TABLE account_request_info DROP COLUMN work;
ALTER TABLE account_request_info DROP COLUMN service;
ALTER TABLE account_request_info DROP COLUMN challenge;
ALTER TABLE account_request_info ADD COLUMN study_id bigint(20);
ALTER TABLE account_request_info ADD COLUMN study_name varchar(255);
ALTER TABLE account_request_info MODIFY contact VARCHAR(255);

CREATE TABLE `access_request` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `motivation` varchar(255) DEFAULT NULL,
  `status` int(11) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  `study_name` varchar(255) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKipjw4x52ro7jvnqai3lbqibwh` (`user_id`),
  CONSTRAINT `FKipjw4x52ro7jvnqai3lbqibwh` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
);
