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

UPDATE users SET extension_request_demand = false WHERE extension_request_demand IS null;
UPDATE users SET first_expiration_notification_sent = false WHERE first_expiration_notification_sent IS null;
UPDATE users SET second_expiration_notification_sent = false WHERE second_expiration_notification_sent IS null;

ALTER TABLE users MODIFY extension_request_demand bit(1) NOT NULL DEFAULT b'0';
ALTER TABLE users MODIFY first_expiration_notification_sent bit(1) NOT NULL DEFAULT b'0';
ALTER TABLE users MODIFY second_expiration_notification_sent bit(1) NOT NULL DEFAULT b'0';

UPDATE users SET first_expiration_notification_sent = false, second_expiration_notification_sent = false
WHERE first_expiration_notification_sent = true
AND second_expiration_notification_sent = true
AND expiration_date > now();