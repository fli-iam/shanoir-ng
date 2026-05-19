-- Shanoir NG - Import, manage and share neuroimaging data
-- Copyright (C) 2009-2019 Inria - https://www.inria.fr/
-- Contact us on https://project.inria.fr/shanoir/
-- 
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
-- 
-- You should have received a copy of the GNU General Public License
-- along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

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