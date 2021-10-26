DELETE FROM subject_study_tag;
DELETE FROM subject_study;
DELETE FROM subject;
DELETE FROM tag;

INSERT INTO subject (id, name) VALUES (SELECT id, name FROM studies.subject);
INSERT INTO tag (id, name, color, study_id) (SELECT id, name, color, study_id FROM studies.tag);
INSERT INTO subject_study (id, subject_id, study_id) (SELECT id, subject_id, study_id FROM studies.subject_study);
INSERT INTO subject_study_tag (subject_study_id, tags_id) (SELECT subject_study_id, tags_id FROM studies.subject_study_tag);