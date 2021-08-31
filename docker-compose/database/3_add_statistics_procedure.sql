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

delimiter //

create procedure getStatistics(IN studyNameInRegExp VARCHAR(255), IN studyNameOutRegExp VARCHAR(255), IN subjectNameInRegExp VARCHAR(255), IN subjectNameOutRegExp VARCHAR(255))
begin
    select 'patient_id', 'shanoir_name', 'double_hash', 'birthname1', 'birthname2', 'birthname3', 'lastname1', 'lastname2', 'lastname3', 'firstname1', 'firstname2', 'firstname3', 'birthdate1', 'sex', 'birth_year', 'study_id', 'study_name', 'sequence_id', 'norm_sequence_name', 'sequence_name', 'center_id', 'center', 'device_manufacturer', 'device_model', 'device_field_strength', 'device_serial_number', 'examination_id', 'examination_date', 'protocol_type'
    union all
    select distinct
    sb.id as patient_id, 
    sb.name as shanoir_name, 
    sb.identifier as double_hash, 
    pseud.birth_name_hash1 as birthname1,
    pseud.birth_name_hash2 as birthname2,
    pseud.birth_name_hash3 as birthname3,
    pseud.last_name_hash1 as lastname1,
    pseud.last_name_hash2 as lastname2,
    pseud.last_name_hash3 as lastname3,
    pseud.first_name_hash1 as firstname1,
    pseud.first_name_hash2 as firstname2,
    pseud.first_name_hash3 as firstname3,
    pseud.birth_date_hash as birthdate1,
    (case sb.sex when 1 then 'M' else 'F' end) as sex,
    year(sb.birth_date) as birth_year,
    st.id as study_id,
    st.name as study_name,
    dt.id as sequence_id,
    dt_md.name as norm_sequence_name, 
    dt_md.comment as sequence_name,
    cnt.id as center_id, 
    cnt.name as center,
    man.name as device_manufacturer,
    man_mod.name as device_model,
    man_mod.magnetic_field as device_field_strength,
    ac_eq.serial_number as device_serial_number,
    ex.id as examination_id,
    date(ex.examination_date) as examination_date,
    pr_md.name as protocol_type

    from studies.subject as sb
    left join studies.pseudonymus_hash_values as pseud on (pseud.id = sb.pseudonymus_hash_values_id)
    inner join studies.study as st
    inner join studies.center as cnt
    inner join studies.manufacturer as man
    inner join studies.manufacturer_model as man_mod
    inner join studies.acquisition_equipment as ac_eq
    inner join datasets.mr_protocol as pr
    inner join datasets.mr_protocol_metadata as pr_md
    inner join datasets.examination as ex
    inner join datasets.dataset as dt
    inner join datasets.dataset_metadata as dt_md
    inner join datasets.dataset_acquisition as dt_acq
    inner join datasets.mr_dataset_acquisition as mr_acq

    on st.id = ex.study_id
    and sb.id = ex.subject_id
    and ex.center_id = cnt.id
    and sb.id = dt.subject_id
    and dt.dataset_acquisition_id = dt_acq.id
    and dt_md.id = dt.updated_metadata_id
    and dt_acq.acquisition_equipment_id = ac_eq.id
    and ac_eq.manufacturer_model_id = man_mod.id
    and man_mod.manufacturer_id = man.id
    and dt_acq.id = mr_acq.id
    and mr_acq.mr_protocol_id = pr.id
    and pr_md.id = pr.id
    and ex.id = dt_acq.examination_id
    and sb.name rlike if(subjectNameInRegExp is null or subjectNameInRegExp = '', '.*', subjectNameInRegExp)
    and sb.name not rlike if(subjectNameOutRegExp is null or subjectNameOutRegExp = '', '^\b\B$', subjectNameOutRegExp)
    and st.name rlike if(studyNameInRegExp is null or studyNameInRegExp = '', '.*', studyNameInRegExp)
    and st.name not rlike if(studyNameOutRegExp is null or studyNameOutRegExp = '', '^\b\B$', studyNameOutRegExp);
end //

delimiter ;
