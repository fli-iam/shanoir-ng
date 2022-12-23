-- create quality_card
CREATE TABLE `quality_card` (
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
  PRIMARY KEY (id),
  CONSTRAINT FK8bavfrsgqwil7aei15l00dg6g FOREIGN KEY (quality_card_id) REFERENCES quality_card (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- study_card_condition
ALTER TABLE study_card_condition MODIFY dicom_tag int(11) NULL;
ALTER TABLE study_card_condition ADD COLUMN shanoir_field int(11) NULL;
ALTER TABLE study_card_condition ADD COLUMN scope varchar(31) NOT NULL;
UPDATE study_card_condition SET scope = 'StudyCardDICOMCondition';

SET @dataset_field_ids = (1,6,10,11,14); -- dataset field ids
SET @acquisition_field_ids = (2,3,4,5,7,8,9,12,13,15); -- acquisition field ids

-- study_card_assignment, add scope and set its value
ALTER TABLE study_card_assignment ADD COLUMN scope varchar(31) NOT NULL;
UPDATE study_card_assignment SET scope = 'Dataset' WHERE field in @dataset_field_ids;
UPDATE study_card_assignment SET scope = 'DatasetAcquisition' WHERE field in @acquisition_field_ids;

-- study_card_rule, add scope and set its value
ALTER TABLE study_card_rule ADD COLUMN scope varchar(31) NOT NULL;
UPDATE study_card_rule SET scope = 'Dataset' WHERE id IN (
  SELECT rule_id FROM study_card_assignment WHERE scope LIKE 'Dataset'
);
UPDATE study_card_rule SET scope = 'DatasetAcquisition' WHERE id IN (
  SELECT rule_id FROM study_card_assignment WHERE scope LIKE 'DatasetAcquisition'
);

-- create new table for values
CREATE TABLE study_card_condition_value (
  id bigint(20) NOT NULL AUTO_INCREMENT,
  condition_id bigint(20) NOT NULL,
  value varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
ALTER TABLE study_card_condition_value ADD CONSTRAINT FKnmg0gxqrew2nqktd0mm5hvi64 FOREIGN KEY (condition_id) REFERENCES study_card_condition(id);
-- copy values afterwards
INSERT INTO study_card_condition_value (condition_id, value) select id, dicom_value FROM study_card_condition;
ALTER TABLE study_card_condition CHANGE dicom_tag dicom_tag_or_field int(11) NOT NULL;
-- delete old column
ALTER TABLE study_card_condition DROP dicom_value;