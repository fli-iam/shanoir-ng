ALTER TABLE role DROP COLUMN access_level;

UPDATE role SET display_name = 'Expert_tmp', name = 'ROLE_EXPERT_TMP' WHERE id = 4;

INSERT INTO role (id, display_name, name) VALUES (2, 'Expert', 'ROLE_EXPERT');

UPDATE users SET role_id = 2 WHERE role_id = 4;

DELETE FROM role WHERE id = 4;
