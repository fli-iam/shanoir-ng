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
-- along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.htm

-------- Datasets DB
-- Removing stuff related to examinations without subject_id or subject without study_id
-- I'm deleting because from what I see, the related dataset/acquisition below are kinda broken
-- (and in all cases, those data were difficulty accessible)
create temporary table garbage_datasets as (select d.id from datasets.dataset d join datasets.dataset_acquisition acq on d.dataset_acquisition_id = acq.id join datasets.examination e on e.id = acq.examination_id where e.subject_id is null); -- 2 on neurinfo / 0 on ofsep
insert into garbage_datasets (id) select  id from dataset where subject_id is null; -- 3 new on neurinfo / 0 on ofsep
create temporary table duplicate_subjects as (select sub.id from datasets.subject sub join datasets.examination e on e.subject_id = sub.id where sub.study_id is null and e.study_id is not null and exists (select 1 from datasets.subject where name = sub.name and study_id = e.study_id)); -- 27 on neurinfo / 0 on ofsep
insert into garbage_datasets (id) select id from datasets.dataset where subject_id in (select id from duplicate_subjects); -- 2 on neurinfo / 0 on ofsep

delete from datasets.flip_angle where mr_dataset_id in (select id from garbage_datasets); -- 1 on neurinfo / 0 on ofsep
delete from datasets.echo_time where mr_dataset_id in (select id from garbage_datasets); -- 1 on neurinfo / 0 on ofsep
delete from datasets.repetition_time where mr_dataset_id in (select id from garbage_datasets); -- 1 on neurinfo / 0 on ofsep
delete from datasets.mr_dataset where id in (select id from garbage_datasets); -- 3 on neurinfo / 0 on ofsep
delete from datasets.dataset_file where dataset_expression_id in (select id from dataset_expression where dataset_id in (select id from garbage_datasets)); -- 296 on neurinfo / 0 on ofsep
delete from datasets.dataset_expression where original_dataset_expression_id in (select id from datasets.dataset_expression where dataset_id in (select id from garbage_datasets)); -- 1 on neurinfo / 0 on ofsep
delete from datasets.dataset_expression where dataset_id in (select id from garbage_datasets); -- 7 on neurinfo / 0 on ofsep
delete from datasets.ct_dataset where id in (select id from garbage_datasets); -- 3 on neurinfo / 0 on ofsep
delete from datasets.processing_resource where dataset_id in (select id from garbage_datasets); -- 2 on neurinfo / 0 on ofsep
delete from datasets.input_of_dataset_processing where dataset_id in (select id from garbage_datasets); -- 3 on neurinfo / 0 on ofsep
delete from datasets.dataset where id in (select id from garbage_datasets); -- 6 on neurinfo / 0 on ofsep
delete from datasets.dataset_metadata where id in (select origin_metadata_id from datasets.dataset d where d.id in (select * from garbage_datasets)); -- 0 on neurinfo / 0 on ofsep
delete from datasets.dataset_metadata where id in (select updated_metadata_id from datasets.dataset d where d.id in (select * from garbage_datasets)); -- 0 on neurinfo / 0 on ofsep

create temporary table garbage_acquisitions as (select acq.id from datasets.dataset_acquisition acq join datasets.examination e on e.id = acq.examination_id where e.subject_id is null or not exists (select 1 from datasets.dataset d where d.dataset_acquisition_id = acq.id)); -- 8997 on neurinfo / 59664 on ofsep (normal, related to burn in annotation importation failure)

delete from datasets.mr_dataset_acquisition where id in (select id from garbage_acquisitions); -- 6734 on neurinfo / 59477 on ofsep
delete from datasets.ct_dataset_acquisition where id in (select id from garbage_acquisitions); -- 2230 on neurinfo / 4 on ofsep
delete from datasets.pet_dataset_acquisition where id in (select id from garbage_acquisitions); -- 16 on neurinfo / 0 on ofsep
delete from datasets.generic_dataset_acquisition where id in (select id from garbage_acquisitions); -- 3 on neurinfo / 183 on ofsep
delete from datasets.xa_dataset_acquisition where id in (select id from garbage_acquisitions); -- 14 on neurinfo / 0 on ofsep
delete from datasets.dataset_acquisition where id in (select id from garbage_acquisitions); -- 8997 on neurinfo / 59664 on ofsep

create temporary table garbage_examinations as (select e.id from examination e where e.subject_id is null or not exists (select 1 from datasets.dataset_acquisition acq where acq.examination_id = e.id) or e.subject_id in (select id from duplicate_subjects)); -- 1517 on neurinfo / 4324 on ofsep

delete from datasets.extra_data_file_path where examination_id in (select id from garbage_examinations); -- 159 on neurinfo / 2 on ofsep
delete from datasets.examination where id in (select id from garbage_examinations); -- 1517 on neurinfo / 4324 on ofsep

delete from datasets.subject where id in (select id from duplicate_subjects); -- 4 on neurinfo / 0 on ofsep
delete s from datasets.subject s where not exists (select 1 from examination e where e.subject_id = s.id); -- 1417 on neurinfo / 1501 on ofsep

-- Saving the subjects which have datasets related to them that do not look like duplicates
update datasets.subject as sub set sub.study_id = (select distinct e.study_id from datasets.examination e where subject_id = sub.id) where sub.study_id is null; -- 19 on neurinfo / 17 on ofsep

-- Creating constraints in DB
alter table datasets.examination modify column subject_id BIGINT(20) not NULL;
alter table datasets.subject modify column study_id BIGINT(20) not NULL;

-------- Studies DB
-- Correcting unconsistency between studies.study_examination.subject_id and datasets.examination.subject_id
-- When comparing both sides, we clearly see that's the only difference between both sides
-- And as the tree displaying is based on datasets.examination, we take it as base for the fix
update studies.study_examination se set se.subject_id = (select e.subject_id from datasets.examination e where e.id = se.examination_id) where se.subject_id != (select e.subject_id from datasets.examination e where e.id = se.examination_id); -- 1 on neurinfo / 9 on ofsp

-- Removing stuff
create temporary table duplicate_subjects_bis as (select sub.id from studies.subject sub join studies.study_examination e on e.subject_id = sub.id where sub.study_id is null and e.study_id is not null and exists (select 1 from studies.subject where name = sub.name and study_id = e.study_id)); -- 0 on neurinfo / 0 on ofsep

delete from studies.study_examination where subject_id is null; -- 1 on neurinfo / 0 on ofsep
delete e from studies.study_examination e where not exists (select 1 from datasets.examination exam where e.examination_id = exam.id); -- 1468 on neurinfo / 4299 on ofsep

delete ss from studies.subject_study ss where subject_id in (select id from duplicate_subjects_bis) or not exists (select 1 from datasets.subject sub where sub.id = ss.subject_id); -- 1242 on neurinfo / 1467 on ofsep
delete from studies.subject where id in (select id from duplicate_subjects_bis); -- 0 on neurinfo / 0 on ofsep
delete s from studies.subject s where not exists (select 1 from datasets.subject sub where sub.id = s.id); -- 1366 on neurinfo / 1504 on ofsep

-- Subject with null study but existing exam management
update studies.subject as sub set sub.study_id = (select distinct e.study_id from studies.study_examination e where subject_id = sub.id) where sub.study_id is null; -- 18 on neurinfo / 17 on ofsep

-- Creating constraints in DB
alter table studies.subject modify column study_id BIGINT(20) not NULL;
