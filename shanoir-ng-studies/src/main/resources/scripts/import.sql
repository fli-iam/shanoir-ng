-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !
--
use shanoir_ng_studies;

INSERT INTO study
	(id, name, start_date, end_date, clinical, with_examination, is_visible_by_default, is_downloadable_by_default, study_status)
VALUES
	(1,'Study 1', '2013/01/01', null, 0, 1, 0, 0, 'FINISHED'),
	(2,'Study 2', '2009/12/01', null, 0, 1, 0, 0, 'IN_PROGRESS'),
	(3,'Study 3', '2010/02/21', null, 0, 1, 0, 0, 'FINISHED'),
	(4,'Study 4', '2015/10/03', null, 0, 1, 0, 0, 'IN_PROGRESS');


INSERT INTO `center`
	(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`)
VALUES
	(1,'France','CHU Rennes','','','','Rennes',''),
	(2,'France','CHU Reims','','','','Reims',''),
	(3,'France','LPS - CENIR','0157274007','75013','Bâtiment ICM (niv -1) , 47 Bd de l''Hôpital','Paris','www.cenir.org'),
	(4,'France','CHU Marseille','','','','Marseille',''),
	(5,'France','HCL - NeuroCardio','','','','Lyon',''),
	(7,'France','Hôpitaux Universitaires de Strasbourg','','','','Strasbourg',''),
	(8,'France','Centre Hospitalier Yves Le Foll','','','','Saint Brieuc',''),
	(9,'France','CHU Nîmes','','','','Nîmes',''),
	(10,'France','CHU Toulouse','','','','Toulouse',''),
	(11,'France','CHU Nantes','','','','Nantes',''),
	(12,'France','CHU Brabois','','','','Nancy',''),
	(13,'France','CHU de Fort-de-France','','','','Fort-de-France',''),
	(14,'France','CHU Michallon','','38700','Site Santé','Grenoble',''),
	(15,'France','GIN','','38706','','Grenoble','http://neurosciences.ujf-grenoble.fr/main-home-1-sub-home-0-lang-en.html'),
	(16,'France','CHU Brest','','','','Brest',''),
	(17,'France','CHGR','','','','Rennes',''),
	(18,'France','HCL - NeuroCardio to delete','','','','Lyon',''),
	(19,'France','HCL - Hopital Edouard Herriot','','','','Lyon',''),
	(20,'France','HCL - CHLS RMN','','','','Lyon',''),
	(21,'France','HCL - NeuroCardio HFME','','','','Lyon',''),
	(22,'France','HCL - GIE Lyon Nord','','','','Lyon',''),
	(23,'France','INSERM_825','','','','Toulouse',''),
	(24,'Belgique','UCL','','','','Bruxelles',''),
	(25,'France','CH Bretagne Atlantique Vannes','','','','Vannes',''),
	(27,'France','ICM','','','','Paris',''),
	(28,'France','CHU Bordeaux','','','','Bordeaux',''),
	(29,'France','HCL - CHLS','','','','Lyon',''),
	(30,'France','CHRU Clermont-Ferrand Gabriel Montpied','','','','Clermont-Ferrand',''),
	(31,'France','CHRU Montpellier Gui de Chauliac','','','','Montpellier',''),
	(33,'France','IRM des sources','','','','Lyon',''),
	(35,'France','IPB Strasbourg','','','','Strasbourg',''),
	(36,'France','Hôpital de la Timone - Marseille','','13005','264 rue St Pierre','Marseille','http://fr.ap-hm.fr/nos-hopitaux/hopital-de-la-timone'),
	(37,'France','HCL - Hôpital Pierre Wertheimer','','69677','59 bvd Pinel','Lyon','http://www.chu-lyon.fr/web/Hopital_Pierre-w_2346.html'),
	(38,'France','CHU - Charles-Nicolle- Rouen','','76031','1 rue de Germont','Rouen','http://www3.chu-rouen.fr/internet/connaitreCHU/reperes/soins/charles_nicolle/'),
	(39,'France','CHU - Dijon','','21079','14 rue Gaffarel','Dijon','http://www.chu-dijon.fr/'),
	(40,'France','CHU - Hôpital Central - Nancy','','54035','29 avenue du Maréchal de Lattre de Tassigny','Nancy','http://www.chu-nancy.fr/'),
	(41,'France','CHRU - Lille','','59037','2 avenue Oscar Lambret','Lille','http://www.chru-lille.fr/'),
	(43,'France','Hôpital Beaujon -Paris','','92110','100 boulevard du général Leclerc','Clichy','http://www.aphp.fr/hopital/beaujon/'),
	(44,'France','Hôpital Bicêtre - Paris','','94275','78 rue du général Leclerc','Le Kremlin Bicetre',''),
	(45,'France','Hopital d''Instruction des Armées Sainte Anne','0483162014','83800','2 bvd Sainte Anne','Toulon','http://www.sainteanne.org/'),
	(46,'France','CHU St-Etienne','0477829226','42277','Hôpital Nord- avenue albert raimond','St Priest en Jarez - St Etienne','http://www.chu-st-etienne.fr/'),
	(47,'France','CH Annecy Genevois','','74374','1avenue de l''hôpital','Metz - Tessy','http://www.ch-annecygenevois.fr/fr'),
	(48,'France','Hopital Nord - Marseille','0491380000','13915','Chemin des Bourrely','Marseille','http://fr.ap-hm.fr/nos-hopitaux/hopital-nord'),
	(49,'France','Hopital Pitié Salpétrière','0157274007','75013','47-83 bd de l’hôpital','Paris',''),
	(50,'France','CHU St Roch - Nice','','06006','5 rue Pierre Devoluy','Nice',''),
	(51,'France','CHU Poitiers','','86000','rue de la miletrie','Poitiers','');


INSERT INTO pseudonymus_hash_values
 (id, birth_name_hash1, birth_name_hash2, birth_name_hash3, last_name_hash1, last_name_hash2, last_name_hash3, first_name_hash1, first_name_hash2, first_name_hash3, birth_date_hash)
VALUES
 (  1,
    'edbee6d1302d1b5a749aeb42e5747ea8503f3f5ae3f2b41247cac3e735106ed5',
    'f5b1f63c652852724daec3ab2fc51ba20792a0cf85c102066d412746dda72b84',
    'f7ca8a978bd2ba11ee0d843453103938562b6e48ef3237e2daf3a743f826f7ee',
    'edbee6d1302d1b5a749aeb42e5747ea8503f3f5ae3f2b41247cac3e735106ed5',
    'f5b1f63c652852724daec3ab2fc51ba20792a0cf85c102066d412746dda72b84',
    'f7ca8a978bd2ba11ee0d843453103938562b6e48ef3237e2daf3a743f826f7ee',
    'edbee6d1302d1b5a749aeb42e5747ea8503f3f5ae3f2b41247cac3e735106ed5',
    'f5b1f63c652852724daec3ab2fc51ba20792a0cf85c102066d412746dda72b84',
    'f7ca8a978bd2ba11ee0d843453103938562b6e48ef3237e2daf3a743f826f7ee',
    'efa0bd9d3793157b8b44cd76814c079e0eb1f8a3a3017dc0a58959f581d7a097');

INSERT INTO subject
	(id, name, identifier, birth_date, imaged_object_category, language_hemispheric_dominance,  manual_hemispheric_dominance, sex,  pseudonymus_hash_values_id)
VALUES
	(1,'subject1', 'sub1', '2013/01/01', 'LIVING_HUMAN_BEING','Left','Left','F',1),
	(2,'subject2', 'sub2', '2001/02/01', 'LIVING_HUMAN_BEING','Right','Left','F',1),
	(3,'0010001', 'sub3', '2001/02/01', 'LIVING_HUMAN_BEING','Left','Right','F',1);


INSERT INTO subject_study
	(id, physically_involved, study, subject )
VALUES
	(1, 0, 1, 1),
	(2, 0, 1, 2),
	(3, 0, 2, 1);


INSERT INTO manufacturer
	(id, name)
VALUES
	(1, 'GE Healthcare'),
	(2, 'GE Medical Systems'),
	(3, 'Philips Healthcare');

INSERT INTO manufacturer_model
	(id, dataset_modality_type, manufacturer_id, name, magnetic_field)
VALUES
	(1, 'MR_DATASET', 2, 'DISCOVERY MR750', 3),
	(2, 'PET_DATASET', 2, 'DISCOVERY MR750w', null),
	(3, 'MR_DATASET', 3, 'Ingenia', 1.5);

INSERT INTO acquisition_equipment
	(id, center_id, manufacturer_model_id, serial_number)
VALUES
	(1, 1, 1, '123456789'),
	(2, 2, 1, '234567891'),
	(3, 1, 2, '345678912');
