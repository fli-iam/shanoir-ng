-- drop and recreate unique constraint on acquisition_equipment to include center_id
ALTER TABLE acquisition_equipment
    DROP FOREIGN KEY xxx,
    DROP INDEX model_number_idx,
    ADD UNIQUE KEY model_number_idx (center_id, manufacturer_model_id, serial_number),
    ADD CONSTRAINT xxx
        FOREIGN KEY (manufacturer_model_id) REFERENCES manufacturer_model(id);
