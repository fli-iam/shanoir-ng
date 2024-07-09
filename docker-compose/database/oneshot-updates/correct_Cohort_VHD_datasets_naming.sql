UPDATE dataset_metadata mtd SET mtd.name = mtd.comment
WHERE mtd.id IN (
SELECT ds.updated_metadata_id FROM dataset ds 
INNER JOIN dataset_acquisition acq ON ds.dataset_acquisition_id = acq.id
INNER JOIN examination ex ON acq.examination_id = ex.id AND ex.study_id = 66
INNER JOIN dataset_property prop ON ds.id = prop.dataset_id AND prop.name = 'volume.name');
