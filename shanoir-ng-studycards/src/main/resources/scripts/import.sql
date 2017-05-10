-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

--use shanoir_ng_studyCards;

INSERT INTO study_cards 
	(id, acquisition_equipment_id, center_id, disabled, name, nifti_converter_id, study_id)
VALUES 
	(1, 1, 1, 0, 'StudyCard1', 1, 1),
	(2, 1, 1, 0, 'StudyCard2', 1, 1),
	(3, 3, 1, 0, 'StudyCard3', 1, 2),
	(4, 4, 3, 0, 'StudyCard4', 1, 3);
