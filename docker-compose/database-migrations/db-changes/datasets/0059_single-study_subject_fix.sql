UPDATE datasets.subject d
JOIN studies.subject s ON d.id = s.id
SET
    d.study_id     = s.study_id,
    d.subject_type = s.subject_type,
    d.quality_tag  = s.quality_tag
WHERE
    -- On ne met à jour que si les valeurs sont différentes ou null
    (d.study_id     IS NULL OR d.study_id     <> s.study_id)
 OR (d.subject_type IS NULL OR d.subject_type <> s.subject_type)
 OR (d.quality_tag  IS NULL OR d.quality_tag  <> s.quality_tag)
;
