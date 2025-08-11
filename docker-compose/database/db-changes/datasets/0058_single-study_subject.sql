# Extend subject with columns from subject_study
ALTER TABLE subject
    ADD COLUMN study_id BIGINT(20),
    ADD COLUMN subject_type INT(11),
    ADD COLUMN quality_tag INT(11),
    ADD CONSTRAINT unique_subject_name_study_id UNIQUE (name, study_id);


