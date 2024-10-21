# List of datasets not correctly updated

UPDATE dataset d SET d.subject_id =
    (SELECT e.subject_id FROM examination e WHERE e.id =
        (SELECT examination_id FROM dataset_acquisition da WHERE da.id = d.dataset_acquisition_id))
WHERE d.subject_id !=
    (SELECT e.subject_id from examination e WHERE e.id =
        (SELECT examination_id from dataset_acquisition da WHERE da.id = d.dataset_acquisition_id));
