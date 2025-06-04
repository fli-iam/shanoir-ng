-- Before:
-- BDD | Enum | Valeur
-- ____|______|_______
--  0  |  1   | Mandatory
--  1  |  2   | Optional
--  2  |  3   | Disabled
-- 
-- After:
-- BDD | Enum | Valeur
-- ____|______|_______
--  0  |  1   | Mandatory
--  1  |  2   | Disabled
-- 
-- Remove Optional and update study with those value to Mandatory
-- Update former values of Disabled to prevent index out of bounds exception

UPDATE study SET study_card_policy=0 WHERE study_card_policy=1;

UPDATE study SET study_card_policy=1 WHERE study_card_policy=2;