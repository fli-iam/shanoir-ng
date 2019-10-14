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
		(22,'concentration','mg/ml','unit'),
		(23,'strain','Long Evans','subject');
		
INSERT INTO pathology
	(id, name)
VALUES (1,'Stroke'),
		(2,'Alzheimer'),
		(3,'Cancer'),
		(4,'Bone');
		
INSERT INTO pathology_model
	(id, comment, name,pathology_id,filename, filepath  )
VALUES (1,NULL,'U836',1,NULL,NULL);
				
INSERT INTO therapy 
	(id, comment, name, therapy_type)
VALUES (1,NULL,'Brainectomy', 'SURGERY'),
		(2,NULL,'Chimiotherapy','DRUG');
		
INSERT INTO anesthetic (id, anesthetic_type, comment, name) VALUES 
(1, 'INJECTION', NULL, 'Injection Iso. 50.0mg/ml'), 
(2, 'GAS', NULL, 'Gas Iso. 52.0% Xyl. 54.0%'),
(3, 'GAS', NULL, 'Gas Iso. 34.0%');

INSERT INTO anesthetic_ingredient (id, concentration, anesthetic_id, concentration_unit_id, name_id) VALUES 
(1, 50, 1, 22, 16), 
(2, 54, 2, 21, 18), 
(3, 52, 2, 21, 16), 
(4, 34, 3, 21, 16);


INSERT INTO animal_subject (id, subject_id, biotype_id, provider_id, specie_id, stabulation_id, strain_id) VALUES
(1, 4, 3, 9, 1, 7, 23);
