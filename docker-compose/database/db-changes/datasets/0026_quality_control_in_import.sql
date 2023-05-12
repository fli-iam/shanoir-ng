ALTER TABLE `quality_card`
ADD COLUMN `to_check_at_import` bit(1) NOT NULL DEFAULT 0;
