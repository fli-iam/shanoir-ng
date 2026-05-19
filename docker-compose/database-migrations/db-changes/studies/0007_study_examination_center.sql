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

# Drop old study_examination table
DELETE FROM study_examination;
DROP TABLE study_examination;

# Create new table with new fields
CREATE TABLE `study_examination` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `examination_id` bigint(20) DEFAULT NULL,
  `center_id` bigint(20) DEFAULT NULL,
  `study_id` bigint(20) DEFAULT NULL,
  `subject_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `FKr0jikjmvsarpch5rtg9te0fys` (`center_id`),
  KEY `FKlbokvx0u8921ujhfyh1751ssl` (`study_id`),
  KEY `FKcwyf6h6f95500k613lau1uxcf` (`subject_id`),
  CONSTRAINT `FKcwyf6h6f95500k613lau1uxcf` FOREIGN KEY (`subject_id`) REFERENCES `subject` (`id`),
  CONSTRAINT `FKlbokvx0u8921ujhfyh1751ssl` FOREIGN KEY (`study_id`) REFERENCES `study` (`id`),
  CONSTRAINT `FKr0jikjmvsarpch5rtg9te0fys` FOREIGN KEY (`center_id`) REFERENCES `center` (`id`)
);

# Clean data: exams with missing center_ids
UPDATE datasets.examination SET center_id = 0 WHERE center_id NOT IN (SELECT id FROM center);

# Fill it with data
INSERT INTO study_examination (study_id, examination_id, center_id, subject_id) SELECT study_id, id, center_id, subject_id from datasets.examination;
