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


USE studies;

DROP PROCEDURE IF EXISTS getStudyStatistics;

delimiter //
CREATE PROCEDURE getStudyStatistics(IN studyId INT)
BEGIN
SELECT DISTINCT
    e.study_id AS study_id,
    c.id AS center_id,
    c.name AS center_name,
    sc.subject_name_prefix AS center_prefix,
    s.id AS subject_id,
    s.name AS common_name,
    e.id AS examination_id,
    e.comment AS examination_comment,
    e.examination_date AS examination_date,
    da.id AS dataset_acquisition_id,
    da.import_date AS import_date,
    d.id AS dataset_id,
    dm.name AS dataset_name,
    (CASE dm.dataset_modality_type WHEN 1 THEN 'Mr' 
                                   WHEN 2 THEN 'Meg' 
                                   WHEN 3 THEN 'Ct' 
                                   WHEN 4 THEN 'Spect' 
                                   WHEN 5 THEN 'Pet' 
                                   WHEN 6 THEN 'Egg' 
                                   WHEN 7 THEN 'Generic' 
                                   WHEN 8 THEN 'Ieeg' 
                                   WHEN 9 THEN 'Micr' 
                                   WHEN 10 THEN 'Beh'
                                   WHEN 11 THEN 'Nirs'
                                   WHEN 12 THEN 'Xa' END) AS modality,
    (CASE ss.quality_tag WHEN 1 THEN 'Valid' WHEN 2 THEN 'Warning' WHEN 3 THEN 'Error' END) AS quality
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
    studies.subject_study ss ON d.subject_id = ss.subject_id
INNER JOIN 
    studies.subject s ON ss.subject_id = s.id
INNER JOIN
    studies.study_center sc ON c.id = sc.center_id
WHERE e.study_id = studyId 
AND ss.study_id = studyId 
AND sc.study_id = studyId;
END //

delimiter ;
