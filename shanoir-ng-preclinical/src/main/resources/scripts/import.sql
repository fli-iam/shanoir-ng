-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use preclinical;

INSERT INTO reference
(id, reftype, value, category)
VALUES (1,'specie','Rat','subject'),
		(2,'specie','Mouse','subject'),
		(3,'biotype','Wild','subject'),
		(4,'biotype','Transgenic','subject'),
		(5,'strain','Wistar','subject'),
		(6,'provider','Charles River','subject'),
		(7,'stabulation','Grenoble','subject'),
		(8,'strain','xx98','subject'),
		(9,'provider','Janvier','subject'),
		(10,'location','Brain','anatomy'),
		(11,'location','Heart','anatomy'),
		(12,'stabulation','Paris','subject'),
		(13,'gray','Gy','unit'),
		(14,'gray','mgy','unit'),
		(15,'volume','ml','unit'),
		(16,'ingredient','Isoflurane','anesthetic'),
		(17,'ingredient','Ketamine','anesthetic'),
		(18,'ingredient','Xylamine','anesthetic'),
		(19,'name','Gadolinium','contrastagent'),
		(20,'name','Uspio','contrastagent'),
		(21,'concentration','%','unit'),
		(22,'concentration','mg/ml','unit');
		
INSERT INTO pathology
	(id, name)
VALUES (1,'Stroke'),
		(2,'Alzheimer'),
		(3,'Cancer'),
		(4,'Bone');
				
INSERT INTO therapy 
	(id, comment, name, therapy_type)
VALUES (1,NULL,'Brainectomy', 'SURGERY'),
		(2,NULL,'Chimiotherapy','DRUG');

