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

select
	s.PROCESSING_SCORE_VALUE_ID as SCORE_ID,
    s.SCORE_VALUE,
    v.PROCESSING_SCORE_VARIABLE_ID as METRIC_ID,
    v.LABEL as METRIC_NAME,
    v.NAN,
    v.PINF,
    v.NINF,
    u.USER_ID,
    CONCAT_WS(' ', u.FIRST_NAME, u.LAST_NAME) as USER_NAME,
    su.SUBJECT_ID,
    su.NAME as SUBJECT_NAME,
    p.STUDY_ID,
    st.NAME as STUDY_NAME,
    d0.DATASET_ID as OUTPUT_DATASET_ID,
    i1.DATASET_ID as INPUT_DATASET_ID
from
	processing_score_value as s,
    processing_score_variable as v,
    users as u,
    rel_user_dataset_processing as r,
    dataset_processing as p,
    study as st,
    rel_inputof_dataset_processing as i1,
    subject as su,
    dataset as d0,
    dataset as d
where s.PROCESSING_SCORE_VARIABLE_ID = v.PROCESSING_SCORE_VARIABLE_ID
and s.DATASET_PROCESSING_ID = r.DATASET_PROCESSING_ID
and r.USER_ID = u.USER_ID
and r.DATASET_PROCESSING_ID = p.DATASET_PROCESSING_ID
and p.STUDY_ID = st.STUDY_ID
and i1.DATASET_PROCESSING_ID = p.DATASET_PROCESSING_ID
and i1.DATASET_ID = d.DATASET_ID
and d.SUBJECT_ID = su.SUBJECT_ID
and d0.DATASET_PROCESSING_ID = p.DATASET_PROCESSING_ID;