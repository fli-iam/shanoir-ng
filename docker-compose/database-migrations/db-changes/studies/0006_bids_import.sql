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

SET sql_mode="NO_AUTO_VALUE_ON_ZERO";
INSERT INTO center (id, name) VALUES (0,'Unknown');
INSERT INTO manufacturer (id, name) values (0, 'Unknown');
INSERT INTO manufacturer_model (id, dataset_modality_type, name, manufacturer_id) values (0,1,'Unknown',0);
INSERT INTO acquisition_equipment (id, center_id, manufacturer_model_id) VALUES (0,0,0);
SET sql_mode=(SELECT REPLACE(@@sql_mode,'NO_AUTO_VALUE_ON_ZERO',''));