-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use users;

INSERT INTO role
VALUES 
	(1,1000,'Administrator','ROLE_ADMIN')
	(2,100,'User','ROLE_USER'),
	(3,200,'Expert','ROLE_EXPERT');

INSERT INTO users
	(id, account_request_demand, account_request_info_id, can_access_to_dicom_association, creation_date, email, expiration_date, extension_date, extension_motivation, extension_request_demand, first_name, first_expiration_notification_sent, second_expiration_notification_sent, last_name, username, role_id)
VALUES
	(1, 0, null, 0, NOW(), 'admin@shanoir.fr', null, null, null, 0, 'adminFirstName', 0, 0, 'adminFirstName', 'admin', 1);