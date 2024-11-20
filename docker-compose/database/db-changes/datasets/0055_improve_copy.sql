INSERT INTO dataset_copies (dataset_id, copies_id) SELECT source_id, id FROM dataset WHERE source_id is not null;.
UPDATE dataset d1 JOIN dataset d2 ON d1.id = d2.source_id SET d1.origin = 2;
UPDATE dataset SET origin = 1 WHERE source_id IS NOT NULL;

INSERT INTO dataset_acquisition_copies (dataset_acquisition_id, copies_id) SELECT source_id, id FROM dataset_acquisition WHERE source_id is not null;.
UPDATE dataset_acquisition da1 JOIN dataset_acquisition da2 ON da1.id = da2.source_id SET da1.origin = 2;
UPDATE dataset_acquisition SET origin = 1 WHERE source_id IS NOT NULL;

INSERT INTO examination_copies (examination_id, copies_id) SELECT source_id, id FROM examination WHERE source_id is not null
UPDATE examination e1 JOIN examination e2 ON e1.id = e2.source_id SET e1.origin = 2;
UPDATE examination SET origin = 1 WHERE source_id IS NOT NULL;