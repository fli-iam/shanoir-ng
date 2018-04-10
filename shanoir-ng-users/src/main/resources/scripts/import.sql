-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use users;

INSERT INTO role 
VALUES 
	(1,1000,'Administrator','ROLE_ADMIN'),
	(2,0,'Guest','ROLE_GUEST'),
	(3,100,'User','ROLE_USER'),
	(4,200,'Expert','ROLE_EXPERT');

INSERT INTO account_request_info
	(id, contact, function, institution, service, study, work)
VALUES
	(1, 'contact1', 'function1', 'institution1', 'service1', 'study1', 'work1'),
	(2, 'contact2', 'function2', 'institution2', 'service2', 'study2', 'work2'),
	(3, 'contact3', 'function3', 'institution3', 'service3', 'study3', 'work3');

INSERT INTO users
	(id, account_request_demand, account_request_info_id, can_access_to_dicom_association, creation_date, email, expiration_date, extension_date, extension_motivation, extension_request_demand, first_name, first_expiration_notification_sent, second_expiration_notification_sent, last_name, username, role_id)
VALUES
	(1, 0, null, 0, NOW(), 'admin@shanoir.fr', null, null, null, 0, 'Michael', 0, 0, 'Kain', 'admin', 1),
	(2, 0, null, 0, NOW(), 'jlouis@shanoir.fr', null, null, null, 0, 'Julien', 0, 0, 'Louis', 'jlouis', 3),
	(3, 0, null, 0, NOW(), 'yyao@shanoir.fr', null, null, null, 0, 'Yao', 0, 0, 'Yao', 'yyao', 3),
	(4, 0, null, 0, NOW(), 'jacques.martin@gmail.com', '2016/12/31', null, null, 0, 'Jacques', 0, 0, 'Martin', 'jmartin', 2),
	(5, 0, 2, 0, NOW(), 'ricky.martin@gmail.com', '2017/05/31', '2018/05/31', 'MotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivation MotivationMotivationMotivationMotivation', 1, 'Ricky', 0, 0, 'Martin', 'wopa', 2),
	(6, 1, 1, 0, NOW(), 'michel.sardou@gmail.com', null, null, null, 0, 'Michel', 0, 0, 'sardou', 'connemara', 2),
	(7, 0, 3, 0, NOW(), 'paul.bismuth@gmail.com', null, null, null, 0, 'Paul', 0, 0, 'Bismuth', 'ns2017', 4);