SET sql_mode="NO_AUTO_VALUE_ON_ZERO";
INSERT INTO center (id, name) VALUES (0,'Unknown');
INSERT INTO manufacturer (id, name) values (0, 'Unknown');
INSERT INTO manufacturer_model (id, dataset_modality_type, name, manufacturer_id) values (0,1,'Unknown',0);
INSERT INTO acquisition_equipment (id, center_id, manufacturer_model_id) VALUES (0,0,0);
SET sql_mode=(SELECT REPLACE(@@sql_mode,'NO_AUTO_VALUE_ON_ZERO',''));