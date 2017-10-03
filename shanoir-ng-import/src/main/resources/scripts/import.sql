-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use shanoir_ng_import;


INSERT INTO Examination
	(id, note)
VALUES 
	(1,'examination1'),
	(2,'examination2'),
	(3,'examination3');
