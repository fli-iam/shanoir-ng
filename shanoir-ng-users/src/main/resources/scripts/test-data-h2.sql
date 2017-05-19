-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

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
	(id, account_request_demand, account_request_info_id, can_access_to_dicom_association, creation_date, email, expiration_date, extension_motivation, extension_request, first_name, is_first_expiration_notification_sent, is_second_expiration_notification_sent, last_name, password, username, role_id)
VALUES
	(1, 0, null, 0, NOW(), 'admin@shanoir.fr', null, null, 0, 'Michael', 0, 0, 'Kain', 'D0-483351E2-30', 'admin', 1),
	(2, 0, null, 0, NOW(), 'jlouis@shanoir.fr', null, null, 0, 'Julien', 0, 0, 'Louis', '182413194A741C', 'jlouis', 3),
	(3, 0, null, 0, NOW(), 'yyao@shanoir.fr', null, null, 0, 'Yao', 0, 0, 'Yao', '51812A42578709', 'yyao', 3),
	(4, 0, null, 0, NOW(), 'jacques.martin@gmail.com', parsedatetime('2016/12/31', 'yyyy/MM/dd'), null, 0, 'Jacques', 0, 0, 'Martin', '92-1102B43C0-6', 'jmartin', 2),
	(5, 0, 2, 0, NOW(), 'ricky.martin@gmail.com', parsedatetime('2017/05/31', 'yyyy/MM/dd'), 'MotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivationMotivation MotivationMotivationMotivationMotivation', 1, 'Ricky', 0, 0, 'Martin', '4165C7-57B4-76', 'wopa', 2),
	(6, 1, 1, 0, NOW(), 'michel.sardou@gmail.com', null, null, 0, 'Michel', 0, 0, 'sardou', '1117DC-36DE-34', 'connemara', 2),
	(7, 0, 3, 0, NOW(), 'paul.bismuth@gmail.com', null, null, 0, 'Paul', 0, 0, 'Bismuth', 'F8-820327C124B', 'ns2017', 4);