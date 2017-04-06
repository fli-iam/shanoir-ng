-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

--use shanoir_ng_studyCards;

INSERT INTO study_cards
VALUES 
	(1 ,0 ,'StudyCard1'),
	(2 ,0 ,'StudyCard2'),
	(3 ,0 ,'StudyCard3'),
	(4 ,0 ,'StudyCard4');
