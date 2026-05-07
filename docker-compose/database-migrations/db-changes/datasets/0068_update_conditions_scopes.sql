-- Shanoir NG - Import, manage and share neuroimaging data
-- Copyright (C) 2009-2019 Inria - https://www.inria.fr/
-- Contact us on https://project.inria.fr/shanoir/
--
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
--
-- You should have received a copy of the GNU General Public License
-- along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

UPDATE study_card_condition c
JOIN study_card_condition_join j ON j.condition_id = c.id
JOIN study_card_rule r ON j.study_card_rule_id = r.id
SET c.scope = CASE
    WHEN r.scope = 'DatasetAcquisition' THEN 'AcqDICOMConditionOnDatasets'
    WHEN r.scope = 'Dataset' THEN 'DatasetDICOMConditionOnDataset'
END
WHERE c.scope = 'StudyCardDICOMConditionOnDatasets';

-- remaining ones are exam conditions
UPDATE card_condition c
SET c.scope = 'ExamDICOMConditionOnDatasets'
WHERE c.scope = 'StudyCardDICOMConditionOnDatasets';

-- RENAME TABLE study_card_condition TO card_condition;
-- 1. Drop FKs
ALTER TABLE study_card_condition_join
DROP FOREIGN KEY FK1k7n1md79nkowvqbibyn7a72k;

ALTER TABLE study_card_condition_values
DROP FOREIGN KEY FKnmg0gxqrew2nqktd0mm5hvi64;

-- 2. Rename tables
RENAME TABLE study_card_condition TO card_condition;
RENAME TABLE study_card_condition_values TO card_condition_values;

-- 3. Recreate FKs
ALTER TABLE study_card_condition_join
ADD CONSTRAINT FK1k7n1md79nkowvqbibyn7a72k
FOREIGN KEY (condition_id)
REFERENCES card_condition(id);

ALTER TABLE card_condition_values CHANGE study_card_condition_id card_condition_id BIGINT(20) NOT NULL;

ALTER TABLE card_condition_values
ADD CONSTRAINT FKnmg0gxqrew2nqktd0mm5hvi64
FOREIGN KEY (card_condition_id)
REFERENCES card_condition(id);
