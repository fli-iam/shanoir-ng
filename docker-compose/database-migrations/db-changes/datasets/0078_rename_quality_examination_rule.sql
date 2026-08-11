-- Shanoir NG - Import, manage and share neuroimaging data
-- Copyright (C) 2009-2026 Inria - https://www.inria.fr/
-- Contact us on https://project.inria.fr/shanoir/
-- 
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
-- 
-- You should have received a copy of the GNU General Public License
-- along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

-- rename quality_examination_rule table to fit the new quality card concept
RENAME TABLE IF EXISTS quality_examination_rule TO quality_card_rule;

ALTER TABLE quality_card_condition_join 
DROP FOREIGN KEY IF EXISTS FKahmgub56cris7hca5ya5rj8m6;

ALTER TABLE quality_card_condition_join
ADD CONSTRAINT FKahmgub56cris7hca5ya5rj8m6
FOREIGN KEY (quality_card_rule_id)
REFERENCES quality_card_rule(id);