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

DROP PROCEDURE IF EXISTS getStatistics;

delimiter //
CREATE PROCEDURE getStatistics(IN studyNameInRegExp VARCHAR(255), IN studyNameOutRegExp VARCHAR(255), IN subjectNameInRegExp VARCHAR(255), IN subjectNameOutRegExp VARCHAR(255), IN startRow INT, IN blocSize INT)
BEGIN
SELECT 'patient_id', 'shanoir_name', 'double_hash', 'birthname1', 'birthname2', 'birthname3', 'lastname1', 'lastname2', 'lastname3', 'firstname1', 'firstname2', 'firstname3', 'birthdate1', 'sex', 'birth_year', 'study_id', 'study_name', 'sequence_id', 'norm_sequence_name', 'sequence_name', 'center_id', 'center', 'center_postcode', 'center_city', 'device_manufacturer', 'device_model', 'device_field_strength', 'device_serial_number', 'examination_id', 'examination_date', 'import_date', 'creation_date', 'series_number', 'protocol_type', 'dicom_size_mo', 'execution'
UNION ALL
SELECT
    subject.id AS patient_id,
    subject.name AS shanoir_name,
    subject.identifier AS double_hash,
    pseudonymus_hash_values.birth_name_hash1 AS birthname1,
    pseudonymus_hash_values.birth_name_hash2 AS birthname2,
    pseudonymus_hash_values.birth_name_hash3 AS birthname3,
    pseudonymus_hash_values.last_name_hash1 AS lastname1,
    pseudonymus_hash_values.last_name_hash2 AS lastname2,
    pseudonymus_hash_values.last_name_hash3 AS lastname3,
    pseudonymus_hash_values.first_name_hash1 AS firstname1,
    pseudonymus_hash_values.first_name_hash2 AS firstname2,
    pseudonymus_hash_values.first_name_hash3 AS firstname3,
    pseudonymus_hash_values.birth_date_hash AS birthdate1,
    (case subject.sex when 1 then 'M' else 'F' end) AS sex,
    year(subject.birth_date) AS birth_year,
    study.id AS study_id,
    study.name AS study_name,
    dataset.id AS sequence_id,
    dataset_metadata.name AS norm_sequence_name,
    dataset_metadata.comment AS sequence_name,
    center.id AS center_id,
    center.name AS center,
    center.postal_code AS center_postcode,
    center.city AS center_city,
    manufacturer.name AS device_manufacturer,
    manufacturer_model.name AS device_model,
    manufacturer_model.magnetic_field AS device_field_strength,
    acquisition_equipment.serial_number AS device_serial_number,
    examination.id AS examination_id,
    date(examination.examination_date) AS examination_date,
    date(dataset_acquisition.import_date) AS import_date,
    date(dataset.creation_date) AS creation_date,
    dataset_acquisition.sorting_index as series_number,
    mr_protocol_metadata.name AS protocol_type,
    (SELECT sum(size)/1000000 FROM dataset_expression AS de WHERE de.dataset_id = dataset.id AND de.dataset_expression_format = 6) AS dicom_size_mo,
    (SELECT JSON_OBJECT('execution_name', exe.name, 'status', (CASE
    WHEN exe.status = 0 THEN 'RUNNING'
    WHEN exe.status = 1 THEN 'FINISHED'
    WHEN exe.status = 2 THEN 'EXECUTION_FAILED'
    WHEN exe.status = 4 THEN 'KILLED'
    ELSE 'UNKNOWN'
    END), 'processing_date', proc.processing_date, 'properties', JSON_ARRAYAGG(JSON_OBJECT(property.name, property.value)))
    FROM datasets.dataset_property property
    LEFT JOIN datasets.dataset_processing proc ON property.dataset_processing_id = proc.id
    LEFT JOIN datasets.execution_monitoring exe ON proc.id = exe.id
    WHERE property.dataset_id = dataset.id
    GROUP BY property.dataset_processing_id
    ORDER BY exe.start_date DESC
    LIMIT 1) AS execution

FROM dataset
    LEFT JOIN dataset_acquisition ON (dataset_acquisition.id = dataset.dataset_acquisition_id)
    LEFT JOIN dataset_metadata ON (dataset_metadata.id = dataset.updated_metadata_id)
    LEFT JOIN mr_dataset_acquisition ON (mr_dataset_acquisition.id = dataset_acquisition.id)
    LEFT JOIN mr_protocol ON (mr_protocol.id = mr_dataset_acquisition.mr_protocol_id)
    LEFT JOIN mr_protocol_metadata ON (mr_protocol_metadata.id = mr_protocol.id)
    LEFT JOIN studies.acquisition_equipment AS acquisition_equipment ON (acquisition_equipment.id = dataset_acquisition.acquisition_equipment_id)
    LEFT JOIN studies.manufacturer_model AS manufacturer_model ON (manufacturer_model.id = acquisition_equipment.manufacturer_model_id)
    LEFT JOIN studies.manufacturer AS manufacturer ON (manufacturer.id = manufacturer_model.manufacturer_id)
    LEFT JOIN examination ON (examination.id = dataset_acquisition.examination_id)
    LEFT JOIN studies.subject AS subject ON (subject.id = examination.subject_id)
    LEFT JOIN studies.study AS study ON (study.id = examination.study_id)
    LEFT JOIN studies.center AS center ON (center.id = examination.center_id)
    LEFT JOIN studies.pseudonymus_hash_values AS pseudonymus_hash_values ON (pseudonymus_hash_values.id = subject.pseudonymus_hash_values_id)

WHERE subject.name rlike if(subjectNameInRegExp IS NULL  OR subjectNameInRegExp = '', '.*', subjectNameInRegExp)
  AND subject.name NOT rlike if(subjectNameOutRegExp IS NULL  OR subjectNameOutRegExp = '', '^\b\B$', subjectNameOutRegExp)
  AND study.name rlike if(studyNameInRegExp IS NULL  OR studyNameInRegExp = '', '.*', studyNameInRegExp)
  AND study.name NOT rlike if(studyNameOutRegExp IS NULL  OR studyNameOutRegExp = '', '^\b\B$', studyNameOutRegExp)
  LIMIT blocSize OFFSET startRow;
END //

delimiter ;
