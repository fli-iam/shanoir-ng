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

---- Datasets DB
-- Subject with null study but existing exam management
update datasets.subject as sub set sub.study_id = (select distinct e.study_id from datasets.examination e where subject_id = sub.id) where sub.study_id is null;

-- Dataset from those subject (the only one is the second processed_dataset ever)
DELIMITER //
begin -- As it only exists in OFSEP
    if exists (select 1 from datasets.subject s join datasets.dataset d on d.subject_id = s.id where d.id = 5602 and s.study_id is null) then
        delete from datasets.mr_dataset where id = 5602;
        delete from datasets.dataset_file where dataset_expression_id = 7283;
        delete from datasets.dataset_expression where dataset_id = 5602;
        delete from datasets.dataset where id = 5602;
        delete from datasets.dataset_metadata where id = 5602;
    end if
end//

DELIMITER ;
-- Subject with null study
delete from datasets.subject where study_id is null;

---- Studies DB
-- Subject with null study but existing exam management
update studies.subject as sub set sub.study_id = (select distinct e.study_id from studies.study_examination e where subject_id = sub.id) where sub.study_id is null;

--
-- Subject with null study
delete from studies.subject where study_id is null;