CREATE TABLE `profile` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `profile_name` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
)

ALTER TABLE study ADD profile_id bigint(20);

INSERT INTO profile (id, profile_name) VALUES (1,'Profile Neurinfo');
INSERT INTO profile (id, profile_name) VALUES (2,'Profile OFSEP');