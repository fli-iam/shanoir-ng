-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use datasets;

INSERT INTO study_cards 
	(id, acquisition_equipment_id, center_id, disabled, name, nifti_converter_id, study_id)
VALUES 
	(1, 1, 1, 0, 'StudyCard1', 1, 1),
	(2, 1, 1, 0, 'StudyCard2', 1, 1),
	(3, 3, 1, 0, 'StudyCard3', 1, 2),
	(4, 4, 3, 0, 'StudyCard4', 1, 3),
	(5, 4, 15, 0, 'Appning', 1, 135);

INSERT INTO examination
	(id, center_id, examination_date, investigator_external, investigator_id, note, study_id, subject_id, comment, preclinical)
VALUES
	(1, 1, now(), false, 1, 'examination1', 1, 1, 'examination1', false),
	(2, 1, now(), false, 1, 'examination2', 1, 2, 'examination2', false),
	(3, 1, now(), false, 1, 'examination3', 1, 3, 'examination3', false),
	(4, 1, now(), false, 1, 'examination4', 2, 1, 'examination4', false),
	(5, 15, now(), false, 1, 'examination5', 2, 15, 'examination5', true);

INSERT INTO mr_protocol_metadata
	(dtype, id, name)
VALUES
	(1, 1, 'MRProtocol1');

INSERT INTO mr_protocol
	(id, echo_train_length, origin_metadata_id)
VALUES
	(1, 5, 1);

INSERT INTO pet_protocol
	(id, dimensionx, dimensiony, number_of_slices, voxel_sizex, voxel_sizey, voxel_sizez)
VALUES
	(1, 10, 10, 5, 2, 2, 2);
	
INSERT INTO ct_protocol
	(id)
VALUES
	(1);

INSERT INTO dataset_acquisition
	(id, acquisition_equipment_id, examination_id, rank, software_release, sorting_index) 
VALUES 
	(1, 1, 1, 1, 'v1.0', 1),
	(2, 1, 2, 1, 'v1.0', 1),
	(3, 1, 2, 1, 'v1.0', 1);

INSERT INTO mr_dataset_acquisition
	(id, mr_protocol_id) 
VALUES 
	(1, 1);
	
INSERT INTO pet_dataset_acquisition
	(id, pet_protocol_id) 
VALUES 
	(2, 1);
	
INSERT INTO ct_dataset_acquisition
	(id, ct_protocol_id) 
VALUES 
	(3, 1);

INSERT INTO dataset_metadata
	(id, cardinality_of_related_subjects, name) 
VALUES 
	(1, 1, 'MRDataset1'),
	(2, 1, 'PETDataset1'),
	(3, 1, 'CTDataset1');

INSERT INTO dataset
	(id, dataset_acquisition_id, origin_metadata_id) 
VALUES 
	(1, 1, 1),
	(2, 2, 2),
	(3, 3, 3);

INSERT INTO mr_dataset_metadata
	(id, mr_dataset_nature) 
VALUES 
	(1, 1);

INSERT INTO mr_dataset
	(id, mr_quality_procedure_type, origin_mr_metadata_id) 
VALUES 
	(1, 1, 1);

INSERT INTO pet_dataset
	(id) 
VALUES 
	(2);

INSERT INTO ct_dataset
	(id) 
VALUES 
	(3);
