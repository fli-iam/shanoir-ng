-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !
--
--use shanoir_ng_studies;


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
	
--INSERT INTO REF_STUDY_USER_TYPE
--	(id, userId, study, refStudyUserType)
--VALUES 
--	(1, 1 ,1 ,1 ),
--	(2, 1 ,2 ,1 ),
--	(3, 2 ,3 ,1 );	
	
