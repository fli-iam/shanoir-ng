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
