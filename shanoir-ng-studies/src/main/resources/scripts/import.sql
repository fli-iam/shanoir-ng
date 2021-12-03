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
--
use studies;

INSERT INTO study
	(id, clinical, coordinator_id, downloadable_by_default, end_date, mono_center, name, start_date, study_status, study_type, visible_by_default, with_examination, challenge)
VALUES
	(1, '\0', NULL, '\0', NULL, 1, 'DemoStudy', '2020-11-02 00:00:00', 1, 3, '\0', 1, 0);

INSERT INTO center(id, country, name, phone_number, postal_code, street, city, website)
VALUES
	(1, 'France', 'CHU Rennes', '', '', '', 'Rennes', '');

INSERT INTO study_center
	(id, center_id, study_id)
VALUES
	(1, 1, 1);


INSERT INTO subject
	(id, name, identifier, birth_date, imaged_object_category, language_hemispheric_dominance,  manual_hemispheric_dominance, sex,  pseudonymus_hash_values_id)
VALUES
	(1,'DemoSubject', 'sub1', '2013/01/01', 2, 1, 1, 2, NULL);


INSERT INTO subject_study
	(id, physically_involved, study_id, subject_id, subject_study_identifier, subject_type)
VALUES
	(1, 0, 1, 1, 'Subject 1 for study 1', 1);

INSERT INTO `manufacturer`
	(id, name)
VALUES
	(1, 'GE MEDICAL SYSTEMS'),
	(2, 'Philips Medical Systems'),
	(3, 'SIEMENS'),
	(5, 'Philips Healthcare'),
	(6, 'AXIOM ARTIS DBA');

INSERT INTO `manufacturer_model`
	(id, dataset_modality_type, name, magnetic_field, manufacturer_id)
VALUES
	(1,1,'Achieva',3,2),
	(2,1,'Symphony',1.5,3),
	(3,1,'Verio',3,3),
	(4,1,'Signa HDxt',3,1),
	(5,1,'Sonata',1.5,3),
	(6,1,'Aera',1.5,3),
	(7,1,'Avanto',1.5,3),
	(8,1,'DISCOVERY MR750',3,1),
	(12,1,'Intera',1.5,2),
	(13,1,'Achieva',1.5,2),
	(14,1,'TrioTim',3,3),
	(16,1,'Ingenia',3,5),
	(17,1,'DISCOVERY MR750w',3,1),
	(18,1,'GENESIS_SIGNA',1.5,1),
	(19,1,'Skyra',3,3),
	(20,1,'Signa',3,1),
	(21,1,'SIGNA',15,1),
	(22,1,'SIGNA HDX',1.5,1),
	(23,1,'Optima MR 450w',1.5,1),
	(24,1,'Ingenia',1.5,5),
	(25,1,'DSA',0,5),
	(26,1,'DSA',0,3),
	(27,1,'DSA',0,1),
	(28,1,'TDM NANTES',0,1),
	(29,1,' LIGHTSPEED VCT64',0,1),
	(30,1,'IGS INNOVA',0,1),
	(31,1,'Artis Zee Pure-DSA',0,3),
	(32,1,'ALLURA DSA',0,5),
	(33,1,'Brillance190p',0,5),
	(34,1,'Brightspeed 16',0,1),
	(35,1,'MR450W W GEM XP',1.5,1),
	(36,1,'TDM',0,1),
	(37,1,'Discovery',0,1),
	(38,1,'LightSpeed Optima CT660 ',0,1),
	(39,1,'Prisma',3,3),
	(40,1,'Definition As64',0,3),
	(41,1,'SKYRA',1.5,3),
	(42,1,'somatom',0,3),
	(43,1,'BUTTERFLY',0,1),
	(44,1,'Artis Q',0,3),
	(45,1,'AXIOM ARTIS DBA',0,3),
	(46,1,'Ingenuity CT 128',0,5),
	(47,1,'Brillance 40',0,5),
	(48,1,'Revolution',0,1),
	(49,1,'Optima 540',0,1),
	(50,1,'Optima540',0,1),
	(51,1,'Achieva-dStream',3,2),
	(52,1,'SIGNA HDe',1.5,1),
	(53,1,'INGENUITY CORE',0,5),
	(54,1,'Ingenia',3,2),
	(55,1,'Optima CT 660',0,1),
	(56,1,'Optima CT660',0,1),
	(57,1,'Revolution EVO',0,1),
	(58,1,'Integris V',0,2);

INSERT INTO `acquisition_equipment`
	(id, serial_number, center_id, manufacturer_model_id)
VALUES
	(1,'00000',1,3);

INSERT INTO `coil`
	(id, center_id, coil_type, manufacturer_model_id, name, number_of_channels, serial_number)
VALUES
	(1, 1, 1, 3, 'BODY', 1, NULL);
