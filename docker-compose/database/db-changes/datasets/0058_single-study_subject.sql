CREATE TABLE subject_tag (
    subject_id bigint(20) NOT NULL,
    tag_id bigint(20) DEFAULT NULL,
    KEY FKe638djnyhwckgsob7qxnvcbyd (subject_id),
    KEY FKcc51xo4yrp74v2yp7df540a04 (tag_id),
    CONSTRAINT FKe638djnyhwckgsob7qxnvcbyd FOREIGN KEY (subject_id) REFERENCES subject (id)
    CONSTRAINT FKcc51xo4yrp74v2yp7df540a04 FOREIGN KEY (tag_id) REFERENCES tag (id),
);

# Remove unique constraint on subject name
ALTER TABLE subject DROP INDEX subject_name_idx;

# Extend subject with columns from subject_study
ALTER TABLE subject
    ADD COLUMN study_id BIGINT(20),
    ADD COLUMN subject_type INT(11),
    ADD COLUMN quality_tag INT(11),
    ADD CONSTRAINT unique_subject_name_study_id UNIQUE (name, study_id);

# Update all existing subjects with values from subject study
# Copy columns from subject_study into new columns in subject
# One study id is set for the multi-study subjects
UPDATE subject s
JOIN (
    SELECT
        subject_id,
        MAX(subject_type) AS subject_type,
        MAX(quality_tag) AS quality_tag,
        MAX(study_id) AS study_id
    FROM subject_study
    GROUP BY subject_id
) ss ON s.id = ss.subject_id
SET
    s.subject_type = ss.subject_type,
    s.quality_tag = ss.quality_tag,
    s.study_id = ss.study_id;


