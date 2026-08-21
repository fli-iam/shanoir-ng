-- Shanoir NG - Import, manage and share neuroimaging data
-- Copyright (C) 2009-2025 Inria - https://www.inria.fr/
-- Contact us on https://project.inria.fr/shanoir/
-- 
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
-- 
-- You should have received a copy of the GNU General Public License
-- along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

USE datasets;

DROP PROCEDURE IF EXISTS computeOverallStatistics;

delimiter //
CREATE PROCEDURE computeOverallStatistics()
BEGIN
DELETE FROM overall_statistics WHERE stats_date = CURDATE();
INSERT into overall_statistics (stats_date, studies_count, subjects_count, dataset_acquisitions_count)
VALUES (CURDATE(),
    (SELECT COUNT(*) FROM study),
    (SELECT COUNT(*) FROM subject where study_id IS NOT NULL),
    (SELECT COUNT(DISTINCT dataset_acquisition_id) FROM dataset)
);
END //

delimiter ;

