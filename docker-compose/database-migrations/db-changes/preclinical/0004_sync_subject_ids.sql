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


-- Copy animal_subject entries with id and subject_id shifted by 1000000
insert into animal_subject (biotype_id, id, provider_id, specie_id, stabulation_id, strain_id, subject_id)
    select biotype_id, id + 1000000, provider_id, specie_id, stabulation_id, strain_id, subject_id + 1000000 from animal_subject;
-- Update related tables to point to new animal_subject ids
update subject_pathology set animal_subject_id = animal_subject_id + 1000000;
update subject_therapy set animal_subject_id = animal_subject_id + 1000000;
-- Remove old animal_subject entries
delete from animal_subject where id < 1000000;
-- Copy back original animal_subject entries with original subject_id as id
insert into animal_subject (biotype_id, id, provider_id, specie_id, stabulation_id, strain_id, subject_id)
    select biotype_id, subject_id - 1000000, provider_id, specie_id, stabulation_id, strain_id, subject_id - 1000000 from animal_subject;
-- Update related tables to point to new animal_subject ids
update subject_pathology set animal_subject_id = (select subject_id - 1000000 from animal_subject where animal_subject.id = animal_subject_id);
update subject_therapy set animal_subject_id = (select subject_id - 1000000 from animal_subject where animal_subject.id = animal_subject_id);
-- Remove duplicated animal_subject entries
delete from animal_subject where id > 1000000;
-- Remove subject_id column (now the id is synced with subject.id)
alter table animal_subject drop subject_id;
