ALTER TABLE `preclinical`.`animal_subject` DISABLE KEYS;

UPDATE `preclinical`.`animal_subject` SET id = subject_id

ALTER TABLE `preclinical`.`animal_subject` ENABLE KEYS;

ALTER TABLE `preclinical`.`animal_subject`
DROP COLUMN `subject_id`;
