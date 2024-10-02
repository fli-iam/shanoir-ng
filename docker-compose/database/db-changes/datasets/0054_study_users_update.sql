INSERT INTO datasets.study_user
SELECT * FROM studies.study_user
ON DUPLICATE KEY UPDATE;