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

drop procedure if exists getStatistics;

delimiter //

create procedure getStatistics(IN studyNameInRegExp VARCHAR(255), IN studyNameOutRegExp VARCHAR(255), IN subjectNameInRegExp VARCHAR(255), IN subjectNameOutRegExp VARCHAR(255))
begin
    select 'patient_id', 'shanoir_name', 'double_hash', 'birthname1', 'birthname2', 'birthname3', 'lastname1', 'lastname2', 'lastname3', 'firstname1', 'firstname2', 'firstname3', 'birthdate1', 'sex', 'birth_year', 'study_id', 'study_name', 'sequence_id', 'norm_sequence_name', 'sequence_name', 'center_id', 'center', 'device_manufacturer', 'device_model', 'device_field_strength', 'device_serial_number', 'examination_id', 'examination_date', 'import_date', 'creation_date', 'series_number', 'protocol_type', 'dicom_size_mo'
    union all
    select 
        subject.id as patient_id,
        subject.name as shanoir_name,
        subject.identifier as double_hash,
        pseudonymus_hash_values.birth_name_hash1 as birthname1,
        pseudonymus_hash_values.birth_name_hash2 as birthname2,
        pseudonymus_hash_values.birth_name_hash3 as birthname3,
        pseudonymus_hash_values.last_name_hash1 as lastname1,
        pseudonymus_hash_values.last_name_hash2 as lastname2,
        pseudonymus_hash_values.last_name_hash3 as lastname3,
        pseudonymus_hash_values.first_name_hash1 as firstname1,
        pseudonymus_hash_values.first_name_hash2 as firstname2,
        pseudonymus_hash_values.first_name_hash3 as firstname3,
        pseudonymus_hash_values.birth_date_hash as birthdate1,
        (case subject.sex when 1 then 'M' else 'F' end) as sex,
        year(subject.birth_date) as birth_year,
        study.id as study_id,
        study.name as study_name,
        dataset.id as sequence_id,
        dataset_metadata.name as norm_sequence_name,
        dataset_metadata.comment as sequence_name,
        center.id as center_id,
        center.name as center,
        manufacturer.name as device_manufacturer,
        manufacturer_model.name as device_model,
        manufacturer_model.magnetic_field as device_field_strength,
        acquisition_equipment.serial_number as device_serial_number,
        examination.id as examination_id,
        date(examination.examination_date) as examination_date,
        date(dataset_acquisition.creation_date) as import_date,
        date(dataset.creation_date) as creation_date,
        dataset_acquisition.sorting_index as series_number,
        mr_protocol_metadata.name as protocol_type,
        (select sum(size)/1000000 from dataset_expression as de where de.dataset_id = dataset.id and de.dataset_expression_format = 6) as dicom_size_mo

    from dataset 
        left join dataset_acquisition on (dataset_acquisition.id = dataset.dataset_acquisition_id)
        left join dataset_metadata on (dataset_metadata.id = dataset.updated_metadata_id)
        left join mr_dataset_acquisition on (mr_dataset_acquisition.id = dataset_acquisition.id)
        left join mr_protocol on (mr_protocol.id = mr_dataset_acquisition.mr_protocol_id)
        left join mr_protocol_metadata on (mr_protocol_metadata.id = mr_protocol.id)
        left join studies.acquisition_equipment as acquisition_equipment on (acquisition_equipment.id = dataset_acquisition.acquisition_equipment_id)
        left join studies.manufacturer_model as manufacturer_model on (manufacturer_model.id = acquisition_equipment.manufacturer_model_id)
        left join studies.manufacturer as manufacturer on (manufacturer.id = manufacturer_model.manufacturer_id)
        left join examination on (examination.id = dataset_acquisition.examination_id)
        left join studies.subject as subject on (subject.id = examination.subject_id)
        left join studies.study as study on (study.id = examination.study_id)
        left join studies.center as center on (center.id = examination.center_id)
        left join studies.pseudonymus_hash_values as pseudonymus_hash_values on (pseudonymus_hash_values.id = subject.pseudonymus_hash_values_id)

    where subject.name rlike if(subjectNameInRegExp is null or subjectNameInRegExp = '', '.*', subjectNameInRegExp)
        and subject.name not rlike if(subjectNameOutRegExp is null or subjectNameOutRegExp = '', '^\b\B$', subjectNameOutRegExp)
        and study.name rlike if(studyNameInRegExp is null or studyNameInRegExp = '', '.*', studyNameInRegExp)
        and study.name not rlike if(studyNameOutRegExp is null or studyNameOutRegExp = '', '^\b\B$', studyNameOutRegExp);
end //

delimiter ;
