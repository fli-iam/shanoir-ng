-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use shanoir_ng_template;

INSERT INTO template 
VALUES 
	(1,'Data1'),
	(2,'Data2'),
	(3,'Data3'),
	(4,'Data4');
