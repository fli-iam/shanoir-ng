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


-- This script sets import expiration_date values with studies.study_user expiration_date value
-- for matching user_id and study_id.

-- This fixes the cases where import expiration_date was not updated with study_user.expiration_date
-- due to a regression during ms import jackson3 migration
UPDATE import.study_user r
JOIN studies.study_user s ON s.study_id = r.study_id AND s.user_id = r.user_id
SET r.expiration_date = s.expiration_date
WHERE NOT (s.expiration_date <=> r.expiration_date);