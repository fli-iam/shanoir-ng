delete from datasets.study_user;

INSERT INTO datasets.study_user
       (id, receive_study_user_report, receive_new_import_report, study_id, user_id, user_name, confirmed)
SELECT id, receive_study_user_report, receive_new_import_report, study_id, user_id, user_name, confirmed
FROM studies.study_user ssu;

delete from datasets.study_user_study_user_rights;

INSERT INTO datasets.study_user_study_user_rights
(study_user_id, study_user_rights)
SELECT study_user_id, study_user_rights
FROM studies.study_user_study_user_rights;