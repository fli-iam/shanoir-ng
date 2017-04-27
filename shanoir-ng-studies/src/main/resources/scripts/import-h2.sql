-- Populates database for test

INSERT INTO study_status 
	(id, label_Name)
VALUES 
	(1,'finished'),
	(2,'in_progress');

INSERT INTO study
	(id,  name, start_date, end_date, clinical, with_examination, is_visible_by_default, is_downloadable_by_default)
VALUES 
	(1,'shanoirStudy1', NOW(),  parsedatetime('2017/12/31', 'yyyy/MM/dd'), 1, 0, 0, 0),
	(2,'shanoirStudy2', NOW(), parsedatetime('2017/12/31', 'yyyy/MM/dd'), 0, 0, 0, 0),
	(3,'shanoirStudy3', NOW(), parsedatetime('2017/12/31', 'yyyy/MM/dd'), 1, 0, 0, 0);
	
INSERT INTO REL_STUDY_USER
	(REL_STUDY_USER_ID, USER_ID, study)
VALUES 
	(1, 1, 1),
	(2, 2, 2),
	(3, 3, 3);

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
	(1, 'MR_DATASET', 2, 'DISCOVERY MR750', 3),
	(2, 'PET_DATASET', 2, 'DISCOVERY MR750w', null),
	(3, 'MR_DATASET', 3, 'Ingenia', 1.5);

INSERT INTO acquisition_equipment
	(id, center_id, manufacturer_model_id, serial_number)
VALUES 
	(1, 1, 1, '123456789'),
	(2, 2, 1, '234567891'),
	(3, 1, 2, '345678912');
	
INSERT INTO subject
	(id, name, identifier, birth_date )
VALUES
	(1,'subject1', 'sub1', parsedatetime('2013/01/01', 'yyyy/MM/dd')),
	(2,'subject2', 'sub2', parsedatetime('2001/02/01', 'yyyy/MM/dd')),
	(3,'0010001', 'sub3', parsedatetime('2001/02/01', 'yyyy/MM/dd')),
	(4,'0010002', 'sub4', parsedatetime('2001/02/01', 'yyyy/MM/dd'));

