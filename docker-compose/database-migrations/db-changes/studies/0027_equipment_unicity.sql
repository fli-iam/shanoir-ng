-- drop and recreate unique constraint on acquisition_equipment to include center_id
ALTER TABLE acquisition_equipment
    DROP FOREIGN KEY FKbvbig13gxsu8gxaw9h6uemhk4,
    DROP INDEX model_number_idx;
ALTER TABLE acquisition_equipment
    ADD UNIQUE KEY model_number_idx (center_id, manufacturer_model_id, serial_number),
    ADD CONSTRAINT FKbvbig13gxsu8gxaw9h6uemhk4
        FOREIGN KEY (manufacturer_model_id) REFERENCES manufacturer_model(id);
