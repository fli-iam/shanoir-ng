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

-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use import;

INSERT IGNORE INTO nifticonverter (id, name, nifti_converter_type, is_active) VALUES (1, 'dcm2nii_2008-03-31', 1, 1);
INSERT IGNORE INTO nifticonverter (id, name, nifti_converter_type, is_active) VALUES (2, 'mcverter_2.0.7', 2, 1);
INSERT IGNORE INTO nifticonverter (id, name, nifti_converter_type, is_active) VALUES (4, 'dcm2nii_2014-08-04', 1, 1);
INSERT IGNORE INTO nifticonverter (id, name, nifti_converter_type, is_active) VALUES (5, 'mcverter_2.1.0',2,1);
INSERT IGNORE INTO nifticonverter (id, name, nifti_converter_type, is_active) VALUES (6, 'dcm2niix',1,1);
INSERT IGNORE INTO nifticonverter (id, name, nifti_converter_type, is_active) VALUES (7, 'dicomifier',5,1);
