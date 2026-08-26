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

alter table study_user add COLUMN expiration_date DATE;
alter table study_user add COLUMN received_expiration_notification BIT(1) NOT NULL DEFAULT FALSE;

-- Set expiration date as the shanoir expiration date except for study admins (study_user_rights = 1) who have no expiration date
UPDATE study_user su
JOIN users u ON u.id = su.user_id
SET su.expiration_date = u.expiration_date
WHERE NOT EXISTS (
    SELECT 1
    FROM study_user_study_user_rights sur
    WHERE sur.study_user_id = su.id
      AND sur.study_user_rights = 1
);

alter table account_request_info add COLUMN study_expiration_date DATE;
alter table access_request add COLUMN expiration_date DATE;
