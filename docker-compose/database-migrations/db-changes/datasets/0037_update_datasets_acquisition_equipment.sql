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

DELETE from datasets.acquisition_equipment;

INSERT INTO datasets.acquisition_equipment (id, name)
SELECT ae.id AS id, CONCAT(sm.name, ' - ', smm.name, IF(smm.magnetic_field IS NULL, '', CONCAT( ' (', smm.magnetic_field, 'T', ') ')), ae.serial_number, ' ', c.name)
    AS name FROM studies.acquisition_equipment ae, studies.manufacturer_model smm, studies.manufacturer sm, studies.center c
    WHERE ae.manufacturer_model_id = smm.id AND smm.manufacturer_id = sm.id AND ae.center_id = c.id;