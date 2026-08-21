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

UPDATE dataset_metadata mtd SET mtd.name = mtd.comment
WHERE mtd.id IN (
SELECT ds.updated_metadata_id FROM dataset ds 
INNER JOIN dataset_acquisition acq ON ds.dataset_acquisition_id = acq.id
INNER JOIN examination ex ON acq.examination_id = ex.id AND ex.study_id = 66
INNER JOIN dataset_property prop ON ds.id = prop.dataset_id AND prop.name = 'volume.name');
