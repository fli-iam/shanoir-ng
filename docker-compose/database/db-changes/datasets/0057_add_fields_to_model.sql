-- Shanoir NG - Import, manage and share neuroimaging data
-- Copyright (C) 2009-2019 Inria - https://www.inria.fr/
-- Contact us on https://project.inria.fr/shanoir/
-- 
-- This program is free software: you can redistribute it and/or modify
-- it under the terms of the GNU General Public License as published by
-- the Free Software Foundation, either version 3 of the License, or
-- (at your option) any later version.
-- 
-- You should have received a copy of the GNU General Public License
-- along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

ALTER TABLE dataset_acquisition ADD COLUMN acquisition_start_time datetime default null;

ALTER TABLE ct_protocol ADD COLUMN slice_thickness double default null;
ALTER TABLE pet_protocol ADD COLUMN slice_thickness double default null;
ALTER TABLE xa_protocol ADD COLUMN slice_thickness double default null;

ALTER TABLE ct_protocol ADD COLUMN number_of_slices int default null;
ALTER TABLE mr_protocol ADD COLUMN number_of_slices int default null;
ALTER TABLE xa_protocol ADD COLUMN number_of_slices int default null;
