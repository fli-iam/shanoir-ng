DROP TABLE IF EXISTS subject_study_tag;
UPDATE subject s INNER JOIN subject_study ss ON ss.subject_id = s.id SET s.study_id = ss.study_id WHERE s.study_id IS NULL;
DROP TABLE IF EXISTS subject_study;
