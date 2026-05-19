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