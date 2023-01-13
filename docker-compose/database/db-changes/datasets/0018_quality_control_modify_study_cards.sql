-- create quality_card
CREATE TABLE quality_card (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  name varchar(255) NOT NULL,
  study_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id),
  UNIQUE KEY UK_poown2bfypi2y14oa0ve4k22s (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- create quality_examination_rule
CREATE TABLE quality_examination_rule (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  tag int(11) DEFAULT NULL,
  quality_card_id bigint(20) DEFAULT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE quality_examination_rule ADD CONSTRAINT FK8bavfrsgqwil7aei15l00dg6g FOREIGN KEY (quality_card_id) REFERENCES quality_card (id);

-- study_card_condition
ALTER TABLE study_card_condition MODIFY dicom_tag int(11) NULL;
ALTER TABLE study_card_condition ADD COLUMN shanoir_field int(11) NULL;
ALTER TABLE study_card_condition ADD COLUMN scope varchar(63) NOT NULL;
ALTER TABLE study_card_condition ADD COLUMN cardinality int(11) NOT NULL;
UPDATE study_card_condition SET scope = 'StudyCardDICOMCondition';

-- study_card_assignment, add scope and set its value
ALTER TABLE study_card_assignment ADD COLUMN scope varchar(31) NOT NULL;
UPDATE study_card_assignment SET scope = 'Dataset' WHERE field IN (1,6,10,11,14);
UPDATE study_card_assignment SET scope = 'DatasetAcquisition' WHERE field IN (2,3,4,5,7,8,9,12,13,15);

-- study_card_rule, add scope and set its value
ALTER TABLE study_card_rule ADD COLUMN scope varchar(31) NOT NULL;
UPDATE study_card_rule SET scope = 'Dataset' WHERE id IN (
  SELECT rule_id FROM study_card_assignment WHERE scope LIKE 'Dataset'
);
UPDATE study_card_rule SET scope = 'DatasetAcquisition' WHERE id IN (
  SELECT rule_id FROM study_card_assignment WHERE scope LIKE 'DatasetAcquisition'
);

-- create new table for values
CREATE TABLE study_card_condition_values (
  study_card_condition_id bigint(20) NOT NULL,
  value varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE study_card_condition_values ADD CONSTRAINT FKnmg0gxqrew2nqktd0mm5hvi64 FOREIGN KEY (study_card_condition_id) REFERENCES study_card_condition(id);

-- copy values afterwards
INSERT INTO study_card_condition_values (study_card_condition_id, value) select id, dicom_value FROM study_card_condition;
-- delete old column
ALTER TABLE study_card_condition DROP dicom_value;

-- add quality_tag in subject_study
ALTER TABLE subject_study ADD COLUMN quality_tag int(11) NULL;