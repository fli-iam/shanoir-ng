INSERT IGNORE INTO datasets.study_user
       (id, receive_study_user_report, receive_new_import_report, study_id, user_id, user_name, confirmed)
SELECT id, receive_study_user_report, receive_new_import_report, study_id, user_id, user_name, confirmed
FROM studies.study_user;