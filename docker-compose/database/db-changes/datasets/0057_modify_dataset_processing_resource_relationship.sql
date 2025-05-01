-- Step 1: Create the join table for the many-to-many relationship
CREATE TABLE processing_resource_to_datasets (
                                              resource_id VARCHAR(255) NOT NULL,
                                              dataset_id BIGINT NOT NULL
);

-- Step 2: Migrate existing data from the old one-to-many relationship to the new join table
INSERT INTO processing_resource_to_datasets (resource_id, dataset_id)
SELECT resource_id, dataset_id
FROM processing_resource;

-- Step 3: Delete duplicated lines with same resource_id (no link with dataset lost, because saved in the step before)
CREATE TEMPORARY TABLE temp_processing_resource AS
SELECT min(id) as min_id FROM processing_resource GROUP BY resource_id;

DELETE FROM processing_resource
WHERE id NOT IN (SELECT min_id FROM temp_processing_resource);

DROP TEMPORARY TABLE temp_processing_resource;

-- Step 4: Move the primary key constraint to resource_id, remove id and dataset_id
ALTER TABLE processing_resource MODIFY COLUMN id BIGINT;  -- Remove auto-increment, needed for sql management
ALTER TABLE processing_resource DROP PRIMARY KEY;
ALTER TABLE processing_resource ADD PRIMARY KEY (resource_id);
ALTER TABLE processing_resource DROP COLUMN id;
ALTER TABLE processing_resource DROP FOREIGN KEY FK7971ykr4l94b3qgns4do405qe;
ALTER TABLE processing_resource DROP COLUMN dataset_id;


-- Step 5: Create new constraints for processing_resource_to_datasets table
ALTER TABLE processing_resource_to_datasets ADD PRIMARY KEY (resource_id, dataset_id);
ALTER TABLE processing_resource_to_datasets ADD FOREIGN KEY (resource_id) REFERENCES processing_resource(resource_id) ON DELETE CASCADE;
ALTER TABLE processing_resource_to_datasets ADD FOREIGN KEY (dataset_id) REFERENCES dataset(id) ON DELETE CASCADE;
