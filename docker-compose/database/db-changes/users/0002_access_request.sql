ALTER TABLE account_request_info DROP COLUMN study;
ALTER TABLE account_request_info DROP COLUMN work;
ALTER TABLE account_request_info DROP COLUMN service;
ALTER TABLE account_request_info DROP COLUMN challenge;
ALTER TABLE account_request_info ADD COLUMN study_id bigint(20);
ALTER TABLE account_request_info ADD COLUMN study_name varchar(255);
