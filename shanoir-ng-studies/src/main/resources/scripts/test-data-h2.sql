-- Populates database for test

INSERT INTO study
	(id, name, start_date, end_date, clinical, with_examination, is_visible_by_default, is_downloadable_by_default, study_status_id)
VALUES 
	(1,'Study 1', parsedatetime('2013/01/01', 'yyyy/MM/dd'), null, 0, 1, 0, 0, null),
	(2,'Study 2', parsedatetime('2009/12/01', 'yyyy/MM/dd'), null, 0, 1, 0, 0, null),
	(3,'Study 3', parsedatetime('2010/02/21', 'yyyy/MM/dd'), null, 0, 1, 0, 0, null),
	(4,'Study 4', parsedatetime('2015/10/03', 'yyyy/MM/dd'), null, 0, 1, 0, 0, null);

insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (1,'France','CHU Rennes','','','','Rennes','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (2,'France','CHU Reims','','','','Reims','');
