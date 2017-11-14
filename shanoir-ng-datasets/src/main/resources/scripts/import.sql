-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use shanoir_ng_datasets;

INSERT INTO Examination
	(id, center_id, examination_date, investigator_external, investigator_id, note, study_id)
VALUES 
	(1, 1, now(), false, 1, 'examination1', 1),
	(2, 1, now(), false, 1, 'examination2', 1),
	(3, 1, now(), false, 1, 'examination3', 1);

INSERT INTO dataset_acquisition
	(id, acquisition_equipment_id, examination_id, rank, software_release, sorting_index) 
VALUES 
	(1, 1, 1, 1, 'v1.0', 1),
	(2, 1, 1, 1, 'v1.0', 1);

INSERT INTO dataset
	(id, cardinality_of_related_subjects, dataset_acquisition_id, name) 
VALUES 
	(1, 1, 1, 'Dataset1');
