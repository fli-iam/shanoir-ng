-- Remove all volume.organ dataset_tags

DELETE FROM datasets.dataset_tag
WHERE study_tag_id IN (SELECT id FROM datasets.study_tag WHERE name LIKE 'volume.organ:%');

-- Remove all volume.organ study_tags in datasets and studies MS

DELETE FROM studies.study_tag
WHERE name LIKE 'volume.organ:%';

DELETE FROM datasets.study_tag
WHERE name LIKE 'volume.organ:%';

-- Correct study_id of dataset_processings

UPDATE dataset_processing dp SET dp.study_id = (SELECT e.study_id FROM examination e
INNER JOIN dataset_acquisition da ON da.examination_id = e.id
INNER JOIN dataset ds ON ds.dataset_acquisition_id = da.id
WHERE ds.id = (SELECT iddp.dataset_id FROM input_of_dataset_processing iddp WHERE dp.id = iddp.processing_id LIMIT 1))
WHERE dp.parent_id IS NOT NULL;

-- Create study_tags in studies for all relevant studies

INSERT INTO studies.study_tag (color, name, study_id)
SELECT DISTINCT '#e74c3c', 'volume.organ:brain', proc.study_id 
FROM datasets.dataset_property prop
INNER JOIN datasets.dataset_processing proc ON proc.parent_id = prop.dataset_processing_id
WHERE prop.name = 'volume.organ' AND prop.value = 'brain';

INSERT INTO studies.study_tag (color, name, study_id)
SELECT DISTINCT '#27ae60', 'volume.organ:spine', proc.study_id 
FROM datasets.dataset_property prop
INNER JOIN datasets.dataset_processing proc ON proc.parent_id = prop.dataset_processing_id
WHERE prop.name = 'volume.organ' AND prop.value = 'spine';

INSERT INTO studies.study_tag (color, name, study_id)
SELECT DISTINCT '#198bda', 'volume.organ:null', proc.study_id 
FROM datasets.dataset_property prop
INNER JOIN datasets.dataset_processing proc ON proc.parent_id = prop.dataset_processing_id
WHERE prop.name = 'volume.organ' AND prop.value = 'null';

-- Copy studies study_tags to datasets study_tags

INSERT INTO datasets.study_tag (id, color, name, study_id)
SELECT id, color, name, study_id
FROM studies.study_tag;

-- Create dataset_tag for all relevant dataset

INSERT INTO datasets.dataset_tag (dataset_id, study_tag_id)
SELECT DISTINCT prop.dataset_id, tag.id 
FROM datasets.dataset_property prop
INNER JOIN datasets.dataset_processing proc ON proc.id = prop.dataset_processing_id
INNER JOIN datasets.study_tag tag ON tag.study_id = proc.study_id AND tag.name = 'volume.organ:brain'
WHERE prop.name = 'volume.organ' AND prop.value = 'brain';

INSERT INTO datasets.dataset_tag (dataset_id, study_tag_id)
SELECT DISTINCT prop.dataset_id, tag.id 
FROM datasets.dataset_property prop
INNER JOIN datasets.dataset_processing proc ON proc.parent_id = prop.dataset_processing_id
INNER JOIN datasets.study_tag tag ON tag.study_id = proc.study_id AND tag.name = 'volume.organ:spine'
WHERE prop.name = 'volume.organ' AND prop.value = 'spine';

INSERT INTO datasets.dataset_tag (dataset_id, study_tag_id)
SELECT DISTINCT prop.dataset_id, tag.id
FROM datasets.dataset_property prop
INNER JOIN datasets.dataset_processing proc ON proc.parent_id = prop.dataset_processing_id
INNER JOIN datasets.study_tag tag ON tag.study_id = proc.study_id AND tag.name = 'volume.organ:null'
WHERE prop.name = 'volume.organ' AND prop.value = 'null';
