-- one shot update to allow download
UPDATE dataset d SET d.downloadable = 0 WHERE d.dataset_acquisition_id in (SELECT da.id FROM dataset_acquisition da where da.creation_date > '2023-06-28');