-- Populates database for test

INSERT INTO Examination
	(id, center_id, examination_date, investigator_external, investigator_id, note, study_id)
VALUES 
	(1, 1, parsedatetime('2017/01/01', 'yyyy/MM/dd'), false, 1, 'examination1', 1),
	(2, 1, parsedatetime('2017/02/01', 'yyyy/MM/dd'), false, 1, 'examination2', 1),
	(3, 1, parsedatetime('2017/03/01', 'yyyy/MM/dd'), false, 1, 'examination3', 1);

INSERT INTO dataset_acquisition
	(id, acquisition_equipment_id, examination_id, rank, software_release, sorting_index) 
VALUES 
	(1, 1, 1, 1, 'v1.0', 1),
	(2, 1, 2, 1, 'v1.0', 1);

INSERT INTO dataset
	(id, cardinality_of_related_subjects, dataset_acquisition_id, name) 
VALUES 
	(1, 1, 1, 'Dataset1');

INSERT INTO mr_dataset
	(id, echo_time_id, flip_angle_id, inversion_time_id, mr_dataset_nature, mr_quality_procedure_type, repetition_time_id) 
VALUES 
	(1, null, null, null, 1, 1, null);
	