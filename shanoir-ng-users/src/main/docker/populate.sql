-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use shanoir_ng_users;

insert into role
	(id, access_level, name)
values
	(1, 1000, 'Administrator'),
    (2, 200, 'Expert'),
    (3, 100, 'User'),
    (4, 0, 'Guest');

--insert into users
--	(id, creation_date, email, first_name, last_name, username, role_id)
--values
--	(1, NOW(), 'admin@shanoir.fr', 'Michael', 'Kain', 'admin', 1),
--	(2, NOW(), 'jlouis@shanoir.fr', 'Julien', 'Louis', 'jlouis', 2),
--	(3, NOW(), 'yyao@shanoir.fr', 'Yao', 'Yao', 'yyao', 2),
--	(4, NOW(), 'jacques.martin@gmail.com', 'Jacques', 'Martin', 'jmartin', 3),
--	(5, NOW(), 'ricky.martin@gmail.com', 'Ricky', 'Martin', 'wopa', 3),
--	(6, NOW(), 'michel.sardou@gmail.com', 'Michel', 'sardou', 'connemara', 3),
--	(7, NOW(), 'paul.bismuth@gmail.com', 'Paul', 'Bismuth', 'ns2017', 4);