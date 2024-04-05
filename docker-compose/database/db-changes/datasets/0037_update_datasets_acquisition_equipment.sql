DELETE from datasets.acquisition_equipment;

INSERT INTO datasets.acquisition_equipment (id, name)
SELECT ae.id AS id, CONCAT(sm.name, ' - ', smm.name, IF(smm.magnetic_field IS NULL, '', CONCAT( ' (', smm.magnetic_field, 'T', ') ')), ae.serial_number, ' ', c.name)
    AS name FROM studies.acquisition_equipment ae, studies.manufacturer_model smm, studies.manufacturer sm, studies.center c
    WHERE ae.manufacturer_model_id = smm.id AND smm.manufacturer_id = sm.id AND ae.center_id = c.id;