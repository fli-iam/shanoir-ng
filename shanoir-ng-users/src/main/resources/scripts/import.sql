-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use shanoir_ng_users;

INSERT INTO role 
VALUES 
	(1,1000,'Administrator','adminRole'),
	(2,0,'Guest','guestRole'),
	(3,100,'User','userRole'),
	(4,200,'Expert','expertRole');

INSERT INTO users
	(id, can_access_to_dicom_association, creation_date, email, first_name, is_first_expiration_notification_sent, is_medical, is_on_demand, is_second_expiration_notification_sent, last_name, password, username, role_id)
VALUES
	(1, 0, NOW(), 'admin@shanoir.fr', 'Michael', 0, 0, 0, 0, 'Kain', 'D0-483351E2-30', 'admin', 1),
	(2, 0, NOW(), 'jlouis@shanoir.fr', 'Julien', 0, 0, 0, 0, 'Louis', '182413194A741C', 'jlouis', 2),
	(3, 0, NOW(), 'yyao@shanoir.fr', 'Yao', 0, 0, 0, 0, 'Yao', '51812A42578709', 'yyao', 2),
	(4, 0, NOW(), 'jacques.martin@gmail.com', 'Jacques', 0, 0, 0, 0, 'Martin', '92-1102B43C0-6', 'jmartin', 3),
	(5, 0, NOW(), 'ricky.martin@gmail.com', 'Ricky', 0, 0, 0, 0, 'Martin', '4165C7-57B4-76', 'wopa', 3),
	(6, 0, NOW(), 'michel.sardou@gmail.com', 'Michel', 0, 0, 0, 0, 'sardou', '1117DC-36DE-34', 'connemara', 3),
	(7, 0, NOW(), 'paul.bismuth@gmail.com', 'Paul', 0, 0, 0, 0, 'Bismuth', 'F8-820327C124B', 'ns2017', 4);