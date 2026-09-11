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

-- dataset_property can accumulate one row per dataset_processing run for the same
-- dataset_id/name; keep only the row(s) with the greatest dataset_processing_id
-- per dataset_id, drop the rest.
--
-- dataset_property holds several million rows, so this is batched (small
-- transactions via a stored procedure loop) instead of one big DELETE, to avoid
-- long lock waits and a huge undo log.

-- 1. precompute the max dataset_processing_id per dataset_id once
DROP TABLE IF EXISTS dataset_property_max_dpi;
CREATE TABLE dataset_property_max_dpi (
    dataset_id bigint(20) NOT NULL PRIMARY KEY,
    max_dpi bigint(20) NOT NULL
);

INSERT INTO dataset_property_max_dpi (dataset_id, max_dpi)
SELECT dataset_id, MAX(dataset_processing_id)
FROM dataset_property
WHERE dataset_id IS NOT NULL
GROUP BY dataset_id;

-- 2. delete non-max rows in small batches until none remain
DROP PROCEDURE IF EXISTS clean_dataset_property_duplicates;

DELIMITER $$
CREATE PROCEDURE clean_dataset_property_duplicates()
BEGIN
    DECLARE batch_size INT DEFAULT 50000;
    DECLARE rows_deleted INT DEFAULT 1;

    WHILE rows_deleted > 0 DO
        DELETE FROM dataset_property
        WHERE id IN (
            SELECT id FROM (
                SELECT dp.id
                FROM dataset_property dp
                JOIN dataset_property_max_dpi m ON m.dataset_id = dp.dataset_id
                WHERE dp.dataset_processing_id < m.max_dpi
                   OR (dp.dataset_processing_id IS NULL AND m.max_dpi IS NOT NULL)
                LIMIT batch_size
            ) AS batch
        );
        SET rows_deleted = ROW_COUNT();
    END WHILE;
END$$
DELIMITER ;

CALL clean_dataset_property_duplicates();

-- 3. cleanup helper objects
DROP PROCEDURE clean_dataset_property_duplicates;
DROP TABLE dataset_property_max_dpi;
