CREATE TABLE subject_tag (
    subject_id bigint(20) NOT NULL,
    tag_id bigint(20) DEFAULT NULL,
    KEY FKe638djnyhwckgsob7qxnvcbyd (subject_id),
    KEY FKcc51xo4yrp74v2yp7df540a04 (tag_id),
    CONSTRAINT FKe638djnyhwckgsob7qxnvcbyd FOREIGN KEY (subject_id) REFERENCES subject(id),
    CONSTRAINT FKcc51xo4yrp74v2yp7df540a04 FOREIGN KEY (tag_id) REFERENCES tag(id)
);

# Extend subject with columns from subject_study
ALTER TABLE subject
    ADD COLUMN study_id BIGINT(20) NOT NULL,
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

# Add new subjects, in case of multi-study subjects and only for additional studies
INSERT INTO subject (
    id,
    name,
    subject_type,
    quality_tag,
    study_id
)
SELECT
    s.id,
    s.name,
    ss.subject_type,
    ss.quality_tag,
    ss.study_id
FROM subject_study ss
JOIN subject s ON ss.subject_id = s.id
LEFT JOIN subject existing
    ON existing.name = s.name AND existing.study_id = ss.study_id
WHERE existing.id IS NULL;

# Update subject study to keep only one row per subject
UPDATE subject_study ss
JOIN subject old_sub ON ss.subject_id = old_sub.id
JOIN subject new_sub ON old_sub.name = new_sub.name
    AND ss.study_id = new_sub.study_id
    AND old_sub.id <> new_sub.id
SET ss.subject_id = new_sub.id;

# Migrate subject_study_tags to subject_tag
# Works as subject_ids have already be aligned before
INSERT INTO subject_tag (subject_id, tag_id)
SELECT 
    ss.subject_id,
    sst.tags_id
FROM 
    subject_study_tag sst
JOIN 
    subject_study ss ON sst.subject_study_id = ss.id;

DROP TABLE subject_study_tag;

DROP TABLE subject_study;
