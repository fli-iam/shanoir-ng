ALTER TABLE `dataset` ADD COLUMN `downloadable` bit(1) NOT NULL DEFAULT 1;
UPDATE dataset SET downloadable = 1;
-- This update should not be here, it should be a one shot.
-- UPDATE dataset d SET d.downloadable = 0 WHERE d.dataset_acquisition_id in (SELECT da.id FROM dataset_acquisition da where da.creation_date > '2023-06-17' AND da.creation_date < '2023-06-28');