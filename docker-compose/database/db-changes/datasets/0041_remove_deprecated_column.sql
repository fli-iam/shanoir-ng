-- the rule_id column has been replaced by a join table and all associations has already been copied in it
-- see docker-compose/database/db-changes/datasets/0023_quality_control_modify_study_cards.sql
alter table study_card_condition drop foreign key FKpi8v33fmd0wn64e06q8it8pkv;
alter table study_card_condition drop column rule_id;