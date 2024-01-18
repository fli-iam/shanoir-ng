-- in study_card_condition_join, delete references of rules that have no references in any assignment
-- (having no assignment make a study_card_rule useless)
-- (quality cards have another join table : quality_card_condition_join)
delete from study_card_condition_join where study_card_rule_id in (select id from study_card_rule where id not in (select rule_id from study_card_assignment));

-- delete rules that have no references in any assignment
delete from study_card_rule where id not in (select rule_id from study_card_assignment);

-- change modality type MR to MR_DATASET as it is the standard in Shanoir (see DatasetModalityType.java) (1 is defined in DatasetMetadataField.java)
update study_card_assignment set value = "MR_DATASET" where field = 1 and value like "MR";

-- fix the case for spin density
update study_card_assignment set value = "SPIN_DENSITY" where field = 7 and lower(value) like "spin_density" and value not like binary "SPIN_DENSITY";