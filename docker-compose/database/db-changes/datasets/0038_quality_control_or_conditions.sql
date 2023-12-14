ALTER TABLE `study_card_rule`
ADD COLUMN `or_conditions` bit(1) NOT NULL DEFAULT 0;
ALTER TABLE `quality_examination_rule`
ADD COLUMN `or_conditions` bit(1) NOT NULL DEFAULT 0;
