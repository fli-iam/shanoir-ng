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

INSERT INTO reference
(id, reftype, value, category)
VALUES (1,'specie','Rat','subject'),
		(2,'specie','Mouse','subject'),
		(3,'biotype','Wild','subject'),
		(4,'biotype','Transgenic','subject'),
		(6,'strain','Wistar','subject'),
		(7,'provider','Simon','subject'),
		(8,'stabulation','Grenoble','subject'),
		(9,'strain','xx98','subject'),
		(10,'provider','Bobbu','subject'),
		(14,'location','Brain','anatomy'),
		(15,'location','Heart','anatomy'),
		(19,'stabulation','Paris','subject'),
		(22,'gray','Gy','unit'),
		(24,'gray','mgy','unit'),
		(28,'volume','ml','unit'),
		(29,'ingredient','Isoflurane','anesthetic'),
		(30,'ingredient','Ketamine','anesthetic'),
		(31,'ingredient','Xylamine','anesthetic'),
		(34,'name','Gadolinium','contrastagent'),
		(35,'name','Uspio','contrastagent'),
		(36,'concentration','%','unit'),
		(37,'concentration','mg/ml','unit');

INSERT INTO animal_subject 
(id, subject_id, biotype_id, provider_id, specie_id, stabulation_id, strain_id)
VALUES (1,1,4,7,1,8,9),
		(2,2, 4,7,1,8,6),
		(3,3, 4,10,1,8,6),
		(4,4, 4,7,2,8,9);

INSERT INTO pathology
	(id, name)
VALUES (1,'Stroke'),
		(2,'Alzheimer'),
		(3,'Cancer'),
		(4,'Bone');

INSERT INTO pathology_model
	(id, comment, name,pathology_id,filename, filepath,  )
VALUES (1,NULL,'U836',1,NULL,NULL),
		(2,NULL,'XXXX',2,NULL,NULL),
		(3,NULL,'ZZZZ',3,NULL,NULL);
		

INSERT INTO subject_pathology 
	(id, end_date, start_date, location_id, pathology_id, pathology_model_id, animal_subject_id)
VALUES (1,NULL,NULL,14,1,1,1),
		(2,NULL,NULL,14,2,2,2),
		(3,NULL,NULL,14,3,3,3);
		
INSERT INTO therapy 
	(id, comment, name, therapy_type)
VALUES (1,NULL,'Brainectomy', 'SURGERY'),
		(2,NULL,'Chimiotherapy','DRUG');

		
INSERT INTO subject_therapy
	(id, dose, end_date, start_date, dose_unit_id, frequency, animal_subject_id, therapy_id)
VALUES (1,2,NULL,NULL,28,25,1,2),
		(2,NULL,NULL,NULL,NULL,NULL,1,1),
		(3,NULL,NULL,NULL,NULL,NULL,3,2);

INSERT INTO contrast_agent 
	(id, concentration, dose, manufactured_name, concentration_unit_id, dose_unit_id, injection_interval, injection_site, injection_type, name_id, protocol_id)
VALUES (1,NULL,NULL,'Gadolinium',NULL,NULL,NULL,NULL,NULL,34,1);

INSERT INTO anesthetic
	(id, anesthetic_type, comment, name)
VALUES (1, 'GAS', NULL , 'Gas Iso. 2% Ket. 25%'),
		(2, 'GAS', NULL , 'Gas Ket. 25%'),
		(3, 'INJECTION', NULL , 'Injection Iso. 5%');
		
INSERT INTO anesthetic_ingredient 
	(id, concentration, anesthetic_id,concentration_unit_id, name_id)
VALUES (1,2,1,36,29),
		(2,25,1,36,30),
		(3,5,3,36,29);
		
INSERT INTO examination_anesthetic 
	(id, dose, end_date, injection_interval, injection_site, injection_type, start_date, anesthetic_id, dose_unit_id,examination_id)
VALUES (1,NULL,NULL,NULL,NULL,NULL,NULL,1,NULL,1),
		(2,NULL,NULL,NULL,NULL,NULL,NULL,2,NULL,2),
		(3,NULL,NULL,NULL,NULL,NULL,NULL,3,NULL,3);

INSERT INTO examination_extradata
	(dtype, id, examination_id, extradatatype, filename, filepath, has_heart_rate, has_respiratory_rate, has_sao2, has_temperature)
VALUES ('ExaminationExtraData',1,1,'Extra data','extradata.txt','/home/sloury/Documents/FLI-IAM/SHANOIR_NG/upload/1/physiologicaldata/dictionnaire_dicom.txt',NULL,NULL,NULL,NULL),
		('PhysiologicalData',2,1,'Physiological data','physiologicaldata.txt','/home/sloury/Documents/FLI-IAM/SHANOIR_NG/upload/1/bloodgasdata/dictionnaire_dicom.txt',0,1,1,0),
		('BloodGasData',3,1,'Blood gas data','bloodgasdata.txt','/home/sloury/Documents/FLI-IAM/SHANOIR_NG/upload/1/physiologicaldata/dictionnaire_dicom.txt',NULL,NULL,NULL,NULL);

