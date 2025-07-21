# Single-study subject

DROP TABLE subject_group_of_subjects;

DROP TABLE group_of_subjects;

# Extend subject with columns from subject_study
ALTER TABLE subject
    ADD COLUMN study_id BIGINT(20),
    ADD COLUMN study_identifier VARCHAR(255),
    ADD COLUMN physically_involved BIT(1),
    ADD COLUMN subject_type INT(11),
    ADD COLUMN quality_tag INT(11);

# Copy columns from subject_study into subject
UPDATE subject s
JOIN subject_study ss ON s.id = ss.subject_id
SET
    s.study_id = ss.study_id,
    s.study_identifier = ss.subject_study_identifier,
    s.physically_involved = ss.physically_involved,
    s.subject_type = ss.subject_type,
    s.quality_tag = ss.quality_tag;
