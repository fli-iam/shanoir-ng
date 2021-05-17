# Update all protocol metadata that are "updated" to dtype = 1
UPDATE mr_protocol_metadata SET dtype = 2 WHERE id IN (SELECT updated_metadata_id FROM mr_protocol WHERE updated_metadata IS NOT NULL);