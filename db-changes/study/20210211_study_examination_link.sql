# The idea is to re-create the study_examination table from scratch
DELETE * FROM study_examination;
INSERT INTO study_examination (study_id, examination_id) SELECT study_id, id from datasets.examination;