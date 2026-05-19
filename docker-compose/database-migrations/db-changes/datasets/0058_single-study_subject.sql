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
