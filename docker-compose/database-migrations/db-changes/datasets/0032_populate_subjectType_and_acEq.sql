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

ALTER TABLE shanoir_metadata ADD COLUMN acquisition_equipment_name varchar(255);
ALTER TABLE shanoir_metadata ADD COLUMN subject_type int(11);

UPDATE datasets.subject_study JOIN studies.subject_study ON datasets.subject_study.id = studies.subject_study.id SET datasets.subject_study.subject_type = studies.subject_study.subject_type;

INSERT INTO datasets.acquisition_equipment (id, name) SELECT smm.id AS id, CONCAT(sm.name, ' ', smm.name) AS name FROM studies.manufacturer_model AS smm INNER JOIN studies.manufacturer AS sm ON smm.manufacturer_id = sm.id;