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

ALTER TABLE `dataset` ADD COLUMN `downloadable` bit(1) NOT NULL DEFAULT 1;
UPDATE dataset SET downloadable = 1;
-- This update should not be here, it should be a one shot.
-- UPDATE dataset d SET d.downloadable = 0 WHERE d.dataset_acquisition_id in (SELECT da.id FROM dataset_acquisition da where da.creation_date > '2023-06-17' AND da.creation_date < '2023-06-28');