ALTER TABLE `examination` ADD COLUMN `study_instanceuid` varchar(255);

CREATE UNIQUE INDEX UK_15imoe7p5ks3na50kltrhvw6r ON examination(study_instanceuid);
