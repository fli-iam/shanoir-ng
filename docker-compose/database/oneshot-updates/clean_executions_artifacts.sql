SET group_concat_max_len=15000;

CREATE TEMPORARY TABLE proc_to_delete_ids AS
SELECT DISTINCT p.id AS processing_id, em.id AS execution_id
FROM dataset_processing AS p
         LEFT JOIN execution_monitoring AS em ON p.id = em.id
WHERE (p.parent_id IS NULL AND em.status != 1) -- Failed exec/job
    OR (p.parent_id IS NOT NULL
        AND NOT EXISTS (
            SELECT 1
            FROM dataset ds
            WHERE ds.dataset_processing_id = p.id
        ))
    OR (
        p.id IN (
            SELECT DISTINCT t1.processing_id
            FROM (
                     SELECT
                         p2.id AS processing_id,
                         em2.pipeline_identifier,
                         em2.start_date,
                         GROUP_CONCAT(input.dataset_id ORDER BY input.dataset_id) AS input_lists
                     FROM dataset_processing p2
                              JOIN execution_monitoring em2 ON p2.parent_id = em2.id
                              JOIN input_of_dataset_processing input ON input.processing_id = p2.id
                     WHERE em2.status = 1
                       AND p2.parent_id IS NOT NULL
                       AND EXISTS (
                         SELECT 1
                         FROM dataset ds
                         WHERE ds.dataset_processing_id = p2.id
                         )
                     GROUP BY p2.id, em2.pipeline_identifier, em2.start_date
                 ) t1
                     JOIN (
                SELECT
                    p2.id AS processing_id,
                    em2.pipeline_identifier,
                    em2.start_date,
                    GROUP_CONCAT(input.dataset_id ORDER BY input.dataset_id) AS input_lists
                FROM dataset_processing p2
                         JOIN execution_monitoring em2 ON p2.parent_id = em2.id
                         JOIN input_of_dataset_processing input ON input.processing_id = p2.id
                WHERE em2.status = 1
                  AND p2.parent_id IS NOT NULL
                  AND EXISTS (
                    SELECT 1
                    FROM dataset ds
                    WHERE ds.dataset_processing_id = p2.id
                    )
                GROUP BY p2.id, em2.pipeline_identifier, em2.start_date
            ) t2
        ON t1.pipeline_identifier = t2.pipeline_identifier
            AND t1.input_lists = t2.input_lists
            AND t1.start_date < t2.start_date
    ))
;

DELETE input
FROM input_of_dataset_processing AS input
JOIN proc_to_delete_ids t ON input.processing_id = t.processing_id;

DELETE seg
FROM segmentation_dataset as seg
JOIN dataset AS d on seg.id = d.id
JOIN proc_to_delete_ids t ON d.dataset_processing_id = t.processing_id;

DELETE mesh
FROM mesh_dataset as mesh
JOIN dataset AS d on mesh.id = d.id
JOIN proc_to_delete_ids t ON d.dataset_processing_id = t.processing_id;

DELETE mr
FROM mr_dataset as mr
JOIN dataset AS d on mr.id = d.id
JOIN proc_to_delete_ids t ON d.dataset_processing_id = t.processing_id;

DELETE df
FROM dataset_file AS df
JOIN dataset_expression AS de ON de.id = df.dataset_expression_id
JOIN dataset AS d on de.dataset_id = d.id
JOIN proc_to_delete_ids t ON d.dataset_processing_id = t.processing_id;

DELETE de
FROM dataset_expression AS de
JOIN dataset AS d ON de.dataset_id = d.id
JOIN proc_to_delete_ids t ON d.dataset_processing_id = t.processing_id;

DELETE gd
FROM generic_dataset as gd
JOIN dataset AS d on gd.id = d.id
JOIN proc_to_delete_ids t ON d.dataset_processing_id = t.processing_id;

DELETE cd
FROM calibration_dataset as cd
JOIN dataset AS d on cd.id = d.id
JOIN proc_to_delete_ids t ON d.dataset_processing_id = t.processing_id;

DELETE pr
FROM processing_resource pr
JOIN dataset d ON pr.dataset_id = d.id
JOIN proc_to_delete_ids t ON d.dataset_processing_id = t.processing_id;

DELETE dp
FROM dataset_property AS dp
JOIN dataset_processing AS p ON p.id = dp.dataset_processing_id
JOIN proc_to_delete_ids t ON p.id = t.processing_id;

DELETE d
FROM dataset AS d
JOIN proc_to_delete_ids t ON d.dataset_processing_id = t.processing_id;

DELETE p
FROM dataset_processing AS p
JOIN proc_to_delete_ids t ON p.id = t.processing_id;

DELETE em
FROM execution_monitoring AS em
JOIN proc_to_delete_ids t ON em.id = t.execution_id
WHERE (select 1 from dataset_processing proc where proc.parent_id = em.id ) IS NULL;