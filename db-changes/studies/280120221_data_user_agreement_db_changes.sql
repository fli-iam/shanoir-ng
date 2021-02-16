ALTER TABLE study_user ADD COLUMN confirmed bit NOT NULL DEFAULT TRUE;

CREATE TABLE data_user_agreement_file (
  study_id bigint(20) NOT NULL,
  path varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE data_user_agreement_file ADD CONSTRAINT FKnmg0gxlptf2nqktd0jj5hvi64 FOREIGN KEY (study_id) REFERENCES study(id);

CREATE TABLE data_user_agreement (
  id bigint(20) PRIMARY KEY NOT NULL,
  timestamp_of_accepted timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  timestamp_of_new timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  user_id bigint(20) NOT NULL,
  study_id bigint(20) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE data_user_agreement ADD CONSTRAINT FKrt509nksblm8s9f7f9ehfjxd FOREIGN KEY (study_id) REFERENCES study(id);