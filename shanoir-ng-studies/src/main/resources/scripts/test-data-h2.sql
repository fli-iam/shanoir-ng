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

-- Populates database for test

INSERT INTO study
	(id,  name, start_date, end_date, clinical, with_examination, visible_by_default, downloadable_by_default, study_status, mono_center, study_type)
VALUES 
	(1,'shanoirStudy1', NOW(), parsedatetime('2017/12/31', 'yyyy/MM/dd'), 1, 0, 0, 0, 1, 1, 1),
	(2,'shanoirStudy2', NOW(), parsedatetime('2017/12/31', 'yyyy/MM/dd'), 0, 0, 0, 0, 1, 1, 1),
	(3,'shanoirStudy3', NOW(), parsedatetime('2017/12/31', 'yyyy/MM/dd'), 1, 0, 0, 0, 1, 0, 1);

INSERT INTO study_user
	(id, receive_anonymization_report, receive_new_import_report, study_id, user_id, user_name)
VALUES
	(1, 0, 1, 1, 1, 'admin'),
	(2, 0, 1, 3, 1, 'admin'),
	(3, 0, 0, 1, 2, 'jlouis'),
	(4, 0, 1, 2, 1, 'admin');

INSERT INTO study_user_study_user_rights
	(study_user_id, study_user_rights)
VALUES
    (1, 1),
    (2, 1),
    (3, 3),
    (3, 4),
    (4, 1);
	
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (1,'France','CHU Rennes','','','','Rennes','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (2,'France','CHU Reims','','','','Reims','');

INSERT INTO manufacturer
	(id, name)
VALUES 
	(1, 'GE Healthcare'),
	(2, 'GE Medical Systems'),
	(3, 'Philips Healthcare');

INSERT INTO manufacturer_model
	(id, dataset_modality_type, manufacturer_id, name, magnetic_field)
VALUES 
	(1, 1, 2, 'DISCOVERY MR750', 3),
	(2, 5, 2, 'DISCOVERY MR750w', null),
	(3, 1, 3, 'Ingenia', 1.5);

INSERT INTO acquisition_equipment
	(id, center_id, manufacturer_model_id, serial_number)
VALUES 
	(1, 1, 1, '123456789'),
	(2, 2, 1, '234567891'),
	(3, 1, 2, '345678912');
	
INSERT INTO coil
	(id, center_id, coil_type, manufacturer_model_id, name, number_of_channels, serial_number)
VALUES 
	(1, 1, 2, 2, 'coil 1', 8, '123456789'),
	(2, 1, 1, 2, 'coil 2', 16, 123456789),
	(3, 2, 1, 2, 'coil 3', 4, '234567891');

INSERT INTO subject
	(id, name, identifier, birth_date )
VALUES
	(1,'subject1', 'sub1', parsedatetime('2013/01/01', 'yyyy/MM/dd')),
	(2,'subject2', 'sub2', parsedatetime('2001/02/01', 'yyyy/MM/dd')),
	(3,'0010001', 'sub3', parsedatetime('2001/02/01', 'yyyy/MM/dd')),
	(4,'0010002', 'sub4', parsedatetime('2001/02/01', 'yyyy/MM/dd'));

INSERT INTO subject_study
	(id, physically_involved, study_id, subject_id, subject_study_identifier, subject_type)
VALUES
	(1, 0, 1, 1, 'Subject 1 for study 1', 1),
	(2, 0, 1, 2, 'Subject 2 for study 1', 2),
	(3, 0, 2, 1, 'Subject 1 for study 2', 2);
