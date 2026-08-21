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

-- the rule_id column has been replaced by a join table and all associations has already been copied in it
-- see docker-compose/database/db-changes/datasets/0023_quality_control_modify_study_cards.sql
alter table study_card_condition drop foreign key FKpi8v33fmd0wn64e06q8it8pkv;
alter table study_card_condition drop column rule_id;