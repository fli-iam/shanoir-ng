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

-- 1. Remove old properties (a new SIMS has occurred after those properties)
DROP TABLE IF EXISTS dataset_property_max_dpi;
CREATE TABLE dataset_property_max_dpi (
    dataset_id bigint(20) NOT NULL PRIMARY KEY,
    max_dpi bigint(20) NOT NULL
);

INSERT INTO dataset_property_max_dpi (dataset_id, max_dpi)
SELECT dataset_id, MAX(dataset_processing_id)
FROM dataset_property
WHERE dataset_id IS NOT NULL
GROUP BY dataset_id
HAVING MAX(dataset_processing_id) IS NOT NULL
   AND (MIN(dataset_processing_id) < MAX(dataset_processing_id)
        OR COUNT(*) > COUNT(dataset_processing_id));

DROP PROCEDURE IF EXISTS clean_old_dataset_property;

DELIMITER $$
CREATE PROCEDURE clean_old_dataset_property()
BEGIN
    DECLARE batch_size BIGINT DEFAULT 50000;
    DECLARE pointer_id BIGINT;
    DECLARE last_id BIGINT;

    SELECT MIN(id), MAX(id) INTO pointer_id, last_id FROM dataset_property;

    WHILE pointer_id <= last_id DO
        DELETE dp
        FROM dataset_property dp
        JOIN dataset_property_max_dpi m ON m.dataset_id = dp.dataset_id
        WHERE dp.id >= pointer_id AND dp.id < pointer_id + batch_size
          AND (dp.dataset_processing_id < m.max_dpi
               OR dp.dataset_processing_id IS NULL);
        SET pointer_id = pointer_id + batch_size;
    END WHILE;
END$$
DELIMITER ;

CALL clean_old_dataset_property();

DROP PROCEDURE clean_old_dataset_property;
DROP TABLE dataset_property_max_dpi;

-- 1. Remove duplicates (an output.json which have been processed multiple times)

CREATE TABLE duplicate_dataset_property (
    dataset_id bigint(20) NOT NULL PRIMARY KEY
);


INSERT INTO duplicate_dataset_property (dataset_id)
SELECT dataset_id
FROM dataset_property
WHERE dataset_id IS NOT NULL
GROUP BY dataset_id
HAVING COUNT(name) > COUNT(DISTINCT name);

DROP PROCEDURE IF EXISTS clean_dataset_property_duplicates;

DELIMITER $$
CREATE PROCEDURE clean_dataset_property_duplicates()
BEGIN
    DECLARE batch_size BIGINT DEFAULT 50000;
    DECLARE pointer_id BIGINT;
    DECLARE last_id BIGINT;

SELECT MIN(id), MAX(id) INTO pointer_id, last_id FROM dataset_property;

WHILE pointer_id <= last_id DO
        DELETE dp FROM dataset_property dp
        WHERE dp.id >= pointer_id AND dp.id < pointer_id + batch_size
        AND dp.dataset_id IN (SELECT dataset_id FROM duplicate_dataset_property);

        SET pointer_id = pointer_id + batch_size;
END WHILE;
END$$
DELIMITER ;

CALL clean_dataset_property_duplicates();

DROP PROCEDURE clean_dataset_property_duplicates;
DROP TABLE duplicate_dataset_property;