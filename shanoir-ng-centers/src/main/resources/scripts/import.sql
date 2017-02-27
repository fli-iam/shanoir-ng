-- Populates database
-- Has to be executed manually inside the docker container, command example : mysql -u {db user name} -p{password} < populate.sql
-- ! But remember to wait for the java web server to have started since the schema is created by hibernate on startup !

use shanoir_ng_centers;

insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (1,'France','CHU Rennes','','','','Rennes','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (2,'France','CHU Reims','','','','Reims','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (3,'France','LPS - CENIR','0157274007','75013','Bâtiment ICM (niv -1) , 47 Bd de l''Hôpital','Paris','www.cenir.org');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (4,'France','CHU Marseille','','','','Marseille','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (5,'France','HCL - NeuroCardio','','','','Lyon','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (7,'France','Hôpitaux Universitaires de Strasbourg','','','','Strasbourg','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (8,'France','Centre Hospitalier Yves Le Foll','','','','Saint Brieuc','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (9,'France','CHU Nîmes','','','','Nîmes','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (10,'France','CHU Toulouse','','','','Toulouse','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (11,'France','CHU Nantes','','','','Nantes','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (12,'France','CHU Brabois','','','','Nancy','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (13,'France','CHU de Fort-de-France','','','','Fort-de-France','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (14,'France','CHU Michallon','','38700','Site Santé','Grenoble','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (15,'France','GIN','','38706','','Grenoble','http://neurosciences.ujf-grenoble.fr/main-home-1-sub-home-0-lang-en.html');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (16,'France','CHU Brest','','','','Brest','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (17,'France','CHGR','','','','Rennes','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (18,'France','HCL - NeuroCardio to delete','','','','Lyon','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (19,'France','HCL - Hopital Edouard Herriot','','','','Lyon','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (20,'France','HCL - CHLS RMN','','','','Lyon','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (21,'France','HCL - NeuroCardio HFME','','','','Lyon','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (22,'France','HCL - GIE Lyon Nord','','','','Lyon','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (23,'France','INSERM_825','','','','Toulouse','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (24,'Belgique','UCL','','','','Bruxelles','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (25,'France','CH Bretagne Atlantique Vannes','','','','Vannes','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (27,'France','ICM','','','','Paris','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (28,'France','CHU Bordeaux','','','','Bordeaux','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (29,'France','HCL - CHLS','','','','Lyon','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (30,'France','CHRU Clermont-Ferrand Gabriel Montpied','','','','Clermont-Ferrand','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (31,'France','CHRU Montpellier Gui de Chauliac','','','','Montpellier','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (33,'France','IRM des sources','','','','Lyon','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (35,'France','IPB Strasbourg','','','','Strasbourg','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (36,'France','Hôpital de la Timone - Marseille','','13005','264 rue St Pierre','Marseille','http://fr.ap-hm.fr/nos-hopitaux/hopital-de-la-timone');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (37,'France','HCL - Hôpital Pierre Wertheimer','','69677','59 bvd Pinel','Lyon','http://www.chu-lyon.fr/web/Hopital_Pierre-w_2346.html');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (38,'France','CHU - Charles-Nicolle- Rouen','','76031','1 rue de Germont','Rouen','http://www3.chu-rouen.fr/internet/connaitreCHU/reperes/soins/charles_nicolle/');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (39,'France','CHU - Dijon','','21079','14 rue Gaffarel','Dijon','http://www.chu-dijon.fr/');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (40,'France','CHU - Hôpital Central - Nancy','','54035','29 avenue du Maréchal de Lattre de Tassigny','Nancy','http://www.chu-nancy.fr/');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (41,'France','CHRU - Lille','','59037','2 avenue Oscar Lambret','Lille','http://www.chru-lille.fr/');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (43,'France','Hôpital Beaujon -Paris','','92110','100 boulevard du général Leclerc','Clichy','http://www.aphp.fr/hopital/beaujon/');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (44,'France','Hôpital Bicêtre - Paris','','94275','78 rue du général Leclerc','Le Kremlin Bicetre','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (45,'France','Hopital d''Instruction des Armées Sainte Anne','0483162014','83800','2 bvd Sainte Anne','Toulon','http://www.sainteanne.org/');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (46,'France','CHU St-Etienne','0477829226','42277','Hôpital Nord- avenue albert raimond','St Priest en Jarez - St Etienne','http://www.chu-st-etienne.fr/');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (47,'France','CH Annecy Genevois','','74374','1avenue de l''hôpital','Metz - Tessy','http://www.ch-annecygenevois.fr/fr');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (48,'France','Hopital Nord - Marseille','0491380000','13915','Chemin des Bourrely','Marseille','http://fr.ap-hm.fr/nos-hopitaux/hopital-nord');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (49,'France','Hopital Pitié Salpétrière','0157274007','75013','47-83 bd de l’hôpital','Paris','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (50,'France','CHU St Roch - Nice','','06006','5 rue Pierre Devoluy','Nice','');
insert into `center`(`id`,`COUNTRY`,`NAME`,`PHONE_NUMBER`,`POSTAL_CODE`,`STREET`,`CITY`,`WEBSITE`) values (51,'France','CHU Poitiers','','86000','rue de la miletrie','Poitiers','');
