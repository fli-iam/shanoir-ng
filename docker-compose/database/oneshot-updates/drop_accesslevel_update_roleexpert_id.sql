ALTER TABLE role DROP COLUMN access_level;

UPDATE role SET id = 2 WHERE id = 4;