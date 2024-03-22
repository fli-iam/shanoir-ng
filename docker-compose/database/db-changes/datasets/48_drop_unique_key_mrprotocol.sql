ALTER TABLE mr_protocol DROP foreign key FK4gm6lnao8ysuok2u1r2vyughy;
ALTER TABLE mr_protocol DROP foreign key FKopgo8t6om1xcyberh5evka7j2;
ALTER TABLE mr_protocol DROP index UK_k1sxr7vss00iohj2pwquyuuca;
ALTER TABLE mr_protocol DROP index UK_sf6uvkudur9pitc2jh056k5lm;
alter table mr_protocol add CONSTRAINT `FK4gm6lnao8ysuok2u1r2vyughy` FOREIGN KEY (`origin_metadata_id`) REFERENCES `mr_protocol_metadata` (`id`);
alter table mr_protocol add CONSTRAINT `FKopgo8t6om1xcyberh5evka7j2` FOREIGN KEY (`updated_metadata_id`) REFERENCES `mr_protocol_metadata` (`id`);