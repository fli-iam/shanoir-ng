-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use shanoir_ng_import;

INSERT INTO nifticonverter (id, name, nifti_converter_type, is_active) VALUES (1, 'dcm2nii_2008-03-31', 1, 1);
INSERT INTO nifticonverter (id, name, nifti_converter_type, is_active) VALUES (2, 'mcverter_2.0.7', 2, 1);
INSERT INTO nifticonverter (id, name, nifti_converter_type, is_active) VALUES (3, 'clidcm', 3, 0);
INSERT INTO nifticonverter (id, name, nifti_converter_type, is_active) VALUES (4, 'dcm2nii_2014-08-04', 1, 1);
