CREATE TEMPORARY TABLE proc_to_delete_ids AS
SELECT DISTINCT p.id AS processing_id, em.id AS execution_id
FROM dataset_processing AS p
         JOIN execution_monitoring AS em ON p.parent_id = em.id
WHERE p.parent_id IS NULL
   OR em.status != 1
   OR p.id IN (
       SELECT processing_id
       FROM (
           SELECT
               p.id AS processing_id,
               ROW_NUMBER() OVER (
                   PARTITION BY em.pipeline_identifier, GROUP_CONCAT(input.list_id ORDER BY input.list_id)
                   ORDER BY p.start_date DESC
               ) AS rn
           FROM dataset_processing AS p
           JOIN execution_monitoring AS em ON p.parent_id = em.id
           JOIN input_of_dataset_processing AS input ON input.processing_id = p.id
           GROUP BY p.id, em.pipeline_identifier, p.start_date
       ) sub
       WHERE rn > 1
   )
   OR p.id IN (SELECT proc.id
      FROM dataset_processing
      WHERE NOT EXISTS (
          SELECT 1
          FROM ds
          WHERE ds.dataset_processing_id = p.id
));

DELETE input
FROM input_of_dataset_processing AS input
JOIN dataset AS d ON d.id = input.dataset_id
JOIN proc_to_delete_ids t ON d.dataset_processing_id = t.processing_id;

DELETE d
FROM dataset AS d
JOIN proc_to_delete_ids t ON d.dataset_processing = t.processing_id;

DELETE p
FROM dataset_processing AS p
JOIN proc_to_delete_ids t ON p.id = t.processing_id;

DELETE em
FROM execution_monitoring AS em
JOIN proc_to_delete_ids t ON em.id = t.execution_id;