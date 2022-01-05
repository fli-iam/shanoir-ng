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

-- INSERT INTO `study_cards` VALUES (1,1,_binary '\0','StudyCard1',1,1),(2,1,_binary '\0','StudyCard2',1,1),(3,3,_binary '\0','StudyCard3',1,2),(4,4,_binary '\0','StudyCard4',1,3);
-- INSERT INTO `study_card_rule` VALUES (3,1),(4,1),(5,1),(6,1);
-- INSERT INTO `study_card_assignment` VALUES (3,11,'WORKED !!!!!',3),(4,14,'TIME_OF_FLIGHT_MR_DATASET',4),(5,5,'5',4),(6,11,'ERROR',5),(7,11,'OVERRIDEN',6),(8,4,'4',6);
-- INSERT INTO `study_card_condition` VALUES (2,528446,'tse_vfl_WIP607',2,3),(3,1573009,'200',6,5),(4,1573009,'150',5,5),(5,1573009,'150',5,6),(6,1573013,'781.00',2,6);

INSERT INTO study
	(id, name)
VALUES
	(1, 'DemoStudy');
	
INSERT INTO subject
	(id, name)
VALUES
	(1, 'subject1');

INSERT INTO center
    (id, name)
VALUES
    (1, 'CHU Rennes');

INSERT INTO subject_study
    (id, study_id, subject_id)
VALUES
    (1, 1, 1);
