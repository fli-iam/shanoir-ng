-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use shanoir_ng_studies;

INSERT INTO ref_study_status 
	(REF_STUDY_STATUS_ID, LABEL_NAME)
VALUES 
	(1,'finished'),
	(2,'in_progress');

INSERT INTO study
	(study_id, name, START_DATE, END_DATE, IS_CLINICAL, IS_WITH_EXAMINATION, IS_VISIBLE_BY_DEFAULT, IS_DOWNLOADABLE_BY_DEFAULT)
VALUES 
	(1,'shanoirStudy1', now(), '2017/12/31', 1, 0, 0, 0),
	(2,'shanoirStudy2', now(), '2017/11/30', 0, 0, 0, 0),
	(3,'shanoirStudy3', now(),'2017/09/30', 1, 0, 0, 0);
