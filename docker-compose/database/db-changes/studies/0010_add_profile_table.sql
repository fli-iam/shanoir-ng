CREATE TABLE `profile` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `profile_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
);

ALTER TABLE study ADD profile_id bigint(20);
ALTER TABLE study ADD CONSTRAINT `FK9o4lyhi0i6ocqf1mpd9yaeyij` FOREIGN KEY (`profile_id`) REFERENCES `profile` (`id`);

INSERT INTO profile (id, profile_name) VALUES (1,'Profile Neurinfo');
INSERT INTO profile (id, profile_name) VALUES (2,'Profile OFSEP');