CREATE TABLE `0053_temp` (
      `processing_id` bigint(20),
      `acquisition_id` bigint(20),
      INDEX `0053_temp_processing_index` (`processing_id`)
);

INSERT INTO 0053_temp (processing_id, acquisition_id)
SELECT DISTINCT input.processing_id, ds.dataset_acquisition_id
FROM input_of_dataset_processing input
INNER JOIN dataset ds ON input.dataset_id = ds.id;

UPDATE dataset ds SET ds.dataset_acquisition_id = (
      SELECT tmp.acquisition_id
      FROM 0053_temp tmp
      WHERE tmp.processing_id = ds.dataset_processing_id
      LIMIT 1
) WHERE ds.dataset_acquisition_id IS NULL AND ds.dataset_processing_id IS NOT NULL;

DROP TABLE `0053_temp`;