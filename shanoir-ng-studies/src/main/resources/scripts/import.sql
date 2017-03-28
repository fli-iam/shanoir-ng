-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !
--
use shanoir_ng_studies;


INSERT INTO study_status 
	(id, label_name)
VALUES 
	(1,'finished'),
	(2,'in_progress');

INSERT INTO study
	(id, name, start_date, end_date, clinical, with_examination, is_visible_by_default, is_downloadable_by_default, study_status_id)
VALUES 
	(1,'Study 1', '2013/01/01', null, 0, 1, 0, 0, null),
	(2,'Study 2', '2009/12/01', null, 0, 1, 0, 0, null),
	(3,'Study 3', '2010/02/21', null, 0, 1, 0, 0, null),
	(4,'Study 4', '2015/10/03', null, 0, 1, 0, 0, null);
	
	
INSERT INTO REL_STUDY_USER
	(REL_STUDY_USER_ID, USER_ID, study)
VALUES 
	(1, 1, 1),
	(2, 1, 2),
	(3, 2, 2),
	(4, 3, 3);

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
