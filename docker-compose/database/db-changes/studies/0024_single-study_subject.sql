# Single-study subject SQL script
DROP TABLE subject_group_of_subjects;
DROP TABLE group_of_subjects;

CREATE TABLE subject_tag (
    subject_id bigint(20) NOT NULL,
    tag_id bigint(20) DEFAULT NULL,
    KEY FKe638djnyhwckgsob7qxnvcbyd (subject_id),
    KEY FKcc51xo4yrp74v2yp7df540a04 (tag_id),
    CONSTRAINT FKe638djnyhwckgsob7qxnvcbyd FOREIGN KEY (subject_id) REFERENCES subject (id),
    CONSTRAINT FKcc51xo4yrp74v2yp7df540a04 FOREIGN KEY (tag_id) REFERENCES tag (id)
);

# Remove unique constraint on subject name
ALTER TABLE subject DROP INDEX subject_name_idx;

# Extend subject with columns from subject_study
# subject study identifier will be removed later
# tested: all good
ALTER TABLE subject
    ADD COLUMN study_id BIGINT(20),
    ADD COLUMN study_identifier VARCHAR(255),
    ADD COLUMN physically_involved BIT(1) DEFAULT b'0',
    ADD COLUMN subject_type INT(11),
    ADD COLUMN quality_tag INT(11),
    ADD CONSTRAINT unique_subject_name_study_id UNIQUE (name, study_id);

# Update all existing subjects with values from subject study
# Copy columns from subject_study into new columns in subject
# One study id is set for the multi-study subjects
# tested: all good
UPDATE subject s
JOIN (
    SELECT
        subject_id,
        MAX(subject_study_identifier) AS subject_study_identifier,
        MAX(physically_involved) AS physically_involved,
        MAX(subject_type) AS subject_type,
        MAX(quality_tag) AS quality_tag,
        MAX(study_id) AS study_id
    FROM subject_study
    GROUP BY subject_id
) ss ON s.id = ss.subject_id
SET
    s.study_identifier = ss.subject_study_identifier,
    s.physically_involved = ss.physically_involved,
    s.subject_type = ss.subject_type,
    s.quality_tag = ss.quality_tag,
    s.study_id = ss.study_id;

# Add new subjects, in case of multi-study subjects and only for additional studies
# tested: all good
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
JOIN subject s ON ss.subject_id = s.id
LEFT JOIN subject existing
    ON existing.name = s.name AND existing.study_id = ss.study_id
WHERE existing.id IS NULL;

# Update subject study to keep only one row per subject
# tested: all good (corresponds to rows in above query)
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
    sst.tag_id
FROM 
    subject_study_tag sst
JOIN 
    subject_study ss ON sst.subject_study_id = ss.id;

# Add constraint to subject study to avoid new entries
# tested: all good
ALTER TABLE subject_study
ADD CONSTRAINT unique_subject_study UNIQUE (subject_id, study_id);

# Update study examination in ms studies after above changes
# tested: all good
UPDATE study_examination se
JOIN subject old_sub ON se.subject_id = old_sub.id
JOIN subject new_sub 
    ON new_sub.name = old_sub.name
   AND new_sub.study_id = se.study_id
   AND new_sub.id <> old_sub.id
SET se.subject_id = new_sub.id;

# Switch existing examinations to new subjects depending on study id
# tested: all good
UPDATE datasets.examination e
JOIN studies.subject old_sub ON e.subject_id = old_sub.id
JOIN studies.subject new_sub ON old_sub.name = new_sub.name AND e.study_id = new_sub.study_id
    AND old_sub.id <> new_sub.id
SET e.subject_id = new_sub.id;
