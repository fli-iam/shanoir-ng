-- Create study_tags in studies for all relevant studies

INSERT INTO studies.study_tag (color, name, study_id)
SELECT DISTINCT '#e74c3c', 'volume.organ:brain', proc.study_id 
FROM datasets.dataset_property prop
INNER JOIN datasets.dataset_processing proc ON proc.id = prop.dataset_processing_id
WHERE prop.name = 'volume.organ' AND prop.value = 'brain';

INSERT INTO studies.study_tag (color, name, study_id)
SELECT DISTINCT '#27ae60', 'volume.organ:spine', proc.study_id 
FROM datasets.dataset_property prop
INNER JOIN datasets.dataset_processing proc ON proc.id = prop.dataset_processing_id
WHERE prop.name = 'volume.organ' AND prop.value = 'spine';

INSERT INTO studies.study_tag (color, name, study_id)
SELECT DISTINCT '#198bda', 'volume.organ:null', proc.study_id 
FROM datasets.dataset_property prop
INNER JOIN datasets.dataset_processing proc ON proc.id = prop.dataset_processing_id
WHERE prop.name = 'volume.organ' AND prop.value = 'null';

-- Copy studies study_tags to datasets study_tags

INSERT INTO datasets.study_tag (id, color, name, study_id)
SELECT id, color, name, study_id
FROM studies.study_tag;

-- Create dataset_tag for all relevant dataset

INSERT INTO dataset_tag (dataset_id, study_tag_id)
SELECT DISTINCT prop.dataset_id, tag.id 
FROM dataset_property prop
INNER JOIN dataset_processing proc ON proc.id = prop.dataset_processing_id
INNER JOIN study_tag tag ON tag.study_id = proc.study_id AND tag.name = 'volume.organ:brain'
WHERE prop.name = 'volume.organ' AND prop.value = 'brain';

INSERT INTO dataset_tag (dataset_id, study_tag_id)
SELECT DISTINCT prop.dataset_id, tag.id 
FROM dataset_property prop
INNER JOIN dataset_processing proc ON proc.id = prop.dataset_processing_id
INNER JOIN study_tag tag ON tag.study_id = proc.study_id AND tag.name = 'volume.organ:spine'
WHERE prop.name = 'volume.organ' AND prop.value = 'spine';

INSERT INTO dataset_tag (dataset_id, study_tag_id)
SELECT DISTINCT prop.dataset_id, tag.id
FROM dataset_property prop
INNER JOIN dataset_processing proc ON proc.id = prop.dataset_processing_id
INNER JOIN study_tag tag ON tag.study_id = proc.study_id AND tag.name = 'volume.organ:null'
WHERE prop.name = 'volume.organ' AND prop.value = 'null';
