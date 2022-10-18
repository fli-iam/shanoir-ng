-- insert type into study_card_rule
ALTER TABLE study_card_rule ADD COLUMN type int NULL;

-- create new table for values, copy values afterwards to new table and delete old column
CREATE TABLE study_card_condition_value (
  id bigint(20) NOT NULL,
  condition_id bigint(20) NOT NULL,
  value varchar(255) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

ALTER TABLE study_card_condition_value ADD CONSTRAINT FKnmg0gxqrew2nqktd0mm5hvi64 FOREIGN KEY (condition_id) REFERENCES study_card_condition(id);

INSERT INTO study_card_condition_value (id, condition_id, value) select id, id, dicom_value FROM study_card_condition;

ALTER TABLE study_card_condition CHANGE dicom_tag dicom_tag_or_field int(11) NOT NULL;

ALTER TABLE study_card_condition DROP value;