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

-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use datasets;

INSERT INTO `study_cards` VALUES (1,1,_binary '\0','StudyCard1',1,1),(2,1,_binary '\0','StudyCard2',1,1),(3,3,_binary '\0','StudyCard3',1,2),(4,4,_binary '\0','StudyCard4',1,3);
INSERT INTO `study_card_rule` VALUES (3,1),(4,1),(5,1),(6,1);
INSERT INTO `study_card_assignment` VALUES (3,11,'WORKED !!!!!',3),(4,14,'TIME_OF_FLIGHT_MR_DATASET',4),(5,5,'5',4),(6,11,'ERROR',5),(7,11,'OVERRIDEN',6),(8,4,'4',6);
INSERT INTO `study_card_condition` VALUES (2,528446,'tse_vfl_WIP607',2,3),(3,1573009,'200',6,5),(4,1573009,'150',5,5),(5,1573009,'150',5,6),(6,1573013,'781.00',2,6);

INSERT INTO examination
	(id, center_id, examination_date, investigator_external, investigator_id, note, study_id, subject_id, comment, preclinical)
VALUES
	(1, 1, now(), false, 1, 'examination1', 1, 1, 'examination1', false),
	(2, 1, now(), false, 1, 'examination2', 1, 2, 'examination2', false),
	(3, 1, now(), false, 1, 'examination3', 1, 3, 'examination3', false),
	(4, 1, now(), false, 1, 'examination4', 2, 1, 'examination4', false);

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
	(3, 1, 'CTDataset1'),
    (4, 1, 'EEGDataset');

INSERT INTO dataset
	(id, dataset_acquisition_id, origin_metadata_id, subject_id) 
VALUES 
	(1, 1, 1, 1),
	(2, 2, 2, 1),
	(3, 3, 3, 1),
	(4, NULL, 4, 1);

INSERT INTO study
	(id, name)
VALUES
	(1, 'NATIVE Divers'),
	(2, 'USPIO-6');
	
INSERT INTO subject
	(id, name)
VALUES
	(1, 'subject1'),
	(2, 'subject2'),
	(3, '0010001');
	
INSERT INTO mr_dataset_metadata
	(id, mr_dataset_nature) 
VALUES 
	(1, 1);

INSERT INTO dataset
	(id, dataset_acquisition_id, origin_metadata_id, updated_metadata_id, study_id, subject_id) 
VALUES 
	(1, 1, 1, 1, 1, 1),
	(2, 2, 2, 2, 1, 1),
	(3, 3, 3, 3, 1, 1),
	(4, NULL, 4, 4, 1, 1);

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
	
INSERT INTO eeg_dataset
    (id, channel_count, sampling_frequency)
VALUES
    (4, 1, 1);

INSERT INTO event
    (id, type, description, channel_number, points, dataset_id)
VALUES
    (1, "type", "description", 1, 1, 4);

INSERT INTO channel
    (id, name, reference_type, reference_units, resolution, x, y, z, high_cutoff, low_cutoff, notch, dataset_id)
VALUES
    (1, "test", 1, "reference_unit", 1, 1, 1, 1, 1, 1, 1, 4);
