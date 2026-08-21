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

alter table shanoir_metadata add column center_id bigint(20);
alter table shanoir_metadata add column center_name varchar(255);
alter table shanoir_metadata add column magnetic_field_strength float;
alter table shanoir_metadata add column pixel_bandwidth float;
alter table shanoir_metadata add column slice_thickness float;
