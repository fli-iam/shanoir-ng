-- the rule_id column has been replaced by a join table and all associations has already been copied in it
-- see docker-compose/database/db-changes/datasets/0023_quality_control_modify_study_cards.sql
alter table study_card_condition drop foreign key FKpi8v33fmd0wn64e06q8it8pkv;
alter table study_card_condition drop column rule_id;

-- squashed
DELETE from datasets.acquisition_equipment;

INSERT INTO datasets.acquisition_equipment (id, name)
SELECT ae.id AS id, CONCAT(sm.name, ' - ', smm.name, IF(smm.magnetic_field IS NULL, '', CONCAT( ' (', smm.magnetic_field, 'T', ') ')), ae.serial_number, ' ', c.name)
    AS name FROM studies.acquisition_equipment ae, studies.manufacturer_model smm, studies.manufacturer sm, studies.center c
    WHERE ae.manufacturer_model_id = smm.id AND smm.manufacturer_id = sm.id AND ae.center_id = c.id;