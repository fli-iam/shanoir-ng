-- Create temporary index to accelerate deletion of duplicates
CREATE INDEX idx_study_exam ON study_examination (study_id, examination_id);

-- Delete duplicates, keeping the one with the lowest id
DELETE se
FROM study_examination se
JOIN study_examination se2
    ON se.study_id = se2.study_id
    AND se.examination_id = se2.examination_id
    AND se.id > se2.id;

-- Drop temporary index
DROP INDEX idx_study_exam ON study_examination;

-- Add unique constraint to prevent future duplicates
ALTER TABLE study_examination ADD UNIQUE KEY uk_study_exam (study_id, examination_id);
