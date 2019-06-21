-- Shanoir NG - Import, manage and share neuroimaging data
-- Copyright (C) 2009-2019 Inria - https://www.inria.fr/
-- Contact us on https://project.inria.fr/shanoir/
-- 
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
-- 
-- You should have received a copy of the GNU General Public License
-- along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

-- Populates database for test

INSERT INTO study_cards 
	(id, acquisition_equipment_id, center_id, disabled, name, nifti_converter_id, study_id)
VALUES 
	(1, 1, 1, 0, 'StudyCard1', 1, 1),
	(2, 1, 1, 0, 'StudyCard2', 1, 1),
	(3, 3, 1, 0, 'StudyCard3', 1, 2),
	(4, 4, 3, 0, 'StudyCard4', 1, 3);

INSERT INTO Examination
	(id, center_id, examination_date, investigator_external, investigator_id, note, study_id, subject_id)
VALUES 
	(1, 1, parsedatetime('2017/01/01', 'yyyy/MM/dd'), false, 1, 'examination1', 1, 1),
	(2, 1, parsedatetime('2017/02/01', 'yyyy/MM/dd'), false, 1, 'examination2', 1, 2),
	(3, 1, parsedatetime('2017/03/01', 'yyyy/MM/dd'), false, 1, 'examination3', 1, 3);

INSERT INTO mr_protocol_metadata
	(id, name)
VALUES
	(1, 'MRProtocol1');

INSERT INTO mr_protocol
	(id, echo_train_length, origin_metadata_id)
VALUES
	(1, 5, 1);

INSERT INTO pet_protocol
	(id, dimensionx, dimensiony, number_of_slices, voxel_sizex, voxel_sizey, voxel_sizez)
VALUES
	(1, 10, 10, 5, 2, 2, 2);

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
	