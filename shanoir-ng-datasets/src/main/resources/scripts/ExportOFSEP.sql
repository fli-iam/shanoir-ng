select 
sb.id as patient_id, 
sb.name as shanoir_name, 
sb.identifier as double_hash, 
-- pseud.birth_name_hash1 as birthname1,
-- pseud.birth_name_hash2 as birthname2,
-- pseud.birth_name_hash3 as birthname3,
-- pseud.last_name_hash1 as lastname1,
-- pseud.last_name_hash2 as lastname2,
-- pseud.last_name_hash3 as lastname3,
-- pseud.first_name_hash1 as firstname1,
-- pseud.first_name_hash2 as firstname2,
-- pseud.first_name_hash3 as firstname3,
-- pseud.birth_date_hash as birthdate1,
(case sb.sex when 1 then 'M' else 'F' end) as sex,
year(sb.birth_date) as birth_year,
st.id as study_id,
st.name as study_name,
dt.id as sequence_id,
-- dt_md.name as norm_sequence_name, 
-- dt_md.comment as sequence_name,
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
inner join studies.pseudonymus_hash_values as pseud
inner join studies.study as st
inner join studies.center as cnt
inner join studies.manufacturer as man
inner join studies.manufacturer_model as man_mod
inner join studies.acquisition_equipment as ac_eq
inner join datasets.mr_protocol as pr
inner join datasets.mr_protocol_metadata as pr_md
inner join datasets.examination as ex
inner join datasets.dataset as dt
-- inner join datasets.dataset_metadata as dt_md
inner join studies.subject_study as rel_sb_st
inner join datasets.dataset_acquisition as dt_acq
inner join datasets.mr_dataset_acquisition as mr_acq

on sb.pseudonymus_hash_values_id = pseud.id
and sb.id = rel_sb_st.subject_id
and rel_sb_st.study_id = st.id
and sb.id = ex.subject_id
and ex.center_id = cnt.id
and sb.id = dt.subject_id
and dt.dataset_acquisition_id = dt_acq.id
-- and dt_md.id = dt.updated_metadata_id
and dt_acq.acquisition_equipment_id = ac_eq.id
and ac_eq.manufacturer_model_id = man_mod.id
and man_mod.manufacturer_id = man.id
and dt_acq.id = mr_acq.id
and mr_acq.mr_protocol_id = pr.id
and pr_md.id = pr.id
and ex.id = dt_acq.examination_id
;