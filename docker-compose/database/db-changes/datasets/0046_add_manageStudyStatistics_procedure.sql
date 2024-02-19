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


use datasets;

DROP PROCEDURE IF EXISTS getManageStudyStatistics;

delimiter //
CREATE PROCEDURE getManageStudyStatistics(IN studyId INT)
BEGIN
SELECT
    e.study_id AS study_id,
    c.name AS center_name,
    s.id AS subject_id,
    s.name AS common_name,
    e.id AS examination_id,
    e.examination_date AS examination_date,
    da.id AS dataset_acquisition_id,
    da.creation_date AS import_date,
    d.id AS dataset_id,
    (case dm.dataset_modality_type when 1 then 'Mr' 
                                   when 2 then 'Meg' 
                                   when 3 then 'Ct' 
                                   when 4 then 'Spect' 
                                   when 5 then 'Pet' 
                                   when 6 then 'Egg' 
                                   when 7 then 'Generic' 
                                   when 8 then 'Ieeg' 
                                   when 9 then 'Micr' 
                                   when 10 then 'Beh'
                                   when 11 then 'Nirs'
                                   when 12 then 'Xa' end) AS modality,
    (case ss.quality_tag when 1 then 'Valid' when 2 then 'Warning' when 3 then 'Error' end) AS quality
FROM
    datasets.examination e
INNER JOIN 
    datasets.center c ON e.center_id = c.id
INNER JOIN
    datasets.dataset_acquisition da ON e.id = da.examination_id
INNER JOIN
    datasets.dataset d ON da.id = d.dataset_acquisition_id
INNER JOIN
    datasets.dataset_metadata dm ON d.updated_metadata_id = dm.id
INNER JOIN
    datasets.subject_study ss ON d.subject_id = ss.subject_id
INNER JOIN 
    studies.subject s ON ss.subject_id = s.id
WHERE e.study_id = studyId;
END //

delimiter ;
