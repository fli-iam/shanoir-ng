SET sql_mode="NO_AUTO_VALUE_ON_ZERO"
INSERT INTO center (id, name) VALUES (0,'Unknown');
SET sql_mode=(SELECT REPLACE(@@sql_mode,'NO_AUTO_VALUE_ON_ZERO',''));