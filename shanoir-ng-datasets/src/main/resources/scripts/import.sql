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

INSERT INTO study_cards 
	(id, acquisition_equipment_id, center_id, disabled, name, nifti_converter_id, study_id)
VALUES 
	(1, 1, 1, 0, 'StudyCard1', 1, 1),
	(2, 1, 1, 0, 'StudyCard2', 1, 1),
	(3, 3, 1, 0, 'StudyCard3', 1, 2),
	(4, 4, 3, 0, 'StudyCard4', 1, 3);

INSERT INTO examination
	(id, center_id, examination_date, investigator_external, investigator_id, note, study_id, subject_id, comment)
VALUES
	(1, 1, now(), false, 1, 'examination1', 1, 1, 'examination1'),
	(2, 1, now(), false, 1, 'examination2', 1, 2, 'examination2'),
	(3, 1, now(), false, 1, 'examination3', 1, 3, 'examination3'),
	(4, 1, now(), false, 1, 'examination4', 2, 1, 'examination4');

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
	(id, dataset_acquisition_id, origin_metadata_id, updated_metadata_id, study_id, subject_id) 
VALUES 
	(1, 1, 1, 1, 1, 1),
	(2, 2, 2, 2, 1, 1),
	(3, 3, 3, 3, 1, 1);

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

INSERT INTO study
	(id, name)
VALUES
	(1, 'NATIVE Divers'),
	(2, 'USPIO-6'),
	(3, 'Phantom Qualite'),
	(4, 'AtlasDIR@Neurinfo'),
	(5, 'DIR@Epilepsie'),
	(6, 'Emodes'),
	(7, 'Moelle'),
	(9, '3D T1 '),
	(10, 'CIS DIR'),
	(11, 'Transfert'),
	(12, 'IRMf '),
	(13, 'isaSegTum'),
	(14, 'asl_SEP'),
	(15, 'asl_CTL'),
	(17, 'DIFF_ISCHEMIE'),
	(18, 'USPIO-6 ASL'),
	(19, 'transIRMf'),
	(20, 'AngioIRM_NATIVE_QISS'),
	(21, 'FIM'),
	(22, 'DEPRESIST'),
	(23, 'FIM ft'),
	(24, 'Jonin'),
	(25, 'DEEP'),
	(26, 'ASL IP'),
	(27, 'ASLf mt'),
	(28, 'asl_tum'),
	(29, 'asl_vasc'),
	(30, 'PNGI'),
	(31, 'DIR EP CLIN'),
	(32, 'ASL SENSE 2'),
	(33, 'OFSEP_Test_Lyon'),
	(34, 'iQALY-SEP'),
	(35, 'AINSI-GIN'),
	(36, 'PerfT1'),
	(37, 'ASL DEM'),
	(38, 'Dysphasie'),
	(39, 'NCE MRA'),
	(40, 'VPIPRO'),
	(41, 'MTX_sep'),
	(42, 'SURFER'),
	(43, 'AINSI'),
	(44, '1RO100'),
	(45, 'CoCoA'),
	(46, 'Synesthesia'),
	(47, 'HYP_T2'),
	(48, 'DEPAPATHIE'),
	(49, 'AfaCorVis3D'),
	(50, 'DIVASL'),
	(51, 'IQALY-SEP+'),
	(52, 'MIDI'),
	(53, 'Emodes_Pilote'),
	(54, 'ATLDIF'),
	(55, 'AVCPOSTIM'),
	(56, 'EMOCAR'),
	(57, 'MALTA'),
	(58, 'NCE MRA PHANTOM'),
	(59, 'USPIO-6 C'),
	(60, 'OFSEP_Pilot_Marseille'),
	(61, 'HEPAT_M'),
	(62, 'OFSEP_Pilot_U825'),
	(63, 'GRECCAR 4'),
	(64, 'OFSEP_Pilot_Rennes'),
	(65, 'OFSEP_Pilot_Vannes'),
	(66, 'TMS Depression'),
	(67, 'OFSEP_Pilot_Reims'),
	(68, 'OFSEP_Pilot_Paris'),
	(69, 'EPI-DISTO'),
	(70, 'SSS-DIMO'),
	(71, 'Methodo ASL'),
	(72, 'OFSEP_Rennes'),
	(73, 'OFSEP_Grenoble'),
	(74, 'HORAW'),
	(75, 'OFSEP_Pilot_CHLS'),
	(76, 'PSY MORPHO ASL'),
	(77, 'MS-REPAIR'),
	(78, 'OFSEP_Pilot_Hopital-Neuro'),
	(79, 'OFSEP_Pilot_Bordeaux'),
	(80, 'AVCPOSTIM MORPHO'),
	(81, 'CAPP-CATI'),
	(82, 'MS-SPI'),
	(83, 'DEEP GREEN'),
	(84, '*TEST'),
	(85, '*Volontaires Sains'),
	(86, 'EMISEP_Pilote'),
	(87, 'LONGIDEP'),
	(88, 'MP2Relaxo'),
	(89, 'PERINE_Pilote'),
	(90, 'VEP'),
	(91, 'OptimMS'),
	(92, 'MoNICa'),
	(93, 'ASL_Pilote'),
	(94, 'EMISEP'),
	(96, 'ASL_Pilote_CATI'),
	(97, 'HEMISFER_Pilote'),
	(98, 'HEMOCOEUR'),
	(99, 'F-Tract'),
	(100, 'OxyTC'),
	(101, 'PERINE'),
	(102, 'LUNG'),
	(103, 'T1Mapping_Repro'),
	(104, 'Sprite'),
	(105, 'AGIR-PARK'),
	(106, 'RICART'),
	(107, 'HEMO MAV'),
	(108, 'Tracto-SCP-Pilote'),
	(109, 'Tracto-SCP'),
	(110, 'ReproRelaxo'),
	(111, 'CineRT'),
	(112, 'BrainGraphs'),
	(113, 'EPMR-MA Pilote OSS'),
	(114, 'HED-O-SHIFT Pilote OSS'),
	(115, 'EPMR-MA'),
	(116, 'HED-O-SHIFT'),
	(117, 'CineIRM'),
	(118, 'COGNISEP Pilote OSS'),
	(119, 'PEPS EC'),
	(120, 'ASL DISTO'),
	(121, 'ASL_pedia'),
	(122, 'DAbdo3D'),
	(123, 'RESSTORE'),
	(124, 'ICAN'),
	(125, 'ADERASL'),
	(126, 'test'),
	(127, 'Test_DerivedStudy'),
	(128, 'QUANT_MRI'),
	(129, 'MB-Diffusion'),
	(130, 'COGNISEP Patients'),
	(131, 'UTE4EEG'),
	(132, 'FastMicroDiff'),
	(133, 'CaractRF'),
	(134, 'Sharing_SynesthesiaData');

INSERT INTO center(id, name)
VALUES
	(1, 'CHU Rennes'),
	(2, 'CHU Reims'),
	(3, 'LPS - CENIR'),
	(4, 'CHU Marseille'),
	(5, 'HCL - NeuroCardio'),
	(7, 'Hôpitaux Universitaires de Strasbourg'),
	(8, 'Centre Hospitalier Yves Le Foll'),
	(9, 'CHU Nîmes'),
	(10, 'CHU Toulouse'),
	(11, 'CHU Nantes'),
	(12, 'CHU Brabois'),
	(13, 'CHU de Fort-de-France'),
	(14, 'CHU Michallon'),
	(15, 'GIN'),
	(16, 'CHU Brest'),
	(17, 'CHGR'),
	(18, 'HCL - NeuroCardio to delete'),
	(19, 'HCL - Hopital Edouard Herriot'),
	(20, 'HCL - CHLS RMN'),
	(21, 'HCL - NeuroCardio HFME'),
	(22, 'HCL - GIE Lyon Nord'),
	(23, 'INSERM_825'),
	(24, 'Belgique'),
	(25, 'CH Bretagne Atlantique Vannes'),
	(27, 'ICM'),
	(28, 'CHU Bordeaux'),
	(29, 'HCL - CHLS'),
	(30, 'CHRU Clermont-Ferrand Gabriel Montpied'),
	(31, 'CHRU Montpellier Gui de Chauliac'),
	(33, 'IRM des sources'),
	(35, 'IPB Strasbourg'),
	(36, 'Hôpital de la Timone - Marseille'),
	(37, 'HCL - Hôpital Pierre Wertheimer'),
	(38, 'CHU - Charles-Nicolle- Rouen'),
	(39, 'CHU - Dijon'),
	(40, 'CHU - Hôpital Central - Nancy'),
	(41, 'CHRU - Lille'),
	(43, 'Hôpital Beaujon -Paris'),
	(44, 'Hôpital Bicêtre - Paris'),
	(45, 'Hopital d\'Instruction des Armées Sainte Anne'),
	(46, 'CHU St-Etienne'),
	(47, 'CH Annecy Genevois'),
	(48, 'Hopital Nord - Marseille'),
	(49, 'Hopital Pitié Salpétrière'),
	(50, 'CHU St Roch - Nice'),
	(51, 'CHU Poitiers'),
	(52, 'Hôpital Saint-Anne'),
	(53, 'CHU Limoges'),
	(54, 'CHU Angers'),
	(55, 'CHU Sud Reunion'),
	(56, 'Hôpital Clairval'),
	(57, 'CHU Amiens'),
	(58, 'CHU Rouen'),
	(59, 'CHU Clermont Ferrand'),
	(60, 'Fondation Ophtalmologique de Rothschild'),
	(61, 'CHU Montpellier'),
	(62, 'CHRU Besançon'),
	(63, 'CHD de Vendée'),
	(64, 'CHU Grenoble'),
	(65, 'CHRU de Tours'),
	(66, 'Colmar'),
	(67, 'CH Colmar'),
	(68, 'CREATIS');

INSERT INTO subject
	(id, name)
VALUES
	(1,'subject1'),
	(2,'subject2'),
	(3,'0010001');

