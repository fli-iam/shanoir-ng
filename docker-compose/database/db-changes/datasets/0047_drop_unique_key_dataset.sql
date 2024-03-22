ALTER TABLE dataset DROP foreign key FK6mnu5quxanbou4iew3a8t2mwc;
ALTER TABLE dataset DROP foreign key FKrp3auy5ka4dxe8up1pltmub41;
ALTER TABLE dataset DROP index UK_qrxunikj09qwx7ci6fpopyyuh;
ALTER TABLE dataset DROP index UK_q5hjffag30a00rysle2hyhyov;
ALTER TABLE dataset ADD CONSTRAINT `FK6mnu5quxanbou4iew3a8t2mwc` FOREIGN KEY (`origin_metadata_id`) REFERENCES `dataset_metadata` (`id`);
ALTER TABLE dataset ADD CONSTRAINT `FKrp3auy5ka4dxe8up1pltmub41` FOREIGN KEY (`updated_metadata_id`) REFERENCES `dataset_metadata` (`id`);