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

INSERT INTO role VALUES 
	(1,'Administrator','ROLE_ADMIN');
INSERT INTO role VALUES
	(2,'User','ROLE_USER');
INSERT INTO role VALUES
	(3,'Expert','ROLE_EXPERT');

INSERT INTO users
	(id, account_request_demand, account_request_info_id, can_access_to_dicom_association, creation_date, email, expiration_date, extension_date, extension_motivation, extension_request_demand, first_name, first_expiration_notification_sent, second_expiration_notification_sent, last_name, username, role_id)
VALUES
	(1, 0, null, 0, NOW(), 'mkain@inria.fr', null, null, null, 0, 'Michael', 0, 0, 'Kain', 'mkain', 1);
	
INSERT INTO users
	(id, account_request_demand, account_request_info_id, can_access_to_dicom_association, creation_date, email, expiration_date, extension_date, extension_motivation, extension_request_demand, first_name, first_expiration_notification_sent, second_expiration_notification_sent, last_name, username, role_id)
VALUES
	(2, 0, null, 0, NOW(), 'yyao@inria.fr', null, null, null, 0, 'Yao', 0, 0, 'Chi', 'yyao', 2);
	
INSERT INTO users
	(id, account_request_demand, account_request_info_id, can_access_to_dicom_association, creation_date, email, expiration_date, extension_date, extension_motivation, extension_request_demand, first_name, first_expiration_notification_sent, second_expiration_notification_sent, last_name, username, role_id)
VALUES
	(3, 0, null, 0, NOW(), 'jlouis@inria.fr', null, null, null, 0, 'Julien', 0, 0, 'Louis', 'jlouis', 3);

	