ALTER TABLE study ADD COLUMN study_card_policy tinyint(4) DEFAULT NULL;
UPDATE study SET study_card_policy=0;