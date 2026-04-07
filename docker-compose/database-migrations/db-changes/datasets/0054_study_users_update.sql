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

SET FOREIGN_KEY_CHECKS=0;

delete from datasets.study_user_study_user_rights;
delete from datasets.study_user;

INSERT INTO datasets.study_user
       (id, receive_study_user_report, receive_new_import_report, study_id, user_id, user_name, confirmed)
SELECT id, receive_study_user_report, receive_new_import_report, study_id, user_id, user_name, confirmed
FROM studies.study_user ssu;

INSERT INTO datasets.study_user_study_user_rights
(study_user_id, study_user_rights)
SELECT study_user_id, study_user_rights
FROM studies.study_user_study_user_rights;

SET FOREIGN_KEY_CHECKS=1;
