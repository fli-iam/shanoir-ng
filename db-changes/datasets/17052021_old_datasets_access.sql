# Update all mr_protocol  where origin_metadata_id is null but not origin_metadata
UPDATE mr_protocol SET origin_metadata_id = updated_metadata_id WHERE origin_metadata_id IS NULL and updated_metadata_id IS NOT NULL;

# Remove updated_metadata_id where they are equal with origin_metadata_id (when they are different it's not a problem)
UPDATE mr_protocol SET updated_metadata_id = NULL WHERE origin_metadata_id IS NOT NULL and updated_metadata_id IS NOT NULL AND origin_metadata_id = updated_metadata_id;