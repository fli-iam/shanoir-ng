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

ALTER TABLE dataset_processing ADD COLUMN monitoring_index INT(3);
ALTER TABLE dataset_processing ADD COLUMN processing_status TINYINT(1);
UPDATE dataset_processing SET monitoring_index = 1 WHERE parent_id IS NULL;
UPDATE dataset_processing dp
    JOIN execution_monitoring em ON em.id = dp.parent_id
    SET dp.processing_status = em.status
WHERE em.status IS NOT NULL;