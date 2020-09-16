#!/bin/sh
basename="Export_"$(date +'%d-%m-%Y')
filename=$basename".txt"
dk-mysql ofsep-prod shanoirdb -e " (select 
sb.subject_id as patient_id, 
sb.name as shanoir_name, 
sb.subject_identifier as double_hash, 
pseud.birth_name_hash_1 as birthname1,
pseud.birth_name_hash_2 as birthname2,
pseud.birth_name_hash_3 as birthname3,
pseud.last_name_hash_1 as lastname1,
pseud.last_name_hash_2 as lastname2,
pseud.last_name_hash_3 as lastname3,
pseud.first_name_hash_1 as firstname1,
pseud.first_name_hash_2 as firstname2,
pseud.first_name_hash_3 as firstname3,
pseud.birth_date_hash as birthdate1,
(case sb.sex when 1 then 'M' else 'F' end) as sex,
year(sb.birth_date) as birth_year,
st.study_id as study_id,
st.name as study_name,
dt.dataset_id as sequence_id,
dt.name as norm_sequence_name, 
dt.comment as sequence_name,
cnt.center_id as center_id, 
cnt.name as center,
man.name as device_manufacturer,
man_mod.name as device_model,
man_mod.magnetic_field as device_field_strength,
ac_eq.serial_number as device_serial_number,
ex.examination_id as examination_id,
date(ex.examination_date) as examination_date,
pr_md.protocol_name as protocol_type

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
inner join studies.subject_study as rel_sb_st
inner join datasets.dataset_acquisition as dt_acq
inner join datasets.mr_dataset_acquisition as mr_acq

on sb.ref_sex_id = sex.ref_sex_id
and sb.pseudonymus_hash_values_id = pseud.id
and sb.subject_id = rel_sb_st.subject_id
and rel_sb_st.study_id = st.study_id
and sb.subject_id = ex.subject_id
and ex.center_id = cnt.center_id
and sb.subject_id = dt.subject_id
and dt.dataset_acquisition_id = dt_acq.dataset_acquisition_id
and dt_acq.acquisition_equipment_id = ac_eq. acquisition_equipment_id
and ac_eq.manufacturer_model_id = man_mod. manufacturer_model_id
and man_mod. manufacturer_id = man. manufacturer_id
and man_mod. manufacturer_model_id = man_mr. manufacturer_model_id
and man_mod. manufacturer_model_id = ac_eq. manufacturer_model_id
and dt_acq.dataset_acquisition_id = mr_acq.dataset_acquisition_id
and mr_acq.mr_protocol_id = pr.id
and pr_md.id = pr.id
and ex.examination_id = dt_acq. examination_id

where sb.name NOT LIKE 'TestDummyRun%'
and sb.name NOT LIKE 'test%'
and st.name LIKE 'Cohorte Principale%')

union

(select 
sb.subject_id as patient_id, 
sb.name as shanoir_name, 
sb.subject_identifier as double_hash, 
null as birthname1,
null as birthname2,
null as birthname3,
null as lastname1,
null as lastname2,
null as lastname3,
null as firstname1,
null as firstname2,
null as firstname3,
null as birthdate1 ,
sex.label_name as sex,
year(sb.birth_date ) as birth_year ,
st.study_id as study_id , 
st.name as study_name ,
dt.dataset_id as sequence_id ,  
dt.name as norm_sequence_name, 
dt.comment as sequence_name,
 cnt.center_id as center_id, 
cnt.name as center , 
man.name as device_manufacturer,
 man_mod.name as device_model , 
man_mr.magnetic_field as device_field_strength, 
ac_eq.serial_number as device_serial_number , 
ex.examination_id as examination_id , 
date(ex.examination_date) as examination_date , 
pr.protocol_name as protocol_type
from subject as sb
inner join ref_sex as sex
inner join study as st
inner join center as cnt
inner join manufacturer as man
inner join manufacturer_model as man_mod
inner join manufacturer_mr_model as man_mr
inner join acquisition_equipment as ac_eq
inner join mr_protocol as pr
inner join examination as ex
inner join dataset as dt


inner join rel_subject_study as rel_sb_st
inner join dataset_acquisition as dt_acq
inner join mr_dataset_acquisition as mr_acq


on sb.ref_sex_id= sex.ref_sex_id

and sb.subject_id = rel_sb_st.subject_id
and rel_sb_st.study_id=st.study_id

and sb.subject_id= ex.subject_id
and ex.center_id=cnt.center_id

and sb.subject_id= dt.subject_id
and dt.dataset_acquisition_id= dt_acq.dataset_acquisition_id
and dt_acq.acquisition_equipment_id= ac_eq. acquisition_equipment_id
and ac_eq.manufacturer_model_id= man_mod. manufacturer_model_id
and man_mod. manufacturer_id= man. manufacturer_id
and man_mod. manufacturer_model_id= man_mr. manufacturer_model_id
and man_mod. manufacturer_model_id= ac_eq. manufacturer_model_id

and dt_acq.dataset_acquisition_id= mr_acq.dataset_acquisition_id
and mr_acq.mr_protocol_id= pr.mr_protocol_id

and ex.examination_id= dt_acq. examination_id

where sb.name NOT LIKE 'TestDummyRun%'
and sb.name NOT LIKE 'test%'
and st.name LIKE 'CISCO%');

" > "$filename"

gpg --output "$basename"_Romain_Casey.txt.gpg --encrypt --recipient romain.casey01@chu-lyon.fr $filename
gpg --output "$basename"_Patrick_Grivot.txt.gpg --encrypt --recipient patrick.grivot@chu-lyon.fr $filename

