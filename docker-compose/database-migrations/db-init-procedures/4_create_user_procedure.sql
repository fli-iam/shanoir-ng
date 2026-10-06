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
USE users;


DROP PROCEDURE IF EXISTS getUserStatistics;

DELIMITER //
CREATE PROCEDURE getUserStatistics() BEGIN
SELECT 'user_id',
       'username',
       'email',
       'first_name',
       'last_name',
       'creation_date',
       'expiration_date',
       'last_login',
       'role',
       'study_name',
       'study_user_right'

UNION ALL
SELECT u.id AS user_id,
       u.username AS username,
       u.email AS email,
       u.first_name AS first_name,
       u.last_name AS last_name,
       u.creation_date AS creation_date,
       u.expiration_date AS expiration_date,
       u.last_login AS last_login,
       (CASE u.role_id
            WHEN 1 then 'Administrator'
            WHEN 2 then 'Expert'
            WHEN 3 then 'User'
        END) AS role,
       s.name AS study_name,
       (CASE sur.study_user_rights
            WHEN 1 then 'CAN_ADMINISTRATE'
            WHEN 2 then 'CAN_IMPORT'
            WHEN 3 then 'CAN_DOWNLOAD'
            WHEN 4 then 'CAN_SEE_ALL'
            WHEN 5 then 'CAN_EXECUTE'
            WHEN 6 then 'CAN_ANNOTATE'
            WHEN 7 then 'CAN_ANNOTATE_REVIEW'
        END) AS study_user_right
    FROM users u
    JOIN study_user su on su.user_id = u.id
    JOIN studies.study s on su.study_id = s.id
    JOIN study_user_study_user_rights sur on sur.study_user_id = su.id
    JOIN role as r on r.id = u.role_id;
END //
DELIMITER ;
