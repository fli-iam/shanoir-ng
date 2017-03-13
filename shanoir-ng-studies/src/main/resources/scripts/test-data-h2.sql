-- Populates database for test

INSERT INTO study
	(id, name, startDate, endDate, clinical, withExamination, isVisibleByDefault, isDownloadableByDefault, refStudyStatus)
VALUES 
	(1,'Study 1', parsedatetime('2013/01/01', 'yyyy/MM/dd'), null, 0, 1, 0, 0, null),
	(1,'Study 2', parsedatetime('2009/12/01', 'yyyy/MM/dd'), null, 0, 1, 0, 0, null),
	(1,'Study 3', parsedatetime('2010/02/21', 'yyyy/MM/dd'), null, 0, 1, 0, 0, null),
	(1,'Study 4', parsedatetime('2015/10/03', 'yyyy/MM/dd'), null, 0, 1, 0, 0, null);
