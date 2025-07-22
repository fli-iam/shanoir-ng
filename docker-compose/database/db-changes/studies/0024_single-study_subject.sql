# Single-study subject

DROP TABLE subject_group_of_subjects;

DROP TABLE group_of_subjects;

# Extend subject with columns from subject_study
ALTER TABLE subject
    ADD COLUMN study_id BIGINT(20),
    ADD COLUMN study_identifier VARCHAR(255),
    ADD COLUMN physically_involved BIT(1),
    ADD COLUMN subject_type INT(11),
    ADD COLUMN quality_tag INT(11)
    ADD CONSTRAINT unique_subject_name_study_id UNIQUE (name, study_id);;

# Copy columns from subject_study into all subjects (single-study subject already)
UPDATE subject s
JOIN (
    SELECT subject_id,
           MAX(subject_study_identifier) AS subject_study_identifier,
           MAX(physically_involved) AS physically_involved,
           MAX(subject_type) AS subject_type,
           MAX(quality_tag) AS quality_tag,
           MAX(study_id) AS study_id
    FROM subject_study ss
    GROUP BY subject_id
) ss ON s.id = ss.subject_id
SET
    s.study_identifier = ss.subject_study_identifier,
    s.physically_involved = ss.physically_involved,
    s.subject_type = ss.subject_type,
    s.quality_tag = ss.quality_tag,
    s.study_id = ss.study_id;

# Add new subject, in case multi-study subject and only for additional studies
INSERT INTO subject (
    birth_date,
    identifier,
    imaged_object_category,
    language_hemispheric_dominance,
    manual_hemispheric_dominance,
    name,
    preclinical,
    sex,
    pseudonymus_hash_values_id,
    study_identifier,
    physically_involved,
    subject_type,
    quality_tag,
    study_id
)
SELECT
    s.birth_date,
    s.identifier,
    s.imaged_object_category,
    s.language_hemispheric_dominance,
    s.manual_hemispheric_dominance,
    s.name,
    s.preclinical,
    s.sex,
    s.pseudonymus_hash_values_id,
    ss.subject_study_identifier,
    ss.physically_involved,
    ss.subject_type,
    ss.quality_tag,
    ss.study_id
FROM subject_study ss
JOIN (
    SELECT subject_id
    FROM subject_study
    GROUP BY subject_id
    HAVING COUNT(*) > 1
) multi ON ss.subject_id = multi.subject_id
JOIN subject s ON ss.subject_id = s.id
LEFT JOIN subject existing ON existing.name = s.name AND existing.study_id = ss.study_id
WHERE existing.id IS NULL;

