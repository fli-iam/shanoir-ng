-- Create study_tags for all relevant studies

INSERT INTO study_tag (color, name, study_id)
SELECT DISTINCT '#e74c3c', 'volume.organ:brain', proc.study_id 
FROM dataset_property prop
INNER JOIN dataset_processing proc ON proc.id = prop.dataset_processing_id
WHERE prop.name = 'volume.organ' AND prop.value = 'brain';

INSERT INTO study_tag (color, name, study_id)
SELECT DISTINCT '#27ae60', 'volume.organ:spine', proc.study_id 
FROM dataset_property prop
INNER JOIN dataset_processing proc ON proc.id = prop.dataset_processing_id
WHERE prop.name = 'volume.organ' AND prop.value = 'spine';

INSERT INTO study_tag (color, name, study_id)
SELECT DISTINCT '#198bda', 'volume.organ:null', proc.study_id 
FROM dataset_property prop
INNER JOIN dataset_processing proc ON proc.id = prop.dataset_processing_id
WHERE prop.name = 'volume.organ' AND prop.value = 'null';

-- Create dataset_tag for all relevant dataset

INSERT INTO dataset_tag (dataset_id, study_tag_id)
SELECT DISTINCT prop.dataset_id, proc.study_id 
FROM dataset_property prop
INNER JOIN dataset_processing proc ON proc.id = prop.dataset_processing_id
INNER JOIN study_tag tag ON tag.study_id = proc.study_id AND tag.name = 'volume.organ:brain'
WHERE prop.name = 'volume.organ' AND prop.value = 'brain';

INSERT INTO dataset_tag (dataset_id, study_tag_id)
SELECT DISTINCT prop.dataset_id, proc.study_id 
FROM dataset_property prop
INNER JOIN dataset_processing proc ON proc.id = prop.dataset_processing_id
INNER JOIN study_tag tag ON tag.study_id = proc.study_id AND tag.name = 'volume.organ:spine'
WHERE prop.name = 'volume.organ' AND prop.value = 'spine';

INSERT INTO dataset_tag (dataset_id, study_tag_id)
SELECT DISTINCT prop.dataset_id, proc.study_id 
FROM dataset_property prop
INNER JOIN dataset_processing proc ON proc.id = prop.dataset_processing_id
INNER JOIN study_tag tag ON tag.study_id = proc.study_id AND tag.name = 'volume.organ:null'
WHERE prop.name = 'volume.organ' AND prop.value = 'null';
