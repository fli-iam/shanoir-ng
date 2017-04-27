-- MySQL dump 10.13  Distrib 8.0.0-dmr, for Linux (x86_64)
--
-- Host: localhost    Database: keycloak
-- ------------------------------------------------------
-- Server version	8.0.0-dmr

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `ADMIN_EVENT_ENTITY`
--

DROP TABLE IF EXISTS `ADMIN_EVENT_ENTITY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ADMIN_EVENT_ENTITY` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ADMIN_EVENT_TIME` bigint(20) DEFAULT NULL,
  `REALM_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `OPERATION_TYPE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `AUTH_REALM_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `AUTH_CLIENT_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `AUTH_USER_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `IP_ADDRESS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `RESOURCE_PATH` varchar(2550) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REPRESENTATION` text COLLATE utf8_unicode_ci,
  `ERROR` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `RESOURCE_TYPE` varchar(64) COLLATE utf8_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ADMIN_EVENT_ENTITY`
--

LOCK TABLES `ADMIN_EVENT_ENTITY` WRITE;
/*!40000 ALTER TABLE `ADMIN_EVENT_ENTITY` DISABLE KEYS */;
/*!40000 ALTER TABLE `ADMIN_EVENT_ENTITY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `ASSOCIATED_POLICY`
--

DROP TABLE IF EXISTS `ASSOCIATED_POLICY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `ASSOCIATED_POLICY` (
  `POLICY_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ASSOCIATED_POLICY_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`POLICY_ID`,`ASSOCIATED_POLICY_ID`),
  KEY `FK_FRSR5S213XCX4WNKOG82SSRFY` (`ASSOCIATED_POLICY_ID`),
  CONSTRAINT `FK_FRSR5S213XCX4WNKOG82SSRFY` FOREIGN KEY (`ASSOCIATED_POLICY_ID`) REFERENCES `RESOURCE_SERVER_POLICY` (`ID`),
  CONSTRAINT `FK_FRSRPAS14XCX4WNKOG82SSRFY` FOREIGN KEY (`POLICY_ID`) REFERENCES `RESOURCE_SERVER_POLICY` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `ASSOCIATED_POLICY`
--

LOCK TABLES `ASSOCIATED_POLICY` WRITE;
/*!40000 ALTER TABLE `ASSOCIATED_POLICY` DISABLE KEYS */;
INSERT INTO `ASSOCIATED_POLICY` VALUES ('13d91fcc-ccb0-4a1f-8a47-6de7d53198de','c9e01425-988f-4385-ace5-f8591f4b3199');
/*!40000 ALTER TABLE `ASSOCIATED_POLICY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `AUTHENTICATION_EXECUTION`
--

DROP TABLE IF EXISTS `AUTHENTICATION_EXECUTION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AUTHENTICATION_EXECUTION` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ALIAS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `AUTHENTICATOR` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `FLOW_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REQUIREMENT` int(11) DEFAULT NULL,
  `PRIORITY` int(11) DEFAULT NULL,
  `AUTHENTICATOR_FLOW` bit(1) NOT NULL DEFAULT b'0',
  `AUTH_FLOW_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `AUTH_CONFIG` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_AUTH_EXEC_REALM` (`REALM_ID`),
  KEY `FK_AUTH_EXEC_FLOW` (`FLOW_ID`),
  CONSTRAINT `FK_AUTH_EXEC_FLOW` FOREIGN KEY (`FLOW_ID`) REFERENCES `AUTHENTICATION_FLOW` (`ID`),
  CONSTRAINT `FK_AUTH_EXEC_REALM` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `AUTHENTICATION_EXECUTION`
--

LOCK TABLES `AUTHENTICATION_EXECUTION` WRITE;
/*!40000 ALTER TABLE `AUTHENTICATION_EXECUTION` DISABLE KEYS */;
INSERT INTO `AUTHENTICATION_EXECUTION` VALUES ('057be517-d454-45d3-9a04-2fafb298a299',NULL,'auth-script-based','shanoir-ng','e622576b-2e09-42be-bf8c-71ff925e3f6e',0,31,'\0',NULL,'9301d0fd-e311-4831-81fd-365a2ec43be7'),('0587f341-d3df-4da4-82b8-dc440377db07',NULL,'auth-spnego','shanoir-ng','e622576b-2e09-42be-bf8c-71ff925e3f6e',3,20,'\0',NULL,NULL),('07d00fc0-6dd8-4a2c-803e-8d330b6a4e79',NULL,'auth-cookie','shanoir-ng','a532c395-637a-4283-a455-668df6c946b2',2,10,'\0',NULL,NULL),('0d49a46d-09d1-4cc5-be0d-8164b343d3a3',NULL,'client-secret','master','61cc90b3-b42d-422e-a116-0f584fc76df3',2,10,'\0',NULL,NULL),('11623ad4-5eb7-4885-bfe7-75e72665db1a',NULL,NULL,'master','ebbcf719-47e9-45ca-bdbc-4150c96b1b90',2,30,'','a5604c9a-cb9f-4379-ab76-5d539977f2db',NULL),('118956f8-a9e5-40cc-931b-097e4421a356',NULL,'direct-grant-validate-username','shanoir-ng','0e20ce2d-379e-4b57-bedd-43075a4b4327',0,10,'\0',NULL,NULL),('1d7ec8b5-4689-4f3c-86f6-89c0230c33a3',NULL,'idp-username-password-form','shanoir-ng','a5bd910d-e98a-4f14-8159-d541f755cfb6',0,10,'\0',NULL,NULL),('2098d6c1-a3d8-4413-bee8-2ef42f1090ea',NULL,'direct-grant-validate-otp','master','62cce624-2e2e-458b-a4f5-5bec4ccd747b',1,30,'\0',NULL,NULL),('280ecc38-9c75-48a5-8346-496330f2364e',NULL,'idp-review-profile','master','31f8b582-7289-40d7-b003-431ce261eb88',0,10,'\0',NULL,'22e6c886-451c-4f70-82b4-bf8ede7214a0'),('2e04f277-5762-45cf-9f13-fcf9f028a815',NULL,'registration-password-action','shanoir-ng','6f5b1307-cca2-41cf-95c3-21a22a367089',0,50,'\0',NULL,NULL),('2f639ae6-89e3-4c54-beb8-cd51ddfd0289',NULL,'reset-password','shanoir-ng','cf00062f-34c3-416e-9cd4-4abe6c77f029',0,30,'\0',NULL,NULL),('2f9f45a7-51c8-472d-bad6-b0399e56be5f',NULL,'reset-otp','master','c3aa4d33-c17b-4609-a07a-4d56b98b0f92',1,40,'\0',NULL,NULL),('31dbc483-8ce0-470c-80d6-fdffa1252e02',NULL,NULL,'shanoir-ng','52822ffe-d410-4427-8768-f239c88bb947',2,30,'','a5bd910d-e98a-4f14-8159-d541f755cfb6',NULL),('35e33b34-829b-4f91-a091-9cf06f22ffe4',NULL,'registration-user-creation','master','5cab487f-1bbe-49d4-a345-bcb3de83a921',0,20,'\0',NULL,NULL),('35ff3142-72b5-4779-887b-659975ef4df5',NULL,NULL,'shanoir-ng','e622576b-2e09-42be-bf8c-71ff925e3f6e',2,30,'','0666086a-032d-4a0d-9820-21ad39b84bd7',NULL),('3ca9c46a-46d9-4364-9fbf-0a388118d117',NULL,'http-basic-authenticator','master','c5dd483d-238d-4712-8b8c-749b0840c6bc',0,10,'\0',NULL,NULL),('3db6e899-cce7-4ffe-971a-bce17ef88670',NULL,'registration-recaptcha-action','master','5cab487f-1bbe-49d4-a345-bcb3de83a921',3,60,'\0',NULL,NULL),('3f24f643-2e85-45f0-a939-fe07efd376ab',NULL,'auth-spnego','shanoir-ng','a532c395-637a-4283-a455-668df6c946b2',3,20,'\0',NULL,NULL),('40255470-068c-494e-a9c4-b3d9f84dc99a',NULL,'idp-create-user-if-unique','shanoir-ng','9b8b68d9-5129-4531-a139-20a7b16bdd22',2,20,'\0',NULL,'c203e4d4-1e39-45d8-b004-46ff31db5bae'),('40d86beb-e517-499e-965a-2ac18bd317a5',NULL,'client-jwt','shanoir-ng','408934cc-53ff-47e0-b438-046fd293e13f',2,20,'\0',NULL,NULL),('4953682c-c8b5-4641-8069-174a75fac6db',NULL,'reset-password','master','c3aa4d33-c17b-4609-a07a-4d56b98b0f92',0,30,'\0',NULL,NULL),('507af042-d561-4a9b-902f-30270641da45',NULL,NULL,'shanoir-ng','a532c395-637a-4283-a455-668df6c946b2',2,30,'','4b42546a-e9b6-4af6-ba32-367255b3a16f',NULL),('5187e00d-4074-461a-bdc9-1f863003efd0',NULL,'direct-grant-validate-password','master','62cce624-2e2e-458b-a4f5-5bec4ccd747b',0,20,'\0',NULL,NULL),('52d1ae85-3545-4cf6-8da4-a4bee0447943',NULL,'reset-credentials-choose-user','master','c3aa4d33-c17b-4609-a07a-4d56b98b0f92',0,10,'\0',NULL,NULL),('5c5722a2-8df8-4ad7-ac4e-73f4728e3d21',NULL,'idp-confirm-link','shanoir-ng','52822ffe-d410-4427-8768-f239c88bb947',0,10,'\0',NULL,NULL),('64b49877-fc84-4b51-85d7-ca95fb5f6ba2',NULL,'identity-provider-redirector','shanoir-ng','e622576b-2e09-42be-bf8c-71ff925e3f6e',2,25,'\0',NULL,NULL),('699a61f4-28e3-4b36-b9f8-58bb839bd1a9',NULL,'auth-otp-form','master','a5604c9a-cb9f-4379-ab76-5d539977f2db',1,20,'\0',NULL,NULL),('735ba87b-07f0-441d-8fb5-28cd9e275f26',NULL,'auth-otp-form','shanoir-ng','0666086a-032d-4a0d-9820-21ad39b84bd7',1,20,'\0',NULL,NULL),('75d14036-6335-48af-971f-8d464e8169d4',NULL,'http-basic-authenticator','shanoir-ng','405ffc08-1ca2-4f5e-85c2-f9e9433dbca4',0,10,'\0',NULL,NULL),('7736a991-da3d-4555-a966-052d7f3b82de',NULL,'auth-username-password-form','master','d09990c5-019c-4179-87ae-0b35970586a6',0,10,'\0',NULL,NULL),('8189e65d-9afe-46e3-a238-8edf91933cdd',NULL,'idp-review-profile','shanoir-ng','9b8b68d9-5129-4531-a139-20a7b16bdd22',0,10,'\0',NULL,'39f4644d-4a3b-4e4b-92c0-d8318aed1fea'),('883d743c-8259-40c0-ad9b-c61d3fe1d917',NULL,'client-jwt','master','61cc90b3-b42d-422e-a116-0f584fc76df3',2,20,'\0',NULL,NULL),('88714500-b33f-433b-89b6-b2c9380a32f2',NULL,'auth-otp-form','shanoir-ng','a5bd910d-e98a-4f14-8159-d541f755cfb6',1,20,'\0',NULL,NULL),('8b4a17bc-cc7c-4594-9664-c7a8b3102545',NULL,'idp-confirm-link','master','ebbcf719-47e9-45ca-bdbc-4150c96b1b90',0,10,'\0',NULL,NULL),('9476e8d4-47e8-49c7-9e92-cb6e1dd9d662',NULL,'identity-provider-redirector','shanoir-ng','a532c395-637a-4283-a455-668df6c946b2',2,25,'\0',NULL,NULL),('952b4435-c748-4c84-a66d-41987261c2fc',NULL,'auth-otp-form','shanoir-ng','4b42546a-e9b6-4af6-ba32-367255b3a16f',1,20,'\0',NULL,NULL),('96b81ca2-5b51-4067-8ba7-70157542cb1b',NULL,'auth-username-password-form','shanoir-ng','0666086a-032d-4a0d-9820-21ad39b84bd7',0,10,'\0',NULL,NULL),('9ba0f0c7-b08e-4d46-8a19-89b1c9eca75c',NULL,'direct-grant-validate-username','master','62cce624-2e2e-458b-a4f5-5bec4ccd747b',0,10,'\0',NULL,NULL),('9f479777-f9a3-4f78-bd21-1fdbffd226e6',NULL,'registration-page-form','shanoir-ng','dbeeb75d-afb2-46e8-8260-f1daae2b3a7a',0,10,'','6f5b1307-cca2-41cf-95c3-21a22a367089',NULL),('a170ba85-5c91-4d62-9a29-a25669bbf942',NULL,'auth-username-password-form','shanoir-ng','4b42546a-e9b6-4af6-ba32-367255b3a16f',0,10,'\0',NULL,NULL),('a24d7b88-0f0d-42b4-a418-a99e85fad5d5',NULL,'auth-spnego','master','9a9a6d2f-e6ef-4445-84cb-d712aeca2ebb',3,20,'\0',NULL,NULL),('aa9d5253-63ce-440e-9a9a-3dd8476a0397',NULL,'reset-otp','shanoir-ng','cf00062f-34c3-416e-9cd4-4abe6c77f029',1,40,'\0',NULL,NULL),('abd9bd0c-faac-48ef-96ad-a67a8c608bed',NULL,'registration-profile-action','shanoir-ng','6f5b1307-cca2-41cf-95c3-21a22a367089',0,40,'\0',NULL,NULL),('aeae9170-6dfc-4645-b481-1df3b9ac96df',NULL,'registration-user-creation','shanoir-ng','6f5b1307-cca2-41cf-95c3-21a22a367089',0,20,'\0',NULL,NULL),('aee8e0fc-c0c1-4b7d-8618-9895574a20cd',NULL,'registration-recaptcha-action','shanoir-ng','6f5b1307-cca2-41cf-95c3-21a22a367089',3,60,'\0',NULL,NULL),('bae939c2-56a9-43b8-89d9-25e0da3b1a3f',NULL,'idp-username-password-form','master','a5604c9a-cb9f-4379-ab76-5d539977f2db',0,10,'\0',NULL,NULL),('bd8370ee-cef2-405e-a581-d4612cfd2194',NULL,'client-secret','shanoir-ng','408934cc-53ff-47e0-b438-046fd293e13f',2,10,'\0',NULL,NULL),('c39fd58a-c81e-4a30-934a-105cf97cb588',NULL,'idp-email-verification','shanoir-ng','52822ffe-d410-4427-8768-f239c88bb947',2,20,'\0',NULL,NULL),('c4a6321f-705a-4e67-b83e-069f60240f84',NULL,NULL,'master','9a9a6d2f-e6ef-4445-84cb-d712aeca2ebb',2,30,'','d09990c5-019c-4179-87ae-0b35970586a6',NULL),('c93b8b73-0f81-4ade-9bfb-037e5b39fae6',NULL,'auth-cookie','master','9a9a6d2f-e6ef-4445-84cb-d712aeca2ebb',2,10,'\0',NULL,NULL),('cb759eb3-2963-48c1-9633-72b7a733ed37',NULL,'auth-cookie','shanoir-ng','e622576b-2e09-42be-bf8c-71ff925e3f6e',2,10,'\0',NULL,NULL),('cbebda3e-0c72-4ffe-a4b3-3c21172ed7a9',NULL,'idp-create-user-if-unique','master','31f8b582-7289-40d7-b003-431ce261eb88',2,20,'\0',NULL,'d14c97e3-e2f7-47e4-bb6f-03fc65b45d79'),('cf3fe8f2-d8f2-4a3e-812a-3566ad6c59d0',NULL,'registration-profile-action','master','5cab487f-1bbe-49d4-a345-bcb3de83a921',0,40,'\0',NULL,NULL),('d1241b23-385a-434c-8fad-74deedc5d757',NULL,'registration-page-form','master','f1ae4295-3036-4d28-a1ce-d377afa73cdb',0,10,'','5cab487f-1bbe-49d4-a345-bcb3de83a921',NULL),('d51e5a14-c864-4bc6-9dd8-e6c6e3fd6854',NULL,'identity-provider-redirector','master','9a9a6d2f-e6ef-4445-84cb-d712aeca2ebb',2,25,'\0',NULL,NULL),('dbc10128-1f34-4685-ac19-88d6025af77f',NULL,'registration-password-action','master','5cab487f-1bbe-49d4-a345-bcb3de83a921',0,50,'\0',NULL,NULL),('dcc2461a-b2fd-4716-9b9d-ac185bbdea9b',NULL,NULL,'master','31f8b582-7289-40d7-b003-431ce261eb88',2,30,'','ebbcf719-47e9-45ca-bdbc-4150c96b1b90',NULL),('e54332ff-a56b-4b0c-9ed4-580b446e13cc',NULL,'reset-credentials-choose-user','shanoir-ng','cf00062f-34c3-416e-9cd4-4abe6c77f029',0,10,'\0',NULL,NULL),('e670a93d-0986-4cf2-a550-aedbe983dae2',NULL,'reset-credential-email','master','c3aa4d33-c17b-4609-a07a-4d56b98b0f92',0,20,'\0',NULL,NULL),('e8862359-a173-4706-b02a-d4687c7f93b2',NULL,'reset-credential-email','shanoir-ng','cf00062f-34c3-416e-9cd4-4abe6c77f029',0,20,'\0',NULL,NULL),('eddef7d7-e0b3-4e84-9227-906ba0b1402b',NULL,'idp-email-verification','master','ebbcf719-47e9-45ca-bdbc-4150c96b1b90',2,20,'\0',NULL,NULL),('eff7f8d5-eb29-4af1-8c9b-501d21a9d946',NULL,NULL,'shanoir-ng','9b8b68d9-5129-4531-a139-20a7b16bdd22',2,30,'','52822ffe-d410-4427-8768-f239c88bb947',NULL),('f03b676b-ca2b-4f5b-92da-68e6c1d451eb',NULL,'auth-otp-form','master','d09990c5-019c-4179-87ae-0b35970586a6',1,20,'\0',NULL,NULL),('f4773e46-8139-42c0-a0f5-95b5d12be81b',NULL,'direct-grant-validate-password','shanoir-ng','0e20ce2d-379e-4b57-bedd-43075a4b4327',0,20,'\0',NULL,NULL),('fe9d227e-92f5-494f-a990-262c8f8292eb',NULL,'direct-grant-validate-otp','shanoir-ng','0e20ce2d-379e-4b57-bedd-43075a4b4327',1,30,'\0',NULL,NULL);
/*!40000 ALTER TABLE `AUTHENTICATION_EXECUTION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `AUTHENTICATION_FLOW`
--

DROP TABLE IF EXISTS `AUTHENTICATION_FLOW`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AUTHENTICATION_FLOW` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ALIAS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `DESCRIPTION` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PROVIDER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'basic-flow',
  `TOP_LEVEL` bit(1) NOT NULL DEFAULT b'0',
  `BUILT_IN` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`ID`),
  KEY `FK_AUTH_FLOW_REALM` (`REALM_ID`),
  CONSTRAINT `FK_AUTH_FLOW_REALM` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `AUTHENTICATION_FLOW`
--

LOCK TABLES `AUTHENTICATION_FLOW` WRITE;
/*!40000 ALTER TABLE `AUTHENTICATION_FLOW` DISABLE KEYS */;
INSERT INTO `AUTHENTICATION_FLOW` VALUES ('0666086a-032d-4a0d-9820-21ad39b84bd7','Browser script forms','Username, password, otp and other auth forms.','shanoir-ng','basic-flow','\0','\0'),('0e20ce2d-379e-4b57-bedd-43075a4b4327','direct grant','OpenID Connect Resource Owner Grant','shanoir-ng','basic-flow','',''),('31f8b582-7289-40d7-b003-431ce261eb88','first broker login','Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account','master','basic-flow','',''),('405ffc08-1ca2-4f5e-85c2-f9e9433dbca4','saml ecp','SAML ECP Profile Authentication Flow','shanoir-ng','basic-flow','',''),('408934cc-53ff-47e0-b438-046fd293e13f','clients','Base authentication for clients','shanoir-ng','client-flow','',''),('4b42546a-e9b6-4af6-ba32-367255b3a16f','forms','Username, password, otp and other auth forms.','shanoir-ng','basic-flow','\0',''),('52822ffe-d410-4427-8768-f239c88bb947','Handle Existing Account','Handle what to do if there is existing account with same email/username like authenticated identity provider','shanoir-ng','basic-flow','\0',''),('5cab487f-1bbe-49d4-a345-bcb3de83a921','registration form','registration form','master','form-flow','\0',''),('61cc90b3-b42d-422e-a116-0f584fc76df3','clients','Base authentication for clients','master','client-flow','',''),('62cce624-2e2e-458b-a4f5-5bec4ccd747b','direct grant','OpenID Connect Resource Owner Grant','master','basic-flow','',''),('6f5b1307-cca2-41cf-95c3-21a22a367089','registration form','registration form','shanoir-ng','form-flow','\0',''),('9a9a6d2f-e6ef-4445-84cb-d712aeca2ebb','browser','browser based authentication','master','basic-flow','',''),('9b8b68d9-5129-4531-a139-20a7b16bdd22','first broker login','Actions taken after first broker login with identity provider account, which is not yet linked to any Keycloak account','shanoir-ng','basic-flow','',''),('a532c395-637a-4283-a455-668df6c946b2','browser','browser based authentication','shanoir-ng','basic-flow','',''),('a5604c9a-cb9f-4379-ab76-5d539977f2db','Verify Existing Account by Re-authentication','Reauthentication of existing account','master','basic-flow','\0',''),('a5bd910d-e98a-4f14-8159-d541f755cfb6','Verify Existing Account by Re-authentication','Reauthentication of existing account','shanoir-ng','basic-flow','\0',''),('c3aa4d33-c17b-4609-a07a-4d56b98b0f92','reset credentials','Reset credentials for a user if they forgot their password or something','master','basic-flow','',''),('c5dd483d-238d-4712-8b8c-749b0840c6bc','saml ecp','SAML ECP Profile Authentication Flow','master','basic-flow','',''),('cf00062f-34c3-416e-9cd4-4abe6c77f029','reset credentials','Reset credentials for a user if they forgot their password or something','shanoir-ng','basic-flow','',''),('d09990c5-019c-4179-87ae-0b35970586a6','forms','Username, password, otp and other auth forms.','master','basic-flow','\0',''),('dbeeb75d-afb2-46e8-8260-f1daae2b3a7a','registration','registration flow','shanoir-ng','basic-flow','',''),('e622576b-2e09-42be-bf8c-71ff925e3f6e','Browser script','browser based authentication','shanoir-ng','basic-flow','','\0'),('ebbcf719-47e9-45ca-bdbc-4150c96b1b90','Handle Existing Account','Handle what to do if there is existing account with same email/username like authenticated identity provider','master','basic-flow','\0',''),('f1ae4295-3036-4d28-a1ce-d377afa73cdb','registration','registration flow','master','basic-flow','','');
/*!40000 ALTER TABLE `AUTHENTICATION_FLOW` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `AUTHENTICATOR_CONFIG`
--

DROP TABLE IF EXISTS `AUTHENTICATOR_CONFIG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AUTHENTICATOR_CONFIG` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ALIAS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_AUTH_REALM` (`REALM_ID`),
  CONSTRAINT `FK_AUTH_REALM` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `AUTHENTICATOR_CONFIG`
--

LOCK TABLES `AUTHENTICATOR_CONFIG` WRITE;
/*!40000 ALTER TABLE `AUTHENTICATOR_CONFIG` DISABLE KEYS */;
INSERT INTO `AUTHENTICATOR_CONFIG` VALUES ('22e6c886-451c-4f70-82b4-bf8ede7214a0','review profile config','master'),('39f4644d-4a3b-4e4b-92c0-d8318aed1fea','review profile config','shanoir-ng'),('9301d0fd-e311-4831-81fd-365a2ec43be7','CheckExpirationDateConfig','shanoir-ng'),('c203e4d4-1e39-45d8-b004-46ff31db5bae','create unique user config','shanoir-ng'),('d14c97e3-e2f7-47e4-bb6f-03fc65b45d79','create unique user config','master');
/*!40000 ALTER TABLE `AUTHENTICATOR_CONFIG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `AUTHENTICATOR_CONFIG_ENTRY`
--

DROP TABLE IF EXISTS `AUTHENTICATOR_CONFIG_ENTRY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `AUTHENTICATOR_CONFIG_ENTRY` (
  `AUTHENTICATOR_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` longtext COLLATE utf8_unicode_ci,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`AUTHENTICATOR_ID`,`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `AUTHENTICATOR_CONFIG_ENTRY`
--

LOCK TABLES `AUTHENTICATOR_CONFIG_ENTRY` WRITE;
/*!40000 ALTER TABLE `AUTHENTICATOR_CONFIG_ENTRY` DISABLE KEYS */;
INSERT INTO `AUTHENTICATOR_CONFIG_ENTRY` VALUES ('22e6c886-451c-4f70-82b4-bf8ede7214a0','missing','update.profile.on.first.login'),('39f4644d-4a3b-4e4b-92c0-d8318aed1fea','missing','update.profile.on.first.login'),('9301d0fd-e311-4831-81fd-365a2ec43be7','/*\n * Template for JavaScript based authenticator\'s.\n * See org.keycloak.authentication.authenticators.browser.ScriptBasedAuthenticatorFactory\n */\n\n// import enum for error lookup\n//AuthenticationFlowError = Java.type(\"org.keycloak.authentication.AuthenticationFlowError\");\n\n/**\n * An example authenticate function.\n *\n * The following variables are available for convenience:\n * user - current user {@see org.keycloak.models.UserModel}\n * realm - current realm {@see org.keycloak.models.RealmModel}\n * session - current KeycloakSession {@see org.keycloak.models.KeycloakSession}\n * httpRequest - current HttpRequest {@see org.jboss.resteasy.spi.HttpRequest}\n * script - current script {@see org.keycloak.models.ScriptModel}\n * LOG - current logger {@see org.jboss.logging.Logger}\n *\n * You one can extract current http request headers via:\n * httpRequest.getHttpHeaders().getHeaderString(\"Forwarded\")\n *\n * @param context {@see org.keycloak.authentication.AuthenticationFlowContext}\n */\nfunction authenticate(context) {\n\n    var username = user ? user.username : \"anonymous\";\n    LOG.info(script.name + \" trace auth for: \" + username);\n\n    var authShouldFail = false;\n    if (authShouldFail) {\n        context.failure(\"INVALID_USER\");\n        return;\n    }\n    \n    if (user.attributes[\'expirationDate\'] !== null && new Date().getTime() > user.attributes[\'expirationDate\'][0]) {\n        user.enabled = false;\n        context.failure(\"USER_DISABLED\");\n        return;\n    }\n\n    context.success();\n}','scriptCode'),('9301d0fd-e311-4831-81fd-365a2ec43be7','Check expiration date on login','scriptDescription'),('9301d0fd-e311-4831-81fd-365a2ec43be7','CheckExpirationDate','scriptName'),('c203e4d4-1e39-45d8-b004-46ff31db5bae','false','require.password.update.after.registration'),('d14c97e3-e2f7-47e4-bb6f-03fc65b45d79','false','require.password.update.after.registration');
/*!40000 ALTER TABLE `AUTHENTICATOR_CONFIG_ENTRY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `BROKER_LINK`
--

DROP TABLE IF EXISTS `BROKER_LINK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `BROKER_LINK` (
  `IDENTITY_PROVIDER` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `STORAGE_PROVIDER_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `BROKER_USER_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `BROKER_USERNAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `TOKEN` text COLLATE utf8_unicode_ci,
  `USER_ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`IDENTITY_PROVIDER`,`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `BROKER_LINK`
--

LOCK TABLES `BROKER_LINK` WRITE;
/*!40000 ALTER TABLE `BROKER_LINK` DISABLE KEYS */;
/*!40000 ALTER TABLE `BROKER_LINK` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT`
--

DROP TABLE IF EXISTS `CLIENT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `FULL_SCOPE_ALLOWED` bit(1) NOT NULL DEFAULT b'0',
  `CLIENT_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NOT_BEFORE` int(11) DEFAULT NULL,
  `PUBLIC_CLIENT` bit(1) NOT NULL DEFAULT b'0',
  `SECRET` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `BASE_URL` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `BEARER_ONLY` bit(1) NOT NULL DEFAULT b'0',
  `MANAGEMENT_URL` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `SURROGATE_AUTH_REQUIRED` bit(1) NOT NULL DEFAULT b'0',
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PROTOCOL` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NODE_REREG_TIMEOUT` int(11) DEFAULT '0',
  `FRONTCHANNEL_LOGOUT` bit(1) NOT NULL DEFAULT b'0',
  `CONSENT_REQUIRED` bit(1) NOT NULL DEFAULT b'0',
  `NAME` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `SERVICE_ACCOUNTS_ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `CLIENT_AUTHENTICATOR_TYPE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ROOT_URL` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `DESCRIPTION` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `REGISTRATION_TOKEN` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `STANDARD_FLOW_ENABLED` bit(1) NOT NULL DEFAULT b'1',
  `IMPLICIT_FLOW_ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `DIRECT_ACCESS_GRANTS_ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `CLIENT_TEMPLATE_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `USE_TEMPLATE_CONFIG` bit(1) NOT NULL DEFAULT b'0',
  `USE_TEMPLATE_SCOPE` bit(1) NOT NULL DEFAULT b'0',
  `USE_TEMPLATE_MAPPERS` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_B71CJLBENV945RB6GCON438AT` (`REALM_ID`,`CLIENT_ID`),
  KEY `FK_CLI_TMPLT_CLIENT` (`CLIENT_TEMPLATE_ID`),
  CONSTRAINT `FK_CLI_TMPLT_CLIENT` FOREIGN KEY (`CLIENT_TEMPLATE_ID`) REFERENCES `CLIENT_TEMPLATE` (`ID`),
  CONSTRAINT `FK_P56CTINXXB9GSK57FO49F9TAC` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT`
--

LOCK TABLES `CLIENT` WRITE;
/*!40000 ALTER TABLE `CLIENT` DISABLE KEYS */;
INSERT INTO `CLIENT` VALUES ('04236224-4e7a-42d0-ba57-821769bd0a6a','','\0','security-admin-console',0,'','03ad3836-9661-445d-b1fb-8a6e2d11344f','/auth/admin/master/console/index.html','\0',NULL,'\0','master',NULL,0,'\0','\0','${client_security-admin-console}','\0','client-secret',NULL,NULL,NULL,'','\0','\0',NULL,'\0','\0','\0'),('1baebc2e-3c0b-4a04-a067-39dac04320aa','','','shanoir-ng-users',0,'\0','70ec913b-8b75-470f-b191-e24025dc1421',NULL,'\0',NULL,'\0','shanoir-ng','openid-connect',-1,'\0','\0',NULL,'','client-secret',NULL,NULL,NULL,'','\0','',NULL,'\0','\0','\0'),('350a782b-05c8-4529-9314-2b5d31ef4918','','\0','admin-cli',0,'','0610e21a-8b4f-4006-8b29-72096bd3342e',NULL,'\0',NULL,'\0','master',NULL,0,'\0','\0','${client_admin-cli}','\0','client-secret',NULL,NULL,NULL,'\0','\0','',NULL,'\0','\0','\0'),('354f7145-851b-4a84-8d27-cb6622d4eb8a','','','master-realm',0,'\0','73d459c8-53aa-4abb-9cff-083bd8ce8f56',NULL,'',NULL,'\0','master',NULL,0,'\0','\0','master Realm','\0','client-secret',NULL,NULL,NULL,'','\0','\0',NULL,'\0','\0','\0'),('47b3eb38-8229-4bbb-a0b0-c37cf2ef6227','','\0','broker',0,'\0','c04a6d3d-8ee5-4b6d-ac44-54843af1f24b',NULL,'\0',NULL,'\0','master',NULL,0,'\0','\0','${client_broker}','\0','client-secret',NULL,NULL,NULL,'','\0','\0',NULL,'\0','\0','\0'),('63cee678-229d-4715-99b7-b319c4cec32d','','\0','account',0,'\0','f07ebb07-c41a-46d1-a433-04e5f5ceef1b','/auth/realms/master/account','\0',NULL,'\0','master',NULL,0,'\0','\0','${client_account}','\0','client-secret',NULL,NULL,NULL,'','\0','\0',NULL,'\0','\0','\0'),('676d9349-500c-4e73-8106-1589f2ac812a','','\0','security-admin-console',0,'','a29094d5-6c24-4d48-90e5-d7e5c21660b4','/auth/admin/shanoir-ng/console/index.html','\0',NULL,'\0','shanoir-ng',NULL,0,'\0','\0','${client_security-admin-console}','\0','client-secret',NULL,NULL,NULL,'','\0','\0',NULL,'\0','\0','\0'),('76d1b923-ed53-405b-b798-d1c8ea62d1fe','','\0','broker',0,'\0','c060a81b-adcc-46a0-92c3-dc4f866347c8',NULL,'\0',NULL,'\0','shanoir-ng',NULL,0,'\0','\0','${client_broker}','\0','client-secret',NULL,NULL,NULL,'','\0','\0',NULL,'\0','\0','\0'),('84284718-4123-4bee-a906-3ecd66bea3d1','','\0','account',0,'\0','2e47235a-b3d2-46ef-a36f-59311b510da9','/auth/realms/shanoir-ng/account','\0',NULL,'\0','shanoir-ng',NULL,0,'\0','\0','${client_account}','\0','client-secret',NULL,NULL,NULL,'','\0','\0',NULL,'\0','\0','\0'),('93635e6b-5f0f-4a37-b304-4337459122aa','','','shanoir-ng-realm',0,'\0','d988935e-0310-43af-b531-ef9377a89251',NULL,'',NULL,'\0','master',NULL,0,'\0','\0','shanoir-ng Realm','\0','client-secret',NULL,NULL,NULL,'','\0','\0',NULL,'\0','\0','\0'),('c62bf86f-3741-4051-aea1-c5469a7d2575','','','shanoir-ng-studies',0,'\0','584a4bec-4683-41e3-a0fb-bfcc436ac84d',NULL,'',NULL,'\0','shanoir-ng','openid-connect',-1,'\0','\0',NULL,'\0','client-secret',NULL,NULL,NULL,'','\0','',NULL,'\0','\0','\0'),('d015be82-be75-46cd-8f39-d9d6c303adff','','\0','admin-cli',0,'','b4090345-428a-4c0c-959e-e2c3f5d20325',NULL,'\0',NULL,'\0','shanoir-ng',NULL,0,'\0','\0','${client_admin-cli}','\0','client-secret',NULL,NULL,NULL,'\0','\0','',NULL,'\0','\0','\0'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','','','shanoir-old',0,'\0','452e7870-c021-43a1-9e35-822a3c0cf395','/auth/realms/shanoir-ng/protocol/saml/clients/shanoir-old','\0',NULL,'\0','shanoir-ng','saml',-1,'','\0',NULL,'\0','client-secret',NULL,NULL,NULL,'','\0','\0',NULL,'\0','\0','\0'),('de02f346-7bb9-4a9e-9871-722fe1381930','','','shanoir-ng-front',0,'','95e5fbd6-f225-48ec-a067-0bb9ca4f7da6',NULL,'\0',NULL,'\0','shanoir-ng','openid-connect',-1,'\0','\0',NULL,'\0','client-secret',NULL,NULL,NULL,'','\0','',NULL,'\0','\0','\0'),('f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','\0','realm-management',0,'\0','8de761e5-3e3a-4c2f-a210-083430bf0ff7',NULL,'',NULL,'\0','shanoir-ng',NULL,0,'\0','\0','${client_realm-management}','\0','client-secret',NULL,NULL,NULL,'','\0','\0',NULL,'\0','\0','\0');
/*!40000 ALTER TABLE `CLIENT` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT_ATTRIBUTES`
--

DROP TABLE IF EXISTS `CLIENT_ATTRIBUTES`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT_ATTRIBUTES` (
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`CLIENT_ID`,`NAME`),
  CONSTRAINT `FK3C47C64BEACCA966` FOREIGN KEY (`CLIENT_ID`) REFERENCES `CLIENT` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT_ATTRIBUTES`
--

LOCK TABLES `CLIENT_ATTRIBUTES` WRITE;
/*!40000 ALTER TABLE `CLIENT_ATTRIBUTES` DISABLE KEYS */;
INSERT INTO `CLIENT_ATTRIBUTES` VALUES ('1baebc2e-3c0b-4a04-a067-39dac04320aa','false','saml_force_name_id_format'),('1baebc2e-3c0b-4a04-a067-39dac04320aa','false','saml.assertion.signature'),('1baebc2e-3c0b-4a04-a067-39dac04320aa','false','saml.authnstatement'),('1baebc2e-3c0b-4a04-a067-39dac04320aa','false','saml.client.signature'),('1baebc2e-3c0b-4a04-a067-39dac04320aa','false','saml.encrypt'),('1baebc2e-3c0b-4a04-a067-39dac04320aa','false','saml.force.post.binding'),('1baebc2e-3c0b-4a04-a067-39dac04320aa','false','saml.multivalued.roles'),('1baebc2e-3c0b-4a04-a067-39dac04320aa','false','saml.server.signature'),('1baebc2e-3c0b-4a04-a067-39dac04320aa','false','saml.server.signature.keyinfo.ext'),('c62bf86f-3741-4051-aea1-c5469a7d2575','false','saml_force_name_id_format'),('c62bf86f-3741-4051-aea1-c5469a7d2575','false','saml.assertion.signature'),('c62bf86f-3741-4051-aea1-c5469a7d2575','false','saml.authnstatement'),('c62bf86f-3741-4051-aea1-c5469a7d2575','false','saml.client.signature'),('c62bf86f-3741-4051-aea1-c5469a7d2575','false','saml.encrypt'),('c62bf86f-3741-4051-aea1-c5469a7d2575','false','saml.force.post.binding'),('c62bf86f-3741-4051-aea1-c5469a7d2575','false','saml.multivalued.roles'),('c62bf86f-3741-4051-aea1-c5469a7d2575','false','saml.server.signature'),('c62bf86f-3741-4051-aea1-c5469a7d2575','false','saml.server.signature.keyinfo.ext'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','http://localhost:8081/Shanoir/saml','saml_assertion_consumer_url_post'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','false','saml_force_name_id_format'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','shanoir-old','saml_idp_initiated_sso_url_name'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','email','saml_name_id_format'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','http://www.w3.org/2001/10/xml-exc-c14n#','saml_signature_canonicalization_method'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','false','saml.assertion.signature'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','false','saml.authnstatement'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','false','saml.client.signature'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','false','saml.encrypt'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','true','saml.force.post.binding'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','false','saml.multivalued.roles'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','true','saml.server.signature'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','false','saml.server.signature.keyinfo.ext'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','KEY_ID','saml.server.signature.keyinfo.xmlSigKeyInfoKeyNameTransformer'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','RSA_SHA256','saml.signature.algorithm'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','MIICpTCCAY0CBgFaYD/GgjANBgkqhkiG9w0BAQsFADAWMRQwEgYDVQQDDAtzaGFub2lyLW9sZDAeFw0xNzAyMjExMDM2MTZaFw0yNzAyMjExMDM3NTZaMBYxFDASBgNVBAMMC3NoYW5vaXItb2xkMIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAkHWxh/BFxJWv4TKE+9zMayxy1HOyQ/na7q0+CAXfUBLBELxTuGSk4BQ3Xve96RyNR936XNtIXDDQRraZZM2F+JtRn+5nI34MosI+6v3uP9HwD/XIzpd2FxeJkr+iLaedVqkohK3ZVXvYBTTJWoRlcO0ZwKI3d6BVGjX9JN0uSSf/A1gJ8rq8aPVd4Iow1MFH5XliF8BIvtazUvZBPz1GjgMmgkzygV1opliGmQOqmLHPdauXUBsWjumIKc/s0RKzscAx6X1AV1xg+ov0Dol+gTY4pPLCnVgf169omnrdqx44wyGgAUJ2qzSdCBrxc2WIyfrfKQLtJWtgyV1mdev2OwIDAQABMA0GCSqGSIb3DQEBCwUAA4IBAQA8tkR/wpMuAavNpyF2FRF+46Q3vQnPMB6dPDkxQzNe5ltnsyBQgrvc4gZQ06E//TzV7HTPwt2nSiu3HfgeMbVSeV0MQ3LUlfQ+MsB/7jCYt2hFcwBUzdh1VVC5Cl8Z0EsjzCga6EPiTZQJK6iyTUdu1T+woK/uZYkCF4ROmaaYcRxSSoZDpfH2hm0hI7spl8+gxs6ubUXHoP87Uf2Bts4scAiPsxPXgDwneazv18a6rVM7n4g6SDrXVQBYi3eGDF5wnEe1v0KGQmnGm5OV8cCiKKfRnEBphRnDKvADd+Gbe6Ph2iU7D7eaETIDaftZwXDCTmhu98iMotV8GCAtSuxc','saml.signing.certificate'),('d66c079f-4e8a-4012-a2b7-6527ee6ae3cc','MIIEowIBAAKCAQEAkHWxh/BFxJWv4TKE+9zMayxy1HOyQ/na7q0+CAXfUBLBELxTuGSk4BQ3Xve96RyNR936XNtIXDDQRraZZM2F+JtRn+5nI34MosI+6v3uP9HwD/XIzpd2FxeJkr+iLaedVqkohK3ZVXvYBTTJWoRlcO0ZwKI3d6BVGjX9JN0uSSf/A1gJ8rq8aPVd4Iow1MFH5XliF8BIvtazUvZBPz1GjgMmgkzygV1opliGmQOqmLHPdauXUBsWjumIKc/s0RKzscAx6X1AV1xg+ov0Dol+gTY4pPLCnVgf169omnrdqx44wyGgAUJ2qzSdCBrxc2WIyfrfKQLtJWtgyV1mdev2OwIDAQABAoIBAGCLyBKqKnav89P2B5qsfpJCKpyClmmH2yIY9pQ2GIFTh2S/+NQLr4Vy/W6i9HGPe3zEhkb45ut2VDxuXSZZlWgJpgvVMWkvHRLJ73LuMwTEMkT4ZzRrVakpOfCx0pngo+7/Lu09ozrWghE4rjmosoM+cy+jOp7gsP/LhVvV1tFcuvBZPQMIkz3YAh7QW/uwnhwvewta+EXYadBgoTY3Gxw3W9X6dxjcbouGrOYURwhsj2icat/YKU1ARrfnBwiyCOebCG07TmtoxRKo7Jfv3Ag6ksuW9QryJJ1unM6ALqJlzwmFoeTN7RQcbJhefxDB+/FLjKmZfsCUISyURVggGUECgYEA7XbIeizTQsDGBwSZiLwvNsvrCL1gQ5m6e8hEVMDMdFJ83vWkUBA7FIrvO15BKGqeIsb3NZAzaosgU3R24cNuESx34l76SVP830KQ+tFLcsZ2P6D9eYT2OYy0j8cqIs58Rxz0FtPz2clqgdChzu3dFdXYzM6YKBlTRZK09xZfKJ8CgYEAm7xqo2Luf76oEZIYPooebHvPBRQcA0UWs5YK/Q2Qp9mxP/GGkKeKLneWabAdkC4OTGCkpplnyDmYXgqZzyht3zPygnV+1C+cYf8oYUVOM20vwN6uiXhJrndhj6qDYrU8hHkfZRzBfHd/8armjUZDaCIlW0/0PHR86AJlvzXPYOUCgYB2zI1BajmK1Mx9aFSzbookXOJJ7gxB5Z/BG5Dr4cSnkTj9hmDc2THldhh3WBE6+hQBDA9TEcBXViEm/0YWbmIbX2bermuC0ezZeQD1Kk4Xrqgr7wvAhr90TStsHtS6sxjDrv2Ciikd1MFfDpRU5PVNXj3nQJgov3rEfL/iOaQDuwKBgHA6a66qkKzGPF/NJGONLTeIUTubrkQ8+YsZSXnq2J/dJPqrlwmPcujhjSdEn0lTp0IrMvxL+Jo1xzOd+BFtVSBQUybofSlz1gJY2kSeqSguOweGccjnQlHT/h6GBoCPkrj0gEdhXZ3QkAcb3WFZjSHx2XHgQGWAnLKlZ6VPpc5xAoGBAKhED9zAI9Bl3oHucxTO2miYrQ4kaUFH/JLmdRccUcIwRkq4IiAKqQmIcanMl+rO/viCd4XLy2Efs1gbcOwPCbx9yZS/nKfU/e3NDvG/vY++2/pJ++D+g9Rr7oGlUX3qf9iZS36fF4K5snT8mib9B9d/QtSb9/Otz6sBfRAguWyf','saml.signing.private.key');
/*!40000 ALTER TABLE `CLIENT_ATTRIBUTES` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT_DEFAULT_ROLES`
--

DROP TABLE IF EXISTS `CLIENT_DEFAULT_ROLES`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT_DEFAULT_ROLES` (
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ROLE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  UNIQUE KEY `UK_8AELWNIBJI49AVXSRTUF6XJOW` (`ROLE_ID`),
  KEY `FK_NUILTS7KLWQW2H8M2B5JOYTKY` (`CLIENT_ID`),
  CONSTRAINT `FK_8AELWNIBJI49AVXSRTUF6XJOW` FOREIGN KEY (`ROLE_ID`) REFERENCES `KEYCLOAK_ROLE` (`ID`),
  CONSTRAINT `FK_NUILTS7KLWQW2H8M2B5JOYTKY` FOREIGN KEY (`CLIENT_ID`) REFERENCES `CLIENT` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT_DEFAULT_ROLES`
--

LOCK TABLES `CLIENT_DEFAULT_ROLES` WRITE;
/*!40000 ALTER TABLE `CLIENT_DEFAULT_ROLES` DISABLE KEYS */;
INSERT INTO `CLIENT_DEFAULT_ROLES` VALUES ('63cee678-229d-4715-99b7-b319c4cec32d','0b9edc64-bb37-447f-960c-0fc2c3c18563'),('63cee678-229d-4715-99b7-b319c4cec32d','6b80ad80-48e1-4a3c-b478-7f53c8329a55'),('84284718-4123-4bee-a906-3ecd66bea3d1','0dd34304-14e1-47c5-9750-52fb0deef76d'),('84284718-4123-4bee-a906-3ecd66bea3d1','6a764793-5b5d-4d7c-9ba2-56215cfad5ff');
/*!40000 ALTER TABLE `CLIENT_DEFAULT_ROLES` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT_IDENTITY_PROV_MAPPING`
--

DROP TABLE IF EXISTS `CLIENT_IDENTITY_PROV_MAPPING`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT_IDENTITY_PROV_MAPPING` (
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `IDENTITY_PROVIDER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `RETRIEVE_TOKEN` bit(1) NOT NULL DEFAULT b'0',
  UNIQUE KEY `UK_7CAELWNIBJI49AVXSRTUF6XJ12` (`IDENTITY_PROVIDER_ID`,`CLIENT_ID`),
  KEY `FK_56ELWNIBJI49AVXSRTUF6XJ23` (`CLIENT_ID`),
  CONSTRAINT `FK_56ELWNIBJI49AVXSRTUF6XJ23` FOREIGN KEY (`CLIENT_ID`) REFERENCES `CLIENT` (`ID`),
  CONSTRAINT `FK_7CELWNIBJI49AVXSRTUF6XJ12` FOREIGN KEY (`IDENTITY_PROVIDER_ID`) REFERENCES `IDENTITY_PROVIDER` (`INTERNAL_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT_IDENTITY_PROV_MAPPING`
--

LOCK TABLES `CLIENT_IDENTITY_PROV_MAPPING` WRITE;
/*!40000 ALTER TABLE `CLIENT_IDENTITY_PROV_MAPPING` DISABLE KEYS */;
/*!40000 ALTER TABLE `CLIENT_IDENTITY_PROV_MAPPING` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT_NODE_REGISTRATIONS`
--

DROP TABLE IF EXISTS `CLIENT_NODE_REGISTRATIONS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT_NODE_REGISTRATIONS` (
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` int(11) DEFAULT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`CLIENT_ID`,`NAME`),
  CONSTRAINT `FK4129723BA992F594` FOREIGN KEY (`CLIENT_ID`) REFERENCES `CLIENT` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT_NODE_REGISTRATIONS`
--

LOCK TABLES `CLIENT_NODE_REGISTRATIONS` WRITE;
/*!40000 ALTER TABLE `CLIENT_NODE_REGISTRATIONS` DISABLE KEYS */;
/*!40000 ALTER TABLE `CLIENT_NODE_REGISTRATIONS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT_SESSION`
--

DROP TABLE IF EXISTS `CLIENT_SESSION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT_SESSION` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REDIRECT_URI` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `STATE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `TIMESTAMP` int(11) DEFAULT NULL,
  `SESSION_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `AUTH_METHOD` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `AUTH_USER_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CURRENT_ACTION` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_B4AO2VCVAT6UKAU74WBWTFQO1` (`SESSION_ID`),
  CONSTRAINT `FK_B4AO2VCVAT6UKAU74WBWTFQO1` FOREIGN KEY (`SESSION_ID`) REFERENCES `USER_SESSION` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT_SESSION`
--

LOCK TABLES `CLIENT_SESSION` WRITE;
/*!40000 ALTER TABLE `CLIENT_SESSION` DISABLE KEYS */;
/*!40000 ALTER TABLE `CLIENT_SESSION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT_SESSION_AUTH_STATUS`
--

DROP TABLE IF EXISTS `CLIENT_SESSION_AUTH_STATUS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT_SESSION_AUTH_STATUS` (
  `AUTHENTICATOR` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `STATUS` int(11) DEFAULT NULL,
  `CLIENT_SESSION` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`CLIENT_SESSION`,`AUTHENTICATOR`),
  CONSTRAINT `AUTH_STATUS_CONSTRAINT` FOREIGN KEY (`CLIENT_SESSION`) REFERENCES `CLIENT_SESSION` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT_SESSION_AUTH_STATUS`
--

LOCK TABLES `CLIENT_SESSION_AUTH_STATUS` WRITE;
/*!40000 ALTER TABLE `CLIENT_SESSION_AUTH_STATUS` DISABLE KEYS */;
/*!40000 ALTER TABLE `CLIENT_SESSION_AUTH_STATUS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT_SESSION_NOTE`
--

DROP TABLE IF EXISTS `CLIENT_SESSION_NOTE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT_SESSION_NOTE` (
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CLIENT_SESSION` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`CLIENT_SESSION`,`NAME`),
  CONSTRAINT `FK5EDFB00FF51C2736` FOREIGN KEY (`CLIENT_SESSION`) REFERENCES `CLIENT_SESSION` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT_SESSION_NOTE`
--

LOCK TABLES `CLIENT_SESSION_NOTE` WRITE;
/*!40000 ALTER TABLE `CLIENT_SESSION_NOTE` DISABLE KEYS */;
/*!40000 ALTER TABLE `CLIENT_SESSION_NOTE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT_SESSION_PROT_MAPPER`
--

DROP TABLE IF EXISTS `CLIENT_SESSION_PROT_MAPPER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT_SESSION_PROT_MAPPER` (
  `PROTOCOL_MAPPER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CLIENT_SESSION` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`CLIENT_SESSION`,`PROTOCOL_MAPPER_ID`),
  CONSTRAINT `FK_33A8SGQW18I532811V7O2DK89` FOREIGN KEY (`CLIENT_SESSION`) REFERENCES `CLIENT_SESSION` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT_SESSION_PROT_MAPPER`
--

LOCK TABLES `CLIENT_SESSION_PROT_MAPPER` WRITE;
/*!40000 ALTER TABLE `CLIENT_SESSION_PROT_MAPPER` DISABLE KEYS */;
/*!40000 ALTER TABLE `CLIENT_SESSION_PROT_MAPPER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT_SESSION_ROLE`
--

DROP TABLE IF EXISTS `CLIENT_SESSION_ROLE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT_SESSION_ROLE` (
  `ROLE_ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `CLIENT_SESSION` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`CLIENT_SESSION`,`ROLE_ID`),
  CONSTRAINT `FK_11B7SGQW18I532811V7O2DV76` FOREIGN KEY (`CLIENT_SESSION`) REFERENCES `CLIENT_SESSION` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT_SESSION_ROLE`
--

LOCK TABLES `CLIENT_SESSION_ROLE` WRITE;
/*!40000 ALTER TABLE `CLIENT_SESSION_ROLE` DISABLE KEYS */;
/*!40000 ALTER TABLE `CLIENT_SESSION_ROLE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT_TEMPLATE`
--

DROP TABLE IF EXISTS `CLIENT_TEMPLATE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT_TEMPLATE` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `DESCRIPTION` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `PROTOCOL` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `FULL_SCOPE_ALLOWED` bit(1) NOT NULL DEFAULT b'0',
  `CONSENT_REQUIRED` bit(1) NOT NULL DEFAULT b'0',
  `STANDARD_FLOW_ENABLED` bit(1) NOT NULL DEFAULT b'1',
  `IMPLICIT_FLOW_ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `DIRECT_ACCESS_GRANTS_ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `SERVICE_ACCOUNTS_ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `FRONTCHANNEL_LOGOUT` bit(1) NOT NULL DEFAULT b'0',
  `BEARER_ONLY` bit(1) NOT NULL DEFAULT b'0',
  `PUBLIC_CLIENT` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_CLI_TEMPLATE` (`REALM_ID`,`NAME`),
  CONSTRAINT `FK_REALM_CLI_TMPLT` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT_TEMPLATE`
--

LOCK TABLES `CLIENT_TEMPLATE` WRITE;
/*!40000 ALTER TABLE `CLIENT_TEMPLATE` DISABLE KEYS */;
/*!40000 ALTER TABLE `CLIENT_TEMPLATE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT_TEMPLATE_ATTRIBUTES`
--

DROP TABLE IF EXISTS `CLIENT_TEMPLATE_ATTRIBUTES`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT_TEMPLATE_ATTRIBUTES` (
  `TEMPLATE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`TEMPLATE_ID`,`NAME`),
  CONSTRAINT `FK_CL_TEMPL_ATTR_TEMPL` FOREIGN KEY (`TEMPLATE_ID`) REFERENCES `CLIENT_TEMPLATE` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT_TEMPLATE_ATTRIBUTES`
--

LOCK TABLES `CLIENT_TEMPLATE_ATTRIBUTES` WRITE;
/*!40000 ALTER TABLE `CLIENT_TEMPLATE_ATTRIBUTES` DISABLE KEYS */;
/*!40000 ALTER TABLE `CLIENT_TEMPLATE_ATTRIBUTES` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CLIENT_USER_SESSION_NOTE`
--

DROP TABLE IF EXISTS `CLIENT_USER_SESSION_NOTE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CLIENT_USER_SESSION_NOTE` (
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CLIENT_SESSION` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`CLIENT_SESSION`,`NAME`),
  CONSTRAINT `FK_CL_USR_SES_NOTE` FOREIGN KEY (`CLIENT_SESSION`) REFERENCES `CLIENT_SESSION` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CLIENT_USER_SESSION_NOTE`
--

LOCK TABLES `CLIENT_USER_SESSION_NOTE` WRITE;
/*!40000 ALTER TABLE `CLIENT_USER_SESSION_NOTE` DISABLE KEYS */;
/*!40000 ALTER TABLE `CLIENT_USER_SESSION_NOTE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `COMPONENT`
--

DROP TABLE IF EXISTS `COMPONENT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `COMPONENT` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PARENT_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PROVIDER_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PROVIDER_TYPE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `SUB_TYPE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_COMPONENT_REALM` (`REALM_ID`),
  CONSTRAINT `FK_COMPONENT_REALM` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `COMPONENT`
--

LOCK TABLES `COMPONENT` WRITE;
/*!40000 ALTER TABLE `COMPONENT` DISABLE KEYS */;
INSERT INTO `COMPONENT` VALUES ('00168b5b-6755-406c-a691-c6c232dbe6a4','Allowed Protocol Mapper Types','shanoir-ng','allowed-protocol-mappers','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','shanoir-ng','authenticated'),('1bd29ecf-fbd2-4666-9065-8ff87eff59e9','Trusted Hosts','shanoir-ng','trusted-hosts','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','shanoir-ng','anonymous'),('200757dc-8bc6-45e4-acb4-05ed74331e95','Allowed Protocol Mapper Types','master','allowed-protocol-mappers','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','master','anonymous'),('24140d0b-0eab-4852-9e62-3acaa43fe3f2','Allowed Client Templates','shanoir-ng','allowed-client-templates','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','shanoir-ng','authenticated'),('3dbbae4f-7678-4eb9-9dd0-e83755b3adda','Trusted Hosts','master','trusted-hosts','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','master','anonymous'),('403b0ec4-3f3b-41bc-9396-81ff093c8399','Allowed Client Templates','master','allowed-client-templates','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','master','anonymous'),('4ac1fff1-9131-4dc5-b408-63e6c3c6b22f','Consent Required','shanoir-ng','consent-required','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','shanoir-ng','anonymous'),('507b7728-237e-440d-8d6b-d8509400ab49','Max Clients Limit','shanoir-ng','max-clients','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','shanoir-ng','anonymous'),('543c7192-e05f-43fc-8f0f-2f78dfeaacb1','rsa-generated','master','rsa-generated','org.keycloak.keys.KeyProvider','master',NULL),('8316c2fb-f4d4-412c-b562-0640cfa99313','Allowed Client Templates','shanoir-ng','allowed-client-templates','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','shanoir-ng','anonymous'),('8abe5d6c-32b3-4d94-93a6-34d9a259a66e','Consent Required','master','consent-required','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','master','anonymous'),('9263c31b-aa7a-4710-aab8-e3fe980b4e2b','Allowed Protocol Mapper Types','master','allowed-protocol-mappers','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','master','authenticated'),('a0f21ae7-cc40-46c3-9492-a6ce55100999','Full Scope Disabled','master','scope','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','master','anonymous'),('a3891d31-da56-4211-933d-4640fada3018','hmac-generated','shanoir-ng','hmac-generated','org.keycloak.keys.KeyProvider','shanoir-ng',NULL),('c2890beb-1ba7-41dc-9e2e-52e751f3780a','rsa-generated','shanoir-ng','rsa-generated','org.keycloak.keys.KeyProvider','shanoir-ng',NULL),('e81b3163-5334-4f3c-ad3d-d67ca541ec82','Allowed Protocol Mapper Types','shanoir-ng','allowed-protocol-mappers','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','shanoir-ng','anonymous'),('e9debfe4-7264-4c9d-92da-48dde394883a','Full Scope Disabled','shanoir-ng','scope','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','shanoir-ng','anonymous'),('eb27283a-fefa-450c-926c-58101fb28101','Allowed Client Templates','master','allowed-client-templates','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','master','authenticated'),('eb6b6f98-190d-48eb-bded-4213abc879ea','hmac-generated','master','hmac-generated','org.keycloak.keys.KeyProvider','master',NULL),('ec401e7b-a163-4cfb-986c-48bf2a190fc2','Max Clients Limit','master','max-clients','org.keycloak.services.clientregistration.policy.ClientRegistrationPolicy','master','anonymous');
/*!40000 ALTER TABLE `COMPONENT` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `COMPONENT_CONFIG`
--

DROP TABLE IF EXISTS `COMPONENT_CONFIG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `COMPONENT_CONFIG` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `COMPONENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(4000) CHARACTER SET utf8 DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_COMPONENT_CONFIG` (`COMPONENT_ID`),
  CONSTRAINT `FK_COMPONENT_CONFIG` FOREIGN KEY (`COMPONENT_ID`) REFERENCES `COMPONENT` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `COMPONENT_CONFIG`
--

LOCK TABLES `COMPONENT_CONFIG` WRITE;
/*!40000 ALTER TABLE `COMPONENT_CONFIG` DISABLE KEYS */;
INSERT INTO `COMPONENT_CONFIG` VALUES ('00d6bef1-7217-46aa-9970-6534f674557f','200757dc-8bc6-45e4-acb4-05ed74331e95','allowed-protocol-mapper-types','oidc-sha256-pairwise-sub-mapper'),('01bc2ef9-e321-45d3-9a79-7df8b9979663','e81b3163-5334-4f3c-ad3d-d67ca541ec82','allowed-protocol-mapper-types','oidc-full-name-mapper'),('037ef0ea-fd19-44e0-b8e2-215f4502421e','00168b5b-6755-406c-a691-c6c232dbe6a4','allowed-protocol-mapper-types','oidc-sha256-pairwise-sub-mapper'),('1e3c6d81-f097-4ab2-81be-6cc5e5591439','eb6b6f98-190d-48eb-bded-4213abc879ea','secret','mUAxezWfy4lY1WoWm3uHHZ7nx-0BctSHT172SAd1Wzo'),('24b0e31d-ece4-48a7-8ef4-7da1884517d5','9263c31b-aa7a-4710-aab8-e3fe980b4e2b','consent-required-for-all-mappers','true'),('26f7acae-306f-4ce0-8c54-9fe3d1e41d0b','e81b3163-5334-4f3c-ad3d-d67ca541ec82','allowed-protocol-mapper-types','saml-role-list-mapper'),('279fb2f3-22c1-4618-9768-078ef9e46a9b','e81b3163-5334-4f3c-ad3d-d67ca541ec82','allowed-protocol-mapper-types','oidc-sha256-pairwise-sub-mapper'),('39c25af4-79f5-4590-96f2-048fd7668d02','543c7192-e05f-43fc-8f0f-2f78dfeaacb1','priority','100'),('3ce9403e-3105-4815-a6a8-601e39f77da6','9263c31b-aa7a-4710-aab8-e3fe980b4e2b','allowed-protocol-mapper-types','saml-user-property-mapper'),('46211cf0-cd59-4bf6-a436-f3c91359445d','00168b5b-6755-406c-a691-c6c232dbe6a4','allowed-protocol-mapper-types','saml-user-property-mapper'),('497122a8-75a7-4ebe-ac98-7e8476096eb0','200757dc-8bc6-45e4-acb4-05ed74331e95','consent-required-for-all-mappers','true'),('4d1156d1-d501-4c78-99d6-085f6c1d804e','eb6b6f98-190d-48eb-bded-4213abc879ea','kid','2696bf21-3146-42b3-993f-4b5d9287efaa'),('4ebf9bee-e097-440b-9bc2-2db30403b594','00168b5b-6755-406c-a691-c6c232dbe6a4','allowed-protocol-mapper-types','oidc-usermodel-attribute-mapper'),('547847d6-f81f-446d-9d4d-0abe5c3b5a90','9263c31b-aa7a-4710-aab8-e3fe980b4e2b','allowed-protocol-mapper-types','saml-role-list-mapper'),('55d56963-1beb-40ad-969a-242dd5eb53aa','a3891d31-da56-4211-933d-4640fada3018','kid','0559a0b1-b409-410a-8eaa-e5e6c9fa5141'),('5966af70-c1a7-474d-beac-6c3aa43785b6','1bd29ecf-fbd2-4666-9065-8ff87eff59e9','client-uris-must-match','true'),('5f21c90e-eda0-43a8-8643-68b02ba0c67b','200757dc-8bc6-45e4-acb4-05ed74331e95','allowed-protocol-mapper-types','saml-role-list-mapper'),('6120de27-849e-4369-b9c4-8c1ad80c1f51','9263c31b-aa7a-4710-aab8-e3fe980b4e2b','allowed-protocol-mapper-types','oidc-usermodel-attribute-mapper'),('62f690f6-d5d3-4f4e-9938-97663b892c86','c2890beb-1ba7-41dc-9e2e-52e751f3780a','priority','100'),('65755326-a9bd-4db0-ac74-5e503190defd','200757dc-8bc6-45e4-acb4-05ed74331e95','allowed-protocol-mapper-types','oidc-usermodel-property-mapper'),('6b831b2c-5efe-4cbe-b352-bbe6a3352b3f','1bd29ecf-fbd2-4666-9065-8ff87eff59e9','host-sending-registration-request-must-match','true'),('6cef0009-eaf3-458d-a033-6881ce5eb793','00168b5b-6755-406c-a691-c6c232dbe6a4','allowed-protocol-mapper-types','saml-role-list-mapper'),('6fcda23b-8e03-470a-8ba9-1c7d8e04c334','e81b3163-5334-4f3c-ad3d-d67ca541ec82','allowed-protocol-mapper-types','oidc-address-mapper'),('71c9f147-4591-4bdf-843b-a61b1f6de01f','9263c31b-aa7a-4710-aab8-e3fe980b4e2b','allowed-protocol-mapper-types','oidc-full-name-mapper'),('739df5a8-fd38-453b-9e70-81d9c0b8a10d','e81b3163-5334-4f3c-ad3d-d67ca541ec82','allowed-protocol-mapper-types','oidc-usermodel-attribute-mapper'),('7558add3-d182-4f7f-a3df-8526d9270cf6','543c7192-e05f-43fc-8f0f-2f78dfeaacb1','privateKey','MIIEpAIBAAKCAQEAuEJCCZcSsdobSIKuTe1s7l6ps1JYsLuhjjLKHcT/PS06SVxjCBKg7dBHZj5ykN7++hv6zaDaXy5nJRM3HPsJNd9Y9IXkgOig62epi/JPzN7e4uowZA72o7i5TtrDxSSVBica+QZE0qpq6okHFgt8NmfNaTTm2pa2m6yFHnmsZr+nAbKylyNoSPOTuJpHLjztma86NBPlKPGxCISTbTihGY8iL+dU7WJNnI5wniLTXTJ4XZb8iaU0h1pzrcI0zEPBTv4B01p7xK5jFf7WOC1TBfvC0D6kSEzk9cflOKGY1qymrjmg6RbVrVWOBOoxb9AFD0joXJajRrltoU2lEMsRVwIDAQABAoIBAQCfhp3t96Hz4UVlzd5jEYASEFsUKeMngAsrNcXPaFMjABlKknYa8yXpPz3Rw5ck3iwbFkZZ6T2m0mHys2Qv7xfWUHyPl6xx/vXzWVhtFWkJkpueEeu7dUkMDk5Bex2rfTSYkxrDJy5GxcyzKde8ukzcPZqVz3tfHMUUmpTWAbnFaghdeT//Xpst0OdEMgLKMgfda2mO6ix+0dj2QVJ6mj8Ib40lyH7TjztC64C1Vcj5ewBFvbEUSga8i0sfj+t8H7YsHKZngbLg3VYoXCzV8pnDMwHmwtC4WkpGb/OdSo7PWzsKDU1zTygNctXXoPLZoa95t6fV/2eu/C5HBXNM3zphAoGBAN4wZB2hyFedw7TB5bV6IHJrLHIXReSAShz/AxMbxLmCtDe38Bn2nek5EU52OiebRha1e/0+kUTjpGC3OqvlA4YjX+6ZPXA00cfrjH67D/JDjFiFO24P6F+rBSe8VyF0M+VSqKEOak5O1KqcmFCnjoRcBYboBNNkEpalgf/PFv4zAoGBANRMQWaXn+v+rFHdj05i7lx3v/LIojRgsJEYT/VRmAcfrXc/wl4C6/T9zFgq4BsqmPDyxqr3uMQP11GOVDsVXX5Oj+7J9603rpcPuelkGpT65srtj4ym+PchdWYF5Cf324RaLNuKV8zGrVeDJLsR53pninrSgkQLa8iV4k1pCPRNAoGBAI3UQfQQU3xqPoThyKnhPVkMRKDHblv/8E82EVZfQWJRpoxyo5dPL1myjfOR0Gl72m+ghXcQO1bIISODkrYrR+aWKiR6OReo/8gn+dTjv7gbmjjfQyJskR6QYogM12mJbZ8S9oVoWD+IXoRR1YCWlIi03OpSrNHrbVowaB1Mqha1AoGALpT81LH4WSPsU5o105FJk+iCqZb0YGHG3E9lXAKnRQjiWwQWdeJWO89kgwQFYHCcVmIuzVzFod+H81EzkjNB7HD4jACs48kP+f1tYZShcIcNFQHDOa15e2BQDi7EvTBZblUK+rEESt5dMpKIALQRAn6tl/2Qr7WB1OVuXmXpnRECgYASVQypiLWhz3AfNzkgJ7F91PFvNIMdyRkpxG5UHQRJVTRyTrWE//QybGJ/SO/fDveuaG9oiqnxFY0ZYHM91DVdh1mcHPXNw/IjItl05m3upGwfcmqMSaR328bHosVAxSGZ9T6Ju3QZUNViyab1hzN5x9zAOEM4rTjy2MpR0yp3uQ=='),('774a8b62-4072-46e9-aeff-138c7f063d07','e81b3163-5334-4f3c-ad3d-d67ca541ec82','allowed-protocol-mapper-types','oidc-usermodel-property-mapper'),('7a009665-a31e-4d39-8cfe-eed39148401c','e81b3163-5334-4f3c-ad3d-d67ca541ec82','consent-required-for-all-mappers','true'),('7baaf7f0-0305-4d0c-9fce-1931595c5842','9263c31b-aa7a-4710-aab8-e3fe980b4e2b','allowed-protocol-mapper-types','saml-user-attribute-mapper'),('7e742759-4262-401b-9dc1-7e410f8d4d33','00168b5b-6755-406c-a691-c6c232dbe6a4','allowed-protocol-mapper-types','oidc-address-mapper'),('800fbe4c-5d9d-40f9-a7a3-19943e1ffad7','3dbbae4f-7678-4eb9-9dd0-e83755b3adda','client-uris-must-match','true'),('8c8475d7-bd61-488e-9c4b-52f54b3d7141','9263c31b-aa7a-4710-aab8-e3fe980b4e2b','allowed-protocol-mapper-types','oidc-usermodel-property-mapper'),('8ca72cc2-9c46-4074-8683-32eda3f8bdfc','200757dc-8bc6-45e4-acb4-05ed74331e95','allowed-protocol-mapper-types','oidc-full-name-mapper'),('947d88d6-90c9-40d9-9800-52c582cc2111','543c7192-e05f-43fc-8f0f-2f78dfeaacb1','certificate','MIICmzCCAYMCBgFbpdA+ijANBgkqhkiG9w0BAQsFADARMQ8wDQYDVQQDDAZtYXN0ZXIwHhcNMTcwNDI1MTU1MDM5WhcNMjcwNDI1MTU1MjE5WjARMQ8wDQYDVQQDDAZtYXN0ZXIwggEiMA0GCSqGSIb3DQEBAQUAA4IBDwAwggEKAoIBAQC4QkIJlxKx2htIgq5N7WzuXqmzUliwu6GOMsodxP89LTpJXGMIEqDt0EdmPnKQ3v76G/rNoNpfLmclEzcc+wk131j0heSA6KDrZ6mL8k/M3t7i6jBkDvajuLlO2sPFJJUGJxr5BkTSqmrqiQcWC3w2Z81pNObalrabrIUeeaxmv6cBsrKXI2hI85O4mkcuPO2Zrzo0E+Uo8bEIhJNtOKEZjyIv51TtYk2cjnCeItNdMnhdlvyJpTSHWnOtwjTMQ8FO/gHTWnvErmMV/tY4LVMF+8LQPqRITOT1x+U4oZjWrKauOaDpFtWtVY4E6jFv0AUPSOhclqNGuW2hTaUQyxFXAgMBAAEwDQYJKoZIhvcNAQELBQADggEBAHPLWCvsZ62jLhs5bDjfP5E/eem2JpSrnOwItpZtDzjFAO9C74FJ/rdbrBjfmwhXcUZhsYduOiV3eAF6nzJ0Kkm011ot86gwcicFhJOiI60yytsXPJ1wey5RqMORgAxNRc1M95y+TCXVWytGW39MBd8BusSQCbDPzhe1muxDkOJotyss6lGdaltujJHkQk7WzcV6SVvDENKOTpfHTWfxqb8DeXZ9T8SqQ2ZWH/bd+WIao9/qRcFZa8qJyLUqwa3Xt+TtDMIBOqiIhRHYUxVGx/TQBjb/WDXd1LRFJ7RTgAhotWiLDWJq3omBKgiOshJ8qzCq5fRKa6mAqQhEUD5kaVY='),('984f3d3d-450c-4a35-bb0f-7bf6b8ec7f85','00168b5b-6755-406c-a691-c6c232dbe6a4','allowed-protocol-mapper-types','oidc-usermodel-property-mapper'),('9abd0004-4d32-4ffc-8caa-02fbbc204fe8','200757dc-8bc6-45e4-acb4-05ed74331e95','allowed-protocol-mapper-types','saml-user-attribute-mapper'),('9b254e2f-6233-4dfd-9d35-278898e0b3fc','c2890beb-1ba7-41dc-9e2e-52e751f3780a','certificate','MIICozCCAYsCBgFbpdDpgDANBgkqhkiG9w0BAQsFADAVMRMwEQYDVQQDDApzaGFub2lyLW5nMB4XDTE3MDQyNTE1NTEyM1oXDTI3MDQyNTE1NTMwM1owFTETMBEGA1UEAwwKc2hhbm9pci1uZzCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoCggEBAKr+dB024Wl6IDKyG4N0kgz8gLdtHkwos7X6tr1NSeWuIh4jmDXdighKS4jb2ImcairwAUnBkzbLLfgeVhwc6u5lEgboWRiz0YcVVUupK1iiLj6+/sQy2ezUB7LIXUuoqf+GvBPM2cvouh36hgdMLVXd8zQjMe/tgEACqqY3NIOBAq4BmCA8Bk2oYb9P1bsLDGXTckwnKreMoSXzDuoedc4lwIrNBNYO4Up07GHPVFGCLJM94n2Gw+TJwQUPQCT7RHzOKsN2/xH0DqOE617iatRFCqaDNXMxmVBZMiopSRLYqGZfWdeE2aYfb0F3pTcHiA3WXMXg3b8TE5un9vzYTrkCAwEAATANBgkqhkiG9w0BAQsFAAOCAQEAbr3bkMomEa4D6XnrayHWZSQz+AAUTShIIw9AKmqFme8TYzHewF/9RoK4bv6qpxPseEHAe/Q6G4PBh14OVYQjvHzxehvr+xlzUIT1Emu+MnYTzX7qwBlr3EvrPNOnHZy9ImxzqIJS78cRuE3FTLLWLECD6XVzemKQUm8Qd6Vur/09wLYX9W8E76BWl2PRc/oTGCe0IR1f3Sx7Y8CCTH+KZrWN1JLulIj/nIOh1wchp9Oa6zgzh1oXWMSzY2StxH/Mj5FI9xwqD2FIm1+j0Ve90QMtuTA4/tnihXoeYddwg5MtFaxtfifqBAW0WXNs8w29T8awcnW4yiopuJn+lOMEWA=='),('9cfe2813-0619-4b7f-a4af-34ad617cc8e1','e81b3163-5334-4f3c-ad3d-d67ca541ec82','allowed-protocol-mapper-types','saml-user-attribute-mapper'),('a01a4ea8-9346-4e36-8b5c-8062d6c877a7','9263c31b-aa7a-4710-aab8-e3fe980b4e2b','allowed-protocol-mapper-types','oidc-sha256-pairwise-sub-mapper'),('a0a608ab-64e7-4c50-b562-091c3f95930c','00168b5b-6755-406c-a691-c6c232dbe6a4','allowed-protocol-mapper-types','oidc-full-name-mapper'),('a3ca4f1a-f883-4a36-8e6f-0a184c72650c','3dbbae4f-7678-4eb9-9dd0-e83755b3adda','host-sending-registration-request-must-match','true'),('bcfb5421-b4e7-4bfc-99ee-fbf465abc98b','e81b3163-5334-4f3c-ad3d-d67ca541ec82','allowed-protocol-mapper-types','saml-user-property-mapper'),('c7ff92fc-bde3-4321-bbc8-ff69c2e7a3b3','c2890beb-1ba7-41dc-9e2e-52e751f3780a','privateKey','MIIEowIBAAKCAQEAqv50HTbhaXogMrIbg3SSDPyAt20eTCiztfq2vU1J5a4iHiOYNd2KCEpLiNvYiZxqKvABScGTNsst+B5WHBzq7mUSBuhZGLPRhxVVS6krWKIuPr7+xDLZ7NQHsshdS6ip/4a8E8zZy+i6HfqGB0wtVd3zNCMx7+2AQAKqpjc0g4ECrgGYIDwGTahhv0/VuwsMZdNyTCcqt4yhJfMO6h51ziXAis0E1g7hSnTsYc9UUYIskz3ifYbD5MnBBQ9AJPtEfM4qw3b/EfQOo4TrXuJq1EUKpoM1czGZUFkyKilJEtioZl9Z14TZph9vQXelNweIDdZcxeDdvxMTm6f2/NhOuQIDAQABAoIBAELEdKZrpXzAGQ5yiVe9DsJPXhtBWlE2m2V1biFgqngqlFvcXjS6OomeKPxZi6XAE5yMdpRMJ6V/lIWLouf7SqingnB8DT4eCLSMWe/fMHO9b/1EKsqFaJ6W9CKVJgjC5Q5Hl+zLUkfSqcnG5sq2rgUXUz2KeJdMz4UvBV89sx2dfehMavGr6Ek4R3xhN0EhT1xDE8auGfbJpgy2h2eSTTbPOlyptOK/uh5lNtI6vPDIOWgUWeVGzshLT1VVv4lckSQzMwOh5RrOBUfINj1+s9DlxRI65HqsqI6pnVQuUbgH6HECBC3mdmViy+EyJXtqvn+RLN7cDeAjGpMUIlwcqOECgYEA/hVweNhVdKaZhpONbe/eLUgR+DC/BALDHIssP6Iyuq196vPu9Dza0nHyCHhljojP4wptvWCp8rayseRYHgXldxnzFhu1caXfOBmuotZZagfCbDik7ENePeOxYuEkEFCLJpKZp+B8FMHIROsK+RPIyiDMPR3HjD6w7mCo0jG+Hq0CgYEArEiXpv40D0U20x8HBBI8xdnDzClaf9olSRPl67DnnxsCTEbbAhaWEU40NRbLmV8VYlIxtA1jtY7oLzvgCmGM0uFxG238AgVPXCwvf3pM2LULkprEJgmUTNh0tbegixuPtVF07oeLVk9uLoTxQeaKAxwfBwbTn7CsJbBV3Pucbb0CgYEAh7huBq8QHqprMZ6ZmlLZq/hmWNu4/Ox4ykNxFGCQlnTb4OgCCQaEoIrrDSk6nkNoQE+uxMzgmtyKYUmm5AqkQtViqrmMVzOSI8ZcKXEz+9Y/PU2Ykvd9XLPGahn3CJvtaGJAn5Li1LeaQW4Hw3qU85tHXZINqFVIb1zEcNrPQlUCgYAI+Ycc5eUkXoCqotUkuyZ7UArJGeZi/qQumGDD1GVcfnWyL4a9JxwOLrq9gUB97twYURvRu1jUXmtt+nqDT+2cFq5jtWkJWel2bb7TrzVNnLDLpFBPgvQ8xJHf3X75gCgOU/35Zwo1JtIgjkBbJH1QCCj9nrdrfDKbjAINDvZpKQKBgCikEW+CFtrF4s1KohwMukI1pIhX5WpQr7cTTyCNWCqbHn3wjXUc7MonS1wWGC8+iCSAfji7xLK8bbL2o6CrZPCbw8ZRXhhym2f/VXsYLzCIVwKqreT0PJO2RIMHLQ3mtoTl+ym7z/Z/xs5GNpIoC7xEeh601It2WXc9VzbZt9pN'),('ced886f4-ddca-4f5c-ac95-3ccad658aa17','200757dc-8bc6-45e4-acb4-05ed74331e95','allowed-protocol-mapper-types','oidc-usermodel-attribute-mapper'),('d4f7b1c2-1528-4832-84f5-8d2156761c4d','200757dc-8bc6-45e4-acb4-05ed74331e95','allowed-protocol-mapper-types','oidc-address-mapper'),('dacf41fb-8531-448b-a568-9c9e10becd73','00168b5b-6755-406c-a691-c6c232dbe6a4','allowed-protocol-mapper-types','saml-user-attribute-mapper'),('df84937a-2deb-4a16-a3f9-8b019e694e13','a3891d31-da56-4211-933d-4640fada3018','secret','vsLfHGTzfhbjA1jEUzBB-2QkSf25iCUaVPxY9VXDZMA'),('e4476fb5-b4bb-4e12-85b6-48b3d94cc0ee','9263c31b-aa7a-4710-aab8-e3fe980b4e2b','allowed-protocol-mapper-types','oidc-address-mapper'),('e8e0f797-a671-49d1-8ac3-873ac4d4ad4b','eb6b6f98-190d-48eb-bded-4213abc879ea','priority','100'),('ee687209-92f8-4ff1-83c5-aba9cd994064','00168b5b-6755-406c-a691-c6c232dbe6a4','consent-required-for-all-mappers','true'),('effb7929-5d28-493a-bf60-303fdb4260cc','a3891d31-da56-4211-933d-4640fada3018','priority','100'),('f863078e-fb6c-4b3e-bc00-d4647a81e396','507b7728-237e-440d-8d6b-d8509400ab49','max-clients','200'),('f99ce9b3-9d75-4ae1-a211-a98d5ae38571','ec401e7b-a163-4cfb-986c-48bf2a190fc2','max-clients','200'),('fa3285ec-5c05-4d9b-80f0-d1669a78bb85','200757dc-8bc6-45e4-acb4-05ed74331e95','allowed-protocol-mapper-types','saml-user-property-mapper');
/*!40000 ALTER TABLE `COMPONENT_CONFIG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `COMPOSITE_ROLE`
--

DROP TABLE IF EXISTS `COMPOSITE_ROLE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `COMPOSITE_ROLE` (
  `COMPOSITE` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CHILD_ROLE` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  KEY `FK_A63WVEKFTU8JO1PNJ81E7MCE2` (`COMPOSITE`),
  KEY `FK_GR7THLLB9LU8Q4VQA4524JJY8` (`CHILD_ROLE`),
  CONSTRAINT `FK_A63WVEKFTU8JO1PNJ81E7MCE2` FOREIGN KEY (`COMPOSITE`) REFERENCES `KEYCLOAK_ROLE` (`ID`),
  CONSTRAINT `FK_GR7THLLB9LU8Q4VQA4524JJY8` FOREIGN KEY (`CHILD_ROLE`) REFERENCES `KEYCLOAK_ROLE` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `COMPOSITE_ROLE`
--

LOCK TABLES `COMPOSITE_ROLE` WRITE;
/*!40000 ALTER TABLE `COMPOSITE_ROLE` DISABLE KEYS */;
INSERT INTO `COMPOSITE_ROLE` VALUES ('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','70c9ed3c-9cd8-440d-b74d-4eee983ca18c'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','30589ea4-b0bc-4043-99f3-d5e19ec0e7aa'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','e61dc489-d8dc-4570-8c30-e3acd215de0c'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','24ea9939-e0fa-456e-8d47-e0afa4a858c1'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','df0e2d45-e4b5-4524-a472-5e71fe083478'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','15ca2839-51d6-4d56-ba98-b819294fbf65'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','a616474e-6553-4f3d-87aa-b4e49e512318'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','4c617019-2440-453d-82c4-421215ff8e7c'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','b02f324c-6456-40d7-a40c-5c0db0704021'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','19fd14f5-914b-4851-95cc-c86019057f17'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','03587e78-8443-44f2-8e58-24c4965780ac'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','d884d9b1-5599-47c5-94e7-535d36485ecf'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','4272b7d4-993b-416f-99cb-5f5afecc4be5'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','1a62560c-30bc-4d26-90c6-fba5a99854e2'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','b6831ef9-03e5-4c95-a6e7-f3df303cb6e8'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','634480a6-de33-466b-aa10-13624ca726b2'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','9d907672-9245-4dd6-98ec-b30fa1c9abfd'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','8cdfa9b8-2e8d-473f-a2a0-d4581b5c9ddc'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','2e265c4c-da6b-457f-87ce-fa97ecef929b'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','2649233c-3f3e-4ded-a058-ef8e61ec2daa'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','4d8ad343-bce7-4b7f-9cdc-e5ee914e3d28'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','28f32fbc-a01d-4f7a-b5ca-e78f059d33e1'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','ba269dab-fbb4-4fdf-ac82-4a0629e86525'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','21ce904e-16de-473a-88a3-78ac4920f0c9'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','98da76c9-9cb5-4163-a4c3-369e38429680'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','e481353f-0ce4-40db-bce4-cb6b3ad0ada2'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','1ab604b6-57c9-481f-bf50-57256243bc7b'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','1f6fad9e-376b-45d2-9c38-db3d1f7c9ac9'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','aad00e99-e8a5-48d0-a350-ed9b49f52994'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','458df76e-cff6-4824-90bb-6ab390c91627'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','9aac6046-0bc8-4098-8a84-24b1d2d015c1'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','824f17d4-e510-4084-8f39-9b0e264e04ea'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','c9f42ea5-330e-4099-be6b-70e4a184c675'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','678d4ad6-2f2f-4bf2-8b7d-d49d58e0ebc0'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','e91df6de-bdcb-4626-bf0a-44ba69548720'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','d04b8f61-bbff-4159-9c87-312ca61e76db'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','f797c336-605a-4b56-a7b6-09f81c77c907'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','d51e1ab7-4097-4435-91c4-b3795d566d5a'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','150090af-7c0f-479b-94d2-05047607ac37'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','528b6bc7-625a-44ba-9c4b-cd54db750397'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','b98b78c9-9434-495d-8f30-81ec8349dbfc'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','99f3ccc2-07d3-4685-977d-571053cbd506'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','9bd64f72-38db-46e9-86ae-7bd58ebade88');
/*!40000 ALTER TABLE `COMPOSITE_ROLE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CREDENTIAL`
--

DROP TABLE IF EXISTS `CREDENTIAL`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CREDENTIAL` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `DEVICE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `HASH_ITERATIONS` int(11) DEFAULT NULL,
  `SALT` blob,
  `TYPE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `VALUE` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
  `USER_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CREATED_DATE` bigint(20) DEFAULT NULL,
  `COUNTER` int(11) DEFAULT '0',
  `DIGITS` int(11) DEFAULT '6',
  `PERIOD` int(11) DEFAULT '30',
  `ALGORITHM` varchar(36) COLLATE utf8_unicode_ci,
  PRIMARY KEY (`ID`),
  KEY `IDX_USER_CREDENTIAL` (`USER_ID`),
  CONSTRAINT `FK_PFYR0GLASQYL0DEI3KL69R6V0` FOREIGN KEY (`USER_ID`) REFERENCES `USER_ENTITY` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CREDENTIAL`
--

LOCK TABLES `CREDENTIAL` WRITE;
/*!40000 ALTER TABLE `CREDENTIAL` DISABLE KEYS */;
INSERT INTO `CREDENTIAL` VALUES ('72f73a46-6d67-4521-be1a-e012b130e725',NULL,20000,'ÞŽ/ðþ:ÿûú\ï0R\Ú','password','C5M6Jl1hZRFzVSbvclnkILgsbJ5UBqN2IeX1e/064Bf9NfwpC4s3wvkRFZ/xoHNkBn/LCegKOi/90hoyMrcvpg==','a8a3601d-83bf-4f71-ac25-48c1a4cd06c0',NULL,0,0,0,'pbkdf2');
/*!40000 ALTER TABLE `CREDENTIAL` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `CREDENTIAL_ATTRIBUTE`
--

DROP TABLE IF EXISTS `CREDENTIAL_ATTRIBUTE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `CREDENTIAL_ATTRIBUTE` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CREDENTIAL_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
  KEY `FK_CRED_ATTR` (`CREDENTIAL_ID`),
  CONSTRAINT `FK_CRED_ATTR` FOREIGN KEY (`CREDENTIAL_ID`) REFERENCES `CREDENTIAL` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `CREDENTIAL_ATTRIBUTE`
--

LOCK TABLES `CREDENTIAL_ATTRIBUTE` WRITE;
/*!40000 ALTER TABLE `CREDENTIAL_ATTRIBUTE` DISABLE KEYS */;
/*!40000 ALTER TABLE `CREDENTIAL_ATTRIBUTE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DATABASECHANGELOG`
--

DROP TABLE IF EXISTS `DATABASECHANGELOG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATABASECHANGELOG` (
  `ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `AUTHOR` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `FILENAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `DATEEXECUTED` datetime NOT NULL,
  `ORDEREXECUTED` int(11) NOT NULL,
  `EXECTYPE` varchar(10) COLLATE utf8_unicode_ci NOT NULL,
  `MD5SUM` varchar(35) COLLATE utf8_unicode_ci DEFAULT NULL,
  `DESCRIPTION` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `COMMENTS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `TAG` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `LIQUIBASE` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CONTEXTS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `LABELS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATABASECHANGELOG`
--

LOCK TABLES `DATABASECHANGELOG` WRITE;
/*!40000 ALTER TABLE `DATABASECHANGELOG` DISABLE KEYS */;
INSERT INTO `DATABASECHANGELOG` VALUES ('1.0.0.Final','sthorger@redhat.com','META-INF/jpa-changelog-1.0.0.Final.xml','2017-04-25 15:52:02',1,'EXECUTED','7:00a57f7a6fb456639b34e62972e0ec02','createTable (x29), addPrimaryKey (x21), addUniqueConstraint (x9), addForeignKeyConstraint (x32)','',NULL,'3.4.1',NULL,NULL),('1.0.0.Final','sthorger@redhat.com','META-INF/db2-jpa-changelog-1.0.0.Final.xml','2017-04-25 15:52:02',2,'MARK_RAN','7:f061c3934594ee60a9b2343f5100ae4e','createTable (x29), addPrimaryKey (x21), addUniqueConstraint (x6), addForeignKeyConstraint (x30)','',NULL,'3.4.1',NULL,NULL),('1.1.0.Beta1','sthorger@redhat.com','META-INF/jpa-changelog-1.1.0.Beta1.xml','2017-04-25 15:52:02',3,'EXECUTED','7:0310eb8ba07cec616460794d42ade0fa','delete (x3), createTable (x3), addColumn (x5), addPrimaryKey (x3), addForeignKeyConstraint (x3), customChange','',NULL,'3.4.1',NULL,NULL),('1.1.0.Final','sthorger@redhat.com','META-INF/jpa-changelog-1.1.0.Final.xml','2017-04-25 15:52:02',4,'EXECUTED','7:5d25857e708c3233ef4439df1f93f012','renameColumn','',NULL,'3.4.1',NULL,NULL),('1.2.0.Beta1','psilva@redhat.com','META-INF/jpa-changelog-1.2.0.Beta1.xml','2017-04-25 15:52:03',5,'EXECUTED','7:c7a54a1041d58eb3817a4a883b4d4e84','delete (x4), createTable (x8), addColumn (x2), addPrimaryKey (x6), addForeignKeyConstraint (x9), addUniqueConstraint (x2), addColumn, dropForeignKeyConstraint (x2), dropUniqueConstraint, renameColumn (x3), addUniqueConstraint, addForeignKeyConstra...','',NULL,'3.4.1',NULL,NULL),('1.2.0.Beta1','psilva@redhat.com','META-INF/db2-jpa-changelog-1.2.0.Beta1.xml','2017-04-25 15:52:03',6,'MARK_RAN','7:2e01012df20974c1c2a605ef8afe25b7','delete (x4), createTable (x8), addColumn (x2), addPrimaryKey (x6), addForeignKeyConstraint (x9), addUniqueConstraint (x2), addColumn, dropForeignKeyConstraint (x2), dropUniqueConstraint, renameColumn (x3), customChange, dropForeignKeyConstraint, d...','',NULL,'3.4.1',NULL,NULL),('1.2.0.RC1','bburke@redhat.com','META-INF/jpa-changelog-1.2.0.CR1.xml','2017-04-25 15:52:04',7,'EXECUTED','7:0f08df48468428e0f30ee59a8ec01a41','delete (x5), createTable (x3), addColumn, createTable (x4), addPrimaryKey (x7), addForeignKeyConstraint (x6), renameColumn, addColumn (x2), update, dropColumn, dropForeignKeyConstraint, renameColumn, addForeignKeyConstraint, dropForeignKeyConstrai...','',NULL,'3.4.1',NULL,NULL),('1.2.0.RC1','bburke@redhat.com','META-INF/db2-jpa-changelog-1.2.0.CR1.xml','2017-04-25 15:52:04',8,'MARK_RAN','7:a77ea2ad226b345e7d689d366f185c8c','delete (x5), createTable (x3), addColumn, createTable (x4), addPrimaryKey (x7), addForeignKeyConstraint (x6), renameColumn, addUniqueConstraint, addColumn (x2), update, dropColumn, dropForeignKeyConstraint, renameColumn, addForeignKeyConstraint, r...','',NULL,'3.4.1',NULL,NULL),('1.2.0.Final','keycloak','META-INF/jpa-changelog-1.2.0.Final.xml','2017-04-25 15:52:04',9,'EXECUTED','7:a3377a2059aefbf3b90ebb4c4cc8e2ab','update (x3)','',NULL,'3.4.1',NULL,NULL),('1.3.0','bburke@redhat.com','META-INF/jpa-changelog-1.3.0.xml','2017-04-25 15:52:05',10,'EXECUTED','7:04c1dbedc2aa3e9756d1a1668e003451','delete (x6), createTable (x7), addColumn, createTable, addColumn (x2), update, dropDefaultValue, dropColumn, addColumn, update (x4), addPrimaryKey (x4), dropPrimaryKey, dropColumn, addPrimaryKey (x4), addForeignKeyConstraint (x8), dropDefaultValue...','',NULL,'3.4.1',NULL,NULL),('1.4.0','bburke@redhat.com','META-INF/jpa-changelog-1.4.0.xml','2017-04-25 15:52:06',11,'EXECUTED','7:36ef39ed560ad07062d956db861042ba','delete (x7), addColumn (x5), dropColumn, renameTable (x2), update (x10), createTable (x3), customChange, dropPrimaryKey, addPrimaryKey (x4), addForeignKeyConstraint (x2), dropColumn, addColumn','',NULL,'3.4.1',NULL,NULL),('1.4.0','bburke@redhat.com','META-INF/db2-jpa-changelog-1.4.0.xml','2017-04-25 15:52:06',12,'MARK_RAN','7:d909180b2530479a716d3f9c9eaea3d7','delete (x7), addColumn (x5), dropColumn, renameTable, dropForeignKeyConstraint, renameTable, addForeignKeyConstraint, update (x10), createTable (x3), customChange, dropPrimaryKey, addPrimaryKey (x4), addForeignKeyConstraint (x2), dropColumn, addCo...','',NULL,'3.4.1',NULL,NULL),('1.5.0','bburke@redhat.com','META-INF/jpa-changelog-1.5.0.xml','2017-04-25 15:52:06',13,'EXECUTED','7:cf12b04b79bea5152f165eb41f3955f6','delete (x7), dropDefaultValue, dropColumn, addColumn (x3)','',NULL,'3.4.1',NULL,NULL),('1.6.1_from15','mposolda@redhat.com','META-INF/jpa-changelog-1.6.1.xml','2017-04-25 15:52:06',14,'EXECUTED','7:7e32c8f05c755e8675764e7d5f514509','addColumn (x3), createTable (x2), addPrimaryKey (x2)','',NULL,'3.4.1',NULL,NULL),('1.6.1_from16-pre','mposolda@redhat.com','META-INF/jpa-changelog-1.6.1.xml','2017-04-25 15:52:06',15,'MARK_RAN','7:980ba23cc0ec39cab731ce903dd01291','delete (x2)','',NULL,'3.4.1',NULL,NULL),('1.6.1_from16','mposolda@redhat.com','META-INF/jpa-changelog-1.6.1.xml','2017-04-25 15:52:06',16,'MARK_RAN','7:2fa220758991285312eb84f3b4ff5336','dropPrimaryKey (x2), addColumn, update, dropColumn, addColumn, update, dropColumn, addPrimaryKey (x2)','',NULL,'3.4.1',NULL,NULL),('1.6.1','mposolda@redhat.com','META-INF/jpa-changelog-1.6.1.xml','2017-04-25 15:52:06',17,'EXECUTED','7:d41d8cd98f00b204e9800998ecf8427e','Empty','',NULL,'3.4.1',NULL,NULL),('1.7.0','bburke@redhat.com','META-INF/jpa-changelog-1.7.0.xml','2017-04-25 15:52:07',18,'EXECUTED','7:91ace540896df890cc00a0490ee52bbc','createTable (x5), addColumn (x2), dropDefaultValue, dropColumn, addPrimaryKey, addForeignKeyConstraint, addPrimaryKey, addForeignKeyConstraint, addPrimaryKey, addForeignKeyConstraint, addPrimaryKey, addForeignKeyConstraint (x2), addUniqueConstrain...','',NULL,'3.4.1',NULL,NULL),('1.8.0','mposolda@redhat.com','META-INF/jpa-changelog-1.8.0.xml','2017-04-25 15:52:07',19,'EXECUTED','7:c31d1646dfa2618a9335c00e07f89f24','addColumn, createTable (x3), dropNotNullConstraint, addColumn (x2), createTable, addPrimaryKey, addUniqueConstraint, addForeignKeyConstraint (x5), addPrimaryKey, addForeignKeyConstraint (x2), addPrimaryKey, addForeignKeyConstraint, update','',NULL,'3.4.1',NULL,NULL),('1.8.0-2','keycloak','META-INF/jpa-changelog-1.8.0.xml','2017-04-25 15:52:07',20,'EXECUTED','7:df8bc21027a4f7cbbb01f6344e89ce07','dropDefaultValue, update','',NULL,'3.4.1',NULL,NULL),('1.8.0','mposolda@redhat.com','META-INF/db2-jpa-changelog-1.8.0.xml','2017-04-25 15:52:07',21,'MARK_RAN','7:f987971fe6b37d963bc95fee2b27f8df','addColumn, createTable (x3), dropNotNullConstraint, addColumn (x2), createTable, addPrimaryKey, addUniqueConstraint, addForeignKeyConstraint (x5), addPrimaryKey, addForeignKeyConstraint (x2), addPrimaryKey, addForeignKeyConstraint, update','',NULL,'3.4.1',NULL,NULL),('1.8.0-2','keycloak','META-INF/db2-jpa-changelog-1.8.0.xml','2017-04-25 15:52:07',22,'MARK_RAN','7:df8bc21027a4f7cbbb01f6344e89ce07','dropDefaultValue, update','',NULL,'3.4.1',NULL,NULL),('1.9.0','mposolda@redhat.com','META-INF/jpa-changelog-1.9.0.xml','2017-04-25 15:52:08',23,'EXECUTED','7:ed2dc7f799d19ac452cbcda56c929e47','update (x9), customChange, dropForeignKeyConstraint (x2), dropUniqueConstraint, dropTable, dropForeignKeyConstraint (x2), dropTable, dropForeignKeyConstraint (x2), dropUniqueConstraint, dropTable, createIndex','',NULL,'3.4.1',NULL,NULL),('1.9.1','keycloak','META-INF/jpa-changelog-1.9.1.xml','2017-04-25 15:52:08',24,'EXECUTED','7:80b5db88a5dda36ece5f235be8757615','modifyDataType (x3)','',NULL,'3.4.1',NULL,NULL),('1.9.1','keycloak','META-INF/db2-jpa-changelog-1.9.1.xml','2017-04-25 15:52:08',25,'MARK_RAN','7:1437310ed1305a9b93f8848f301726ce','modifyDataType (x2)','',NULL,'3.4.1',NULL,NULL),('1.9.2','keycloak','META-INF/jpa-changelog-1.9.2.xml','2017-04-25 15:52:08',26,'EXECUTED','7:b82ffb34850fa0836be16deefc6a87c4','createIndex (x11)','',NULL,'3.4.1',NULL,NULL),('authz-2.0.0','psilva@redhat.com','META-INF/jpa-changelog-authz-2.0.0.xml','2017-04-25 15:52:09',27,'EXECUTED','7:9cc98082921330d8d9266decdd4bd658','createTable, addPrimaryKey, addUniqueConstraint, createTable, addPrimaryKey, addForeignKeyConstraint, addUniqueConstraint, createTable, addPrimaryKey, addForeignKeyConstraint, addUniqueConstraint, createTable, addPrimaryKey, addForeignKeyConstrain...','',NULL,'3.4.1',NULL,NULL),('authz-2.5.1','psilva@redhat.com','META-INF/jpa-changelog-authz-2.5.1.xml','2017-04-25 15:52:09',28,'EXECUTED','7:03d64aeed9cb52b969bd30a7ac0db57e','update','',NULL,'3.4.1',NULL,NULL),('2.1.0','bburke@redhat.com','META-INF/jpa-changelog-2.1.0.xml','2017-04-25 15:52:09',29,'EXECUTED','7:e01599a82bf8d6dc22a9da506e22e868','createTable (x11), addPrimaryKey (x11), addForeignKeyConstraint (x2)','',NULL,'3.4.1',NULL,NULL),('2.2.0','bburke@redhat.com','META-INF/jpa-changelog-2.2.0.xml','2017-04-25 15:52:09',30,'EXECUTED','7:53188c3eb1107546e6f765835705b6c1','addColumn, createTable (x2), modifyDataType, addForeignKeyConstraint (x2)','',NULL,'3.4.1',NULL,NULL),('2.3.0','bburke@redhat.com','META-INF/jpa-changelog-2.3.0.xml','2017-04-25 15:52:10',31,'EXECUTED','7:d6e6f3bc57a0c5586737d1351725d4d4','createTable, addPrimaryKey, dropDefaultValue, dropColumn, addColumn (x2), customChange, dropColumn (x4), addColumn','',NULL,'3.4.1',NULL,NULL),('2.4.0','bburke@redhat.com','META-INF/jpa-changelog-2.4.0.xml','2017-04-25 15:52:10',32,'EXECUTED','7:454d604fbd755d9df3fd9c6329043aa5','customChange','',NULL,'3.4.1',NULL,NULL),('2.5.0','bburke@redhat.com','META-INF/jpa-changelog-2.5.0.xml','2017-04-25 15:52:10',33,'EXECUTED','7:57e98a3077e29caf562f7dbf80c72600','customChange, modifyDataType','',NULL,'3.4.1',NULL,NULL),('2.5.0-unicode-oracle','hmlnarik@redhat.com','META-INF/jpa-changelog-2.5.0.xml','2017-04-25 15:52:10',34,'MARK_RAN','7:e4c7e8f2256210aee71ddc42f538b57a','modifyDataType (x13), addColumn, sql, dropColumn, renameColumn, modifyDataType (x2)','',NULL,'3.4.1',NULL,NULL),('2.5.0-unicode-other-dbs','hmlnarik@redhat.com','META-INF/jpa-changelog-2.5.0.xml','2017-04-25 15:52:10',35,'EXECUTED','7:09a43c97e49bc626460480aa1379b522','modifyDataType (x5), dropUniqueConstraint, modifyDataType (x3), addUniqueConstraint, dropPrimaryKey, modifyDataType, addNotNullConstraint, addPrimaryKey, modifyDataType (x5), dropUniqueConstraint, modifyDataType, addUniqueConstraint, modifyDataType','',NULL,'3.4.1',NULL,NULL),('2.5.0-duplicate-email-support','slawomir@dabek.name','META-INF/jpa-changelog-2.5.0.xml','2017-04-25 15:52:10',36,'EXECUTED','7:26bfc7c74fefa9126f2ce702fb775553','addColumn','',NULL,'3.4.1',NULL,NULL),('2.5.0-unique-group-names','hmlnarik@redhat.com','META-INF/jpa-changelog-2.5.0.xml','2017-04-25 15:52:10',37,'EXECUTED','7:a161e2ae671a9020fff61e996a207377','addUniqueConstraint','',NULL,'3.4.1',NULL,NULL),('2.5.1','bburke@redhat.com','META-INF/jpa-changelog-2.5.1.xml','2017-04-25 15:52:10',38,'EXECUTED','7:37fc1781855ac5388c494f1442b3f717','addColumn','',NULL,'3.4.1',NULL,NULL);
/*!40000 ALTER TABLE `DATABASECHANGELOG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `DATABASECHANGELOGLOCK`
--

DROP TABLE IF EXISTS `DATABASECHANGELOGLOCK`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `DATABASECHANGELOGLOCK` (
  `ID` int(11) NOT NULL,
  `LOCKED` bit(1) NOT NULL,
  `LOCKGRANTED` datetime DEFAULT NULL,
  `LOCKEDBY` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `DATABASECHANGELOGLOCK`
--

LOCK TABLES `DATABASECHANGELOGLOCK` WRITE;
/*!40000 ALTER TABLE `DATABASECHANGELOGLOCK` DISABLE KEYS */;
INSERT INTO `DATABASECHANGELOGLOCK` VALUES (1,'\0',NULL,NULL);
/*!40000 ALTER TABLE `DATABASECHANGELOGLOCK` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `EVENT_ENTITY`
--

DROP TABLE IF EXISTS `EVENT_ENTITY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `EVENT_ENTITY` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CLIENT_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `DETAILS_JSON` varchar(2550) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ERROR` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `IP_ADDRESS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `SESSION_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `EVENT_TIME` bigint(20) DEFAULT NULL,
  `TYPE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `USER_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `EVENT_ENTITY`
--

LOCK TABLES `EVENT_ENTITY` WRITE;
/*!40000 ALTER TABLE `EVENT_ENTITY` DISABLE KEYS */;
/*!40000 ALTER TABLE `EVENT_ENTITY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FED_CREDENTIAL_ATTRIBUTE`
--

DROP TABLE IF EXISTS `FED_CREDENTIAL_ATTRIBUTE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FED_CREDENTIAL_ATTRIBUTE` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CREDENTIAL_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(4000) COLLATE utf8_unicode_ci DEFAULT NULL,
  KEY `FK_FED_CRED_ATTR` (`CREDENTIAL_ID`),
  CONSTRAINT `FK_FED_CRED_ATTR` FOREIGN KEY (`CREDENTIAL_ID`) REFERENCES `FED_USER_CREDENTIAL` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FED_CREDENTIAL_ATTRIBUTE`
--

LOCK TABLES `FED_CREDENTIAL_ATTRIBUTE` WRITE;
/*!40000 ALTER TABLE `FED_CREDENTIAL_ATTRIBUTE` DISABLE KEYS */;
/*!40000 ALTER TABLE `FED_CREDENTIAL_ATTRIBUTE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FED_USER_ATTRIBUTE`
--

DROP TABLE IF EXISTS `FED_USER_ATTRIBUTE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FED_USER_ATTRIBUTE` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `USER_ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `STORAGE_PROVIDER_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `VALUE` varchar(2024) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FED_USER_ATTRIBUTE`
--

LOCK TABLES `FED_USER_ATTRIBUTE` WRITE;
/*!40000 ALTER TABLE `FED_USER_ATTRIBUTE` DISABLE KEYS */;
/*!40000 ALTER TABLE `FED_USER_ATTRIBUTE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FED_USER_CONSENT`
--

DROP TABLE IF EXISTS `FED_USER_CONSENT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FED_USER_CONSENT` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `USER_ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `STORAGE_PROVIDER_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CREATED_DATE` bigint(20) DEFAULT NULL,
  `LAST_UPDATED_DATE` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FED_USER_CONSENT`
--

LOCK TABLES `FED_USER_CONSENT` WRITE;
/*!40000 ALTER TABLE `FED_USER_CONSENT` DISABLE KEYS */;
/*!40000 ALTER TABLE `FED_USER_CONSENT` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FED_USER_CONSENT_PROT_MAPPER`
--

DROP TABLE IF EXISTS `FED_USER_CONSENT_PROT_MAPPER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FED_USER_CONSENT_PROT_MAPPER` (
  `USER_CONSENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `PROTOCOL_MAPPER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`USER_CONSENT_ID`,`PROTOCOL_MAPPER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FED_USER_CONSENT_PROT_MAPPER`
--

LOCK TABLES `FED_USER_CONSENT_PROT_MAPPER` WRITE;
/*!40000 ALTER TABLE `FED_USER_CONSENT_PROT_MAPPER` DISABLE KEYS */;
/*!40000 ALTER TABLE `FED_USER_CONSENT_PROT_MAPPER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FED_USER_CONSENT_ROLE`
--

DROP TABLE IF EXISTS `FED_USER_CONSENT_ROLE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FED_USER_CONSENT_ROLE` (
  `USER_CONSENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ROLE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`USER_CONSENT_ID`,`ROLE_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FED_USER_CONSENT_ROLE`
--

LOCK TABLES `FED_USER_CONSENT_ROLE` WRITE;
/*!40000 ALTER TABLE `FED_USER_CONSENT_ROLE` DISABLE KEYS */;
/*!40000 ALTER TABLE `FED_USER_CONSENT_ROLE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FED_USER_CREDENTIAL`
--

DROP TABLE IF EXISTS `FED_USER_CREDENTIAL`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FED_USER_CREDENTIAL` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `DEVICE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `HASH_ITERATIONS` int(11) DEFAULT NULL,
  `SALT` blob,
  `TYPE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `VALUE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CREATED_DATE` bigint(20) DEFAULT NULL,
  `COUNTER` int(11) DEFAULT '0',
  `DIGITS` int(11) DEFAULT '6',
  `PERIOD` int(11) DEFAULT '30',
  `ALGORITHM` varchar(36) COLLATE utf8_unicode_ci DEFAULT 'HmacSHA1',
  `USER_ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `STORAGE_PROVIDER_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FED_USER_CREDENTIAL`
--

LOCK TABLES `FED_USER_CREDENTIAL` WRITE;
/*!40000 ALTER TABLE `FED_USER_CREDENTIAL` DISABLE KEYS */;
/*!40000 ALTER TABLE `FED_USER_CREDENTIAL` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FED_USER_GROUP_MEMBERSHIP`
--

DROP TABLE IF EXISTS `FED_USER_GROUP_MEMBERSHIP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FED_USER_GROUP_MEMBERSHIP` (
  `GROUP_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `USER_ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `STORAGE_PROVIDER_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`GROUP_ID`,`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FED_USER_GROUP_MEMBERSHIP`
--

LOCK TABLES `FED_USER_GROUP_MEMBERSHIP` WRITE;
/*!40000 ALTER TABLE `FED_USER_GROUP_MEMBERSHIP` DISABLE KEYS */;
/*!40000 ALTER TABLE `FED_USER_GROUP_MEMBERSHIP` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FED_USER_REQUIRED_ACTION`
--

DROP TABLE IF EXISTS `FED_USER_REQUIRED_ACTION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FED_USER_REQUIRED_ACTION` (
  `REQUIRED_ACTION` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT ' ',
  `USER_ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `STORAGE_PROVIDER_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`REQUIRED_ACTION`,`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FED_USER_REQUIRED_ACTION`
--

LOCK TABLES `FED_USER_REQUIRED_ACTION` WRITE;
/*!40000 ALTER TABLE `FED_USER_REQUIRED_ACTION` DISABLE KEYS */;
/*!40000 ALTER TABLE `FED_USER_REQUIRED_ACTION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FED_USER_ROLE_MAPPING`
--

DROP TABLE IF EXISTS `FED_USER_ROLE_MAPPING`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FED_USER_ROLE_MAPPING` (
  `ROLE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `USER_ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `STORAGE_PROVIDER_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ROLE_ID`,`USER_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FED_USER_ROLE_MAPPING`
--

LOCK TABLES `FED_USER_ROLE_MAPPING` WRITE;
/*!40000 ALTER TABLE `FED_USER_ROLE_MAPPING` DISABLE KEYS */;
/*!40000 ALTER TABLE `FED_USER_ROLE_MAPPING` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FEDERATED_IDENTITY`
--

DROP TABLE IF EXISTS `FEDERATED_IDENTITY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FEDERATED_IDENTITY` (
  `IDENTITY_PROVIDER` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `FEDERATED_USER_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `FEDERATED_USERNAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `TOKEN` text COLLATE utf8_unicode_ci,
  `USER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`IDENTITY_PROVIDER`,`USER_ID`),
  KEY `IDX_FEDIDENTITY_USER` (`USER_ID`),
  KEY `IDX_FEDIDENTITY_FEDUSER` (`FEDERATED_USER_ID`),
  CONSTRAINT `FK404288B92EF007A6` FOREIGN KEY (`USER_ID`) REFERENCES `USER_ENTITY` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FEDERATED_IDENTITY`
--

LOCK TABLES `FEDERATED_IDENTITY` WRITE;
/*!40000 ALTER TABLE `FEDERATED_IDENTITY` DISABLE KEYS */;
/*!40000 ALTER TABLE `FEDERATED_IDENTITY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `FEDERATED_USER`
--

DROP TABLE IF EXISTS `FEDERATED_USER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `FEDERATED_USER` (
  `ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `STORAGE_PROVIDER_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `FEDERATED_USER`
--

LOCK TABLES `FEDERATED_USER` WRITE;
/*!40000 ALTER TABLE `FEDERATED_USER` DISABLE KEYS */;
/*!40000 ALTER TABLE `FEDERATED_USER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `GROUP_ATTRIBUTE`
--

DROP TABLE IF EXISTS `GROUP_ATTRIBUTE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GROUP_ATTRIBUTE` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'sybase-needs-something-here',
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `GROUP_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_GROUP_ATTRIBUTE_GROUP` (`GROUP_ID`),
  CONSTRAINT `FK_GROUP_ATTRIBUTE_GROUP` FOREIGN KEY (`GROUP_ID`) REFERENCES `KEYCLOAK_GROUP` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `GROUP_ATTRIBUTE`
--

LOCK TABLES `GROUP_ATTRIBUTE` WRITE;
/*!40000 ALTER TABLE `GROUP_ATTRIBUTE` DISABLE KEYS */;
/*!40000 ALTER TABLE `GROUP_ATTRIBUTE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `GROUP_ROLE_MAPPING`
--

DROP TABLE IF EXISTS `GROUP_ROLE_MAPPING`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `GROUP_ROLE_MAPPING` (
  `ROLE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `GROUP_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ROLE_ID`,`GROUP_ID`),
  KEY `FK_GROUP_ROLE_GROUP` (`GROUP_ID`),
  CONSTRAINT `FK_GROUP_ROLE_GROUP` FOREIGN KEY (`GROUP_ID`) REFERENCES `KEYCLOAK_GROUP` (`ID`),
  CONSTRAINT `FK_GROUP_ROLE_ROLE` FOREIGN KEY (`ROLE_ID`) REFERENCES `KEYCLOAK_ROLE` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `GROUP_ROLE_MAPPING`
--

LOCK TABLES `GROUP_ROLE_MAPPING` WRITE;
/*!40000 ALTER TABLE `GROUP_ROLE_MAPPING` DISABLE KEYS */;
/*!40000 ALTER TABLE `GROUP_ROLE_MAPPING` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `IDENTITY_PROVIDER`
--

DROP TABLE IF EXISTS `IDENTITY_PROVIDER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IDENTITY_PROVIDER` (
  `INTERNAL_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `PROVIDER_ALIAS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PROVIDER_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `STORE_TOKEN` bit(1) NOT NULL DEFAULT b'0',
  `AUTHENTICATE_BY_DEFAULT` bit(1) NOT NULL DEFAULT b'0',
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ADD_TOKEN_ROLE` bit(1) NOT NULL DEFAULT b'1',
  `TRUST_EMAIL` bit(1) NOT NULL DEFAULT b'0',
  `FIRST_BROKER_LOGIN_FLOW_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `POST_BROKER_LOGIN_FLOW_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `PROVIDER_DISPLAY_NAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`INTERNAL_ID`),
  UNIQUE KEY `UK_2DAELWNIBJI49AVXSRTUF6XJ33` (`PROVIDER_ALIAS`,`REALM_ID`),
  KEY `FK2B4EBC52AE5C3B34` (`REALM_ID`),
  CONSTRAINT `FK2B4EBC52AE5C3B34` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `IDENTITY_PROVIDER`
--

LOCK TABLES `IDENTITY_PROVIDER` WRITE;
/*!40000 ALTER TABLE `IDENTITY_PROVIDER` DISABLE KEYS */;
/*!40000 ALTER TABLE `IDENTITY_PROVIDER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `IDENTITY_PROVIDER_CONFIG`
--

DROP TABLE IF EXISTS `IDENTITY_PROVIDER_CONFIG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IDENTITY_PROVIDER_CONFIG` (
  `IDENTITY_PROVIDER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` longtext COLLATE utf8_unicode_ci,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`IDENTITY_PROVIDER_ID`,`NAME`),
  CONSTRAINT `FKDC4897CF864C4E43` FOREIGN KEY (`IDENTITY_PROVIDER_ID`) REFERENCES `IDENTITY_PROVIDER` (`INTERNAL_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `IDENTITY_PROVIDER_CONFIG`
--

LOCK TABLES `IDENTITY_PROVIDER_CONFIG` WRITE;
/*!40000 ALTER TABLE `IDENTITY_PROVIDER_CONFIG` DISABLE KEYS */;
/*!40000 ALTER TABLE `IDENTITY_PROVIDER_CONFIG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `IDENTITY_PROVIDER_MAPPER`
--

DROP TABLE IF EXISTS `IDENTITY_PROVIDER_MAPPER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IDENTITY_PROVIDER_MAPPER` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `IDP_ALIAS` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `IDP_MAPPER_NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_IDPM_REALM` (`REALM_ID`),
  CONSTRAINT `FK_IDPM_REALM` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `IDENTITY_PROVIDER_MAPPER`
--

LOCK TABLES `IDENTITY_PROVIDER_MAPPER` WRITE;
/*!40000 ALTER TABLE `IDENTITY_PROVIDER_MAPPER` DISABLE KEYS */;
/*!40000 ALTER TABLE `IDENTITY_PROVIDER_MAPPER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `IDP_MAPPER_CONFIG`
--

DROP TABLE IF EXISTS `IDP_MAPPER_CONFIG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `IDP_MAPPER_CONFIG` (
  `IDP_MAPPER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` longtext COLLATE utf8_unicode_ci,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`IDP_MAPPER_ID`,`NAME`),
  CONSTRAINT `FK_IDPMCONFIG` FOREIGN KEY (`IDP_MAPPER_ID`) REFERENCES `IDENTITY_PROVIDER_MAPPER` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `IDP_MAPPER_CONFIG`
--

LOCK TABLES `IDP_MAPPER_CONFIG` WRITE;
/*!40000 ALTER TABLE `IDP_MAPPER_CONFIG` DISABLE KEYS */;
/*!40000 ALTER TABLE `IDP_MAPPER_CONFIG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `KEYCLOAK_GROUP`
--

DROP TABLE IF EXISTS `KEYCLOAK_GROUP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `KEYCLOAK_GROUP` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `PARENT_GROUP` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `SIBLING_NAMES` (`REALM_ID`,`PARENT_GROUP`,`NAME`),
  CONSTRAINT `FK_GROUP_REALM` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `KEYCLOAK_GROUP`
--

LOCK TABLES `KEYCLOAK_GROUP` WRITE;
/*!40000 ALTER TABLE `KEYCLOAK_GROUP` DISABLE KEYS */;
/*!40000 ALTER TABLE `KEYCLOAK_GROUP` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `KEYCLOAK_ROLE`
--

DROP TABLE IF EXISTS `KEYCLOAK_ROLE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `KEYCLOAK_ROLE` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CLIENT_REALM_CONSTRAINT` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CLIENT_ROLE` bit(1) DEFAULT NULL,
  `DESCRIPTION` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `NAME` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `REALM_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CLIENT` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `SCOPE_PARAM_REQUIRED` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_J3RWUVD56ONTGSUHOGM184WW2-2` (`NAME`,`CLIENT_REALM_CONSTRAINT`),
  KEY `FK_6VYQFE4CN4WLQ8R6KT5VDSJ5C` (`REALM`),
  KEY `FK_KJHO5LE2C0RAL09FL8CM9WFW9` (`CLIENT`),
  CONSTRAINT `FK_6VYQFE4CN4WLQ8R6KT5VDSJ5C` FOREIGN KEY (`REALM`) REFERENCES `REALM` (`ID`),
  CONSTRAINT `FK_KJHO5LE2C0RAL09FL8CM9WFW9` FOREIGN KEY (`CLIENT`) REFERENCES `CLIENT` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `KEYCLOAK_ROLE`
--

LOCK TABLES `KEYCLOAK_ROLE` WRITE;
/*!40000 ALTER TABLE `KEYCLOAK_ROLE` DISABLE KEYS */;
INSERT INTO `KEYCLOAK_ROLE` VALUES ('03587e78-8443-44f2-8e58-24c4965780ac','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_manage-clients}','manage-clients','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('04d19404-ccdd-46b9-85e2-abf3242f41ac','master','\0','${role_offline-access}','offline_access','master',NULL,'master',''),('096032b5-1c7a-4b2f-a290-49b0709e82a1','1baebc2e-3c0b-4a04-a067-39dac04320aa','',NULL,'uma_protection','shanoir-ng','1baebc2e-3c0b-4a04-a067-39dac04320aa',NULL,'\0'),('0b9edc64-bb37-447f-960c-0fc2c3c18563','63cee678-229d-4715-99b7-b319c4cec32d','','${role_manage-account}','manage-account','master','63cee678-229d-4715-99b7-b319c4cec32d',NULL,'\0'),('0dd34304-14e1-47c5-9750-52fb0deef76d','84284718-4123-4bee-a906-3ecd66bea3d1','','${role_view-profile}','view-profile','shanoir-ng','84284718-4123-4bee-a906-3ecd66bea3d1',NULL,'\0'),('150090af-7c0f-479b-94d2-05047607ac37','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_manage-clients}','manage-clients','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('15ca2839-51d6-4d56-ba98-b819294fbf65','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_view-events}','view-events','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('19fd14f5-914b-4851-95cc-c86019057f17','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_manage-users}','manage-users','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('1a62560c-30bc-4d26-90c6-fba5a99854e2','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_manage-authorization}','manage-authorization','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('1ab604b6-57c9-481f-bf50-57256243bc7b','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_manage-identity-providers}','manage-identity-providers','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('1f6fad9e-376b-45d2-9c38-db3d1f7c9ac9','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_manage-authorization}','manage-authorization','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('21ce904e-16de-473a-88a3-78ac4920f0c9','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_manage-users}','manage-users','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('24ea9939-e0fa-456e-8d47-e0afa4a858c1','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_view-users}','view-users','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('2649233c-3f3e-4ded-a058-ef8e61ec2daa','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_view-events}','view-events','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('28f32fbc-a01d-4f7a-b5ca-e78f059d33e1','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_view-authorization}','view-authorization','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('2e265c4c-da6b-457f-87ce-fa97ecef929b','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_view-clients}','view-clients','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('30589ea4-b0bc-4043-99f3-d5e19ec0e7aa','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_create-client}','create-client','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('4272b7d4-993b-416f-99cb-5f5afecc4be5','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_manage-identity-providers}','manage-identity-providers','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('458df76e-cff6-4824-90bb-6ab390c91627','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_create-client}','create-client','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('4c617019-2440-453d-82c4-421215ff8e7c','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_view-authorization}','view-authorization','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('4d8ad343-bce7-4b7f-9cdc-e5ee914e3d28','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_view-identity-providers}','view-identity-providers','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('528b6bc7-625a-44ba-9c4b-cd54db750397','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_manage-events}','manage-events','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('634480a6-de33-466b-aa10-13624ca726b2','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_create-client}','create-client','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('678d4ad6-2f2f-4bf2-8b7d-d49d58e0ebc0','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_view-events}','view-events','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','master','\0','${role_admin}','admin','master',NULL,'master','\0'),('6a764793-5b5d-4d7c-9ba2-56215cfad5ff','84284718-4123-4bee-a906-3ecd66bea3d1','','${role_manage-account}','manage-account','shanoir-ng','84284718-4123-4bee-a906-3ecd66bea3d1',NULL,'\0'),('6b80ad80-48e1-4a3c-b478-7f53c8329a55','63cee678-229d-4715-99b7-b319c4cec32d','','${role_view-profile}','view-profile','master','63cee678-229d-4715-99b7-b319c4cec32d',NULL,'\0'),('70c9ed3c-9cd8-440d-b74d-4eee983ca18c','master','\0','${role_create-realm}','create-realm','master',NULL,'master','\0'),('7218abbc-4683-410b-822c-3af7f02d10c1','76d1b923-ed53-405b-b798-d1c8ea62d1fe','','${role_read-token}','read-token','shanoir-ng','76d1b923-ed53-405b-b798-d1c8ea62d1fe',NULL,'\0'),('824f17d4-e510-4084-8f39-9b0e264e04ea','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_view-users}','view-users','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('8cdfa9b8-2e8d-473f-a2a0-d4581b5c9ddc','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_view-users}','view-users','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('98da76c9-9cb5-4163-a4c3-369e38429680','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_manage-clients}','manage-clients','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('99f3ccc2-07d3-4685-977d-571053cbd506','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_manage-authorization}','manage-authorization','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('9aac6046-0bc8-4098-8a84-24b1d2d015c1','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_view-realm}','view-realm','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('9bd64f72-38db-46e9-86ae-7bd58ebade88','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_impersonation}','impersonation','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('9d907672-9245-4dd6-98ec-b30fa1c9abfd','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_view-realm}','view-realm','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('a616474e-6553-4f3d-87aa-b4e49e512318','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_view-identity-providers}','view-identity-providers','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('aad00e99-e8a5-48d0-a350-ed9b49f52994','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_impersonation}','impersonation','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('b02f324c-6456-40d7-a40c-5c0db0704021','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_manage-realm}','manage-realm','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('b6831ef9-03e5-4c95-a6e7-f3df303cb6e8','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_impersonation}','impersonation','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('b98b78c9-9434-495d-8f30-81ec8349dbfc','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_manage-identity-providers}','manage-identity-providers','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('ba269dab-fbb4-4fdf-ac82-4a0629e86525','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_manage-realm}','manage-realm','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('c9f42ea5-330e-4099-be6b-70e4a184c675','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_view-clients}','view-clients','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('d04b8f61-bbff-4159-9c87-312ca61e76db','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_view-authorization}','view-authorization','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('d51e1ab7-4097-4435-91c4-b3795d566d5a','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_manage-users}','manage-users','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('d83f8de8-036b-4b41-940a-85202fe6a860','shanoir-ng','\0','${role_uma_authorization}','uma_authorization','shanoir-ng',NULL,'shanoir-ng','\0'),('d884d9b1-5599-47c5-94e7-535d36485ecf','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_manage-events}','manage-events','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('da56af07-3865-4e5b-b661-07ecc3db7413','master','\0','${role_uma_authorization}','uma_authorization','master',NULL,'master','\0'),('dd741a05-d506-4842-aa4c-7311c25b844d','shanoir-ng','\0','${role_offline-access}','offline_access','shanoir-ng',NULL,'shanoir-ng',''),('df0e2d45-e4b5-4524-a472-5e71fe083478','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_view-clients}','view-clients','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('e1979e00-5057-4dc4-beaf-5bf19532b8cc','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_realm-admin}','realm-admin','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('e481353f-0ce4-40db-bce4-cb6b3ad0ada2','93635e6b-5f0f-4a37-b304-4337459122aa','','${role_manage-events}','manage-events','master','93635e6b-5f0f-4a37-b304-4337459122aa',NULL,'\0'),('e61dc489-d8dc-4570-8c30-e3acd215de0c','354f7145-851b-4a84-8d27-cb6622d4eb8a','','${role_view-realm}','view-realm','master','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL,'\0'),('e91df6de-bdcb-4626-bf0a-44ba69548720','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_view-identity-providers}','view-identity-providers','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0'),('f12a48ab-f176-4b88-bf6a-3d291b91f99f','47b3eb38-8229-4bbb-a0b0-c37cf2ef6227','','${role_read-token}','read-token','master','47b3eb38-8229-4bbb-a0b0-c37cf2ef6227',NULL,'\0'),('f797c336-605a-4b56-a7b6-09f81c77c907','f91cb2fc-74db-4aa6-8524-4c42113e7b9f','','${role_manage-realm}','manage-realm','shanoir-ng','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL,'\0');
/*!40000 ALTER TABLE `KEYCLOAK_ROLE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `MIGRATION_MODEL`
--

DROP TABLE IF EXISTS `MIGRATION_MODEL`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `MIGRATION_MODEL` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VERSION` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `MIGRATION_MODEL`
--

LOCK TABLES `MIGRATION_MODEL` WRITE;
/*!40000 ALTER TABLE `MIGRATION_MODEL` DISABLE KEYS */;
INSERT INTO `MIGRATION_MODEL` VALUES ('SINGLETON','2.5.0');
/*!40000 ALTER TABLE `MIGRATION_MODEL` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `OFFLINE_CLIENT_SESSION`
--

DROP TABLE IF EXISTS `OFFLINE_CLIENT_SESSION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OFFLINE_CLIENT_SESSION` (
  `CLIENT_SESSION_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `USER_SESSION_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `OFFLINE_FLAG` varchar(4) COLLATE utf8_unicode_ci NOT NULL,
  `TIMESTAMP` int(11) DEFAULT NULL,
  `DATA` longtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`CLIENT_SESSION_ID`,`OFFLINE_FLAG`),
  KEY `IDX_US_SESS_ID_ON_CL_SESS` (`USER_SESSION_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `OFFLINE_CLIENT_SESSION`
--

LOCK TABLES `OFFLINE_CLIENT_SESSION` WRITE;
/*!40000 ALTER TABLE `OFFLINE_CLIENT_SESSION` DISABLE KEYS */;
/*!40000 ALTER TABLE `OFFLINE_CLIENT_SESSION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `OFFLINE_USER_SESSION`
--

DROP TABLE IF EXISTS `OFFLINE_USER_SESSION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `OFFLINE_USER_SESSION` (
  `USER_SESSION_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `USER_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `LAST_SESSION_REFRESH` int(11) DEFAULT NULL,
  `OFFLINE_FLAG` varchar(4) COLLATE utf8_unicode_ci NOT NULL,
  `DATA` longtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`USER_SESSION_ID`,`OFFLINE_FLAG`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `OFFLINE_USER_SESSION`
--

LOCK TABLES `OFFLINE_USER_SESSION` WRITE;
/*!40000 ALTER TABLE `OFFLINE_USER_SESSION` DISABLE KEYS */;
/*!40000 ALTER TABLE `OFFLINE_USER_SESSION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `POLICY_CONFIG`
--

DROP TABLE IF EXISTS `POLICY_CONFIG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `POLICY_CONFIG` (
  `POLICY_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` longtext COLLATE utf8_unicode_ci,
  PRIMARY KEY (`POLICY_ID`,`NAME`),
  CONSTRAINT `FKDC34197CF864C4E43` FOREIGN KEY (`POLICY_ID`) REFERENCES `RESOURCE_SERVER_POLICY` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `POLICY_CONFIG`
--

LOCK TABLES `POLICY_CONFIG` WRITE;
/*!40000 ALTER TABLE `POLICY_CONFIG` DISABLE KEYS */;
INSERT INTO `POLICY_CONFIG` VALUES ('13d91fcc-ccb0-4a1f-8a47-6de7d53198de','default','true'),('13d91fcc-ccb0-4a1f-8a47-6de7d53198de','defaultResourceType','urn:shanoir-ng-users:resources:default'),('c9e01425-988f-4385-ace5-f8591f4b3199','code','// by default, grants any permission associated with this policy\n$evaluation.grant();\n');
/*!40000 ALTER TABLE `POLICY_CONFIG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PROTOCOL_MAPPER`
--

DROP TABLE IF EXISTS `PROTOCOL_MAPPER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PROTOCOL_MAPPER` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `PROTOCOL` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `PROTOCOL_MAPPER_NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `CONSENT_REQUIRED` bit(1) NOT NULL DEFAULT b'0',
  `CONSENT_TEXT` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CLIENT_TEMPLATE_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_PCM_REALM` (`CLIENT_ID`),
  KEY `FK_CLI_TMPLT_MAPPER` (`CLIENT_TEMPLATE_ID`),
  CONSTRAINT `FK_CLI_TMPLT_MAPPER` FOREIGN KEY (`CLIENT_TEMPLATE_ID`) REFERENCES `CLIENT_TEMPLATE` (`ID`),
  CONSTRAINT `FK_PCM_REALM` FOREIGN KEY (`CLIENT_ID`) REFERENCES `CLIENT` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PROTOCOL_MAPPER`
--

LOCK TABLES `PROTOCOL_MAPPER` WRITE;
/*!40000 ALTER TABLE `PROTOCOL_MAPPER` DISABLE KEYS */;
INSERT INTO `PROTOCOL_MAPPER` VALUES ('0183d784-732b-4124-a202-084c77e19877','email','openid-connect','oidc-usermodel-property-mapper','','${email}','350a782b-05c8-4529-9314-2b5d31ef4918',NULL),('09e307f1-180b-4f63-82ce-23d394f0650b','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','c62bf86f-3741-4051-aea1-c5469a7d2575',NULL),('0a3fb536-0499-4bed-897a-439ae461b061','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','84284718-4123-4bee-a906-3ecd66bea3d1',NULL),('0eb1463b-9c61-4dbb-9479-f6e030d86bee','full name','openid-connect','oidc-full-name-mapper','','${fullName}','d66c079f-4e8a-4012-a2b7-6527ee6ae3cc',NULL),('0f70f56a-154d-4ae2-af0c-1005f9f3d1c4','full name','openid-connect','oidc-full-name-mapper','','${fullName}','350a782b-05c8-4529-9314-2b5d31ef4918',NULL),('116420f9-aa65-4cbf-9db3-bff994deb440','locale','openid-connect','oidc-usermodel-attribute-mapper','\0','${locale}','04236224-4e7a-42d0-ba57-821769bd0a6a',NULL),('1822d0d9-98f1-4169-a17c-a81d8a9af8ed','userId','openid-connect','oidc-usermodel-attribute-mapper','\0',NULL,'de02f346-7bb9-4a9e-9871-722fe1381930',NULL),('1aee067b-4ead-4a1a-9ae3-40cc1bf08a0d','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','c62bf86f-3741-4051-aea1-c5469a7d2575',NULL),('1ce52628-1303-4b0d-9b8c-b5a7d971c3fb','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','676d9349-500c-4e73-8106-1589f2ac812a',NULL),('1e4b2167-d97e-42e2-909b-cc739794cf37','full name','openid-connect','oidc-full-name-mapper','','${fullName}','84284718-4123-4bee-a906-3ecd66bea3d1',NULL),('1fcfcee2-5948-41e9-a445-4df1c251894c','email','openid-connect','oidc-usermodel-property-mapper','','${email}','93635e6b-5f0f-4a37-b304-4337459122aa',NULL),('2151e6c3-22d6-46c9-be98-b61003e65e96','role list','saml','saml-role-list-mapper','\0',NULL,'47b3eb38-8229-4bbb-a0b0-c37cf2ef6227',NULL),('2514db02-88fd-4b62-81ad-8182d6cc0a30','email','openid-connect','oidc-usermodel-property-mapper','','${email}','47b3eb38-8229-4bbb-a0b0-c37cf2ef6227',NULL),('29a9338e-ef10-4df0-a0b6-84b4278d0f93','role list','saml','saml-role-list-mapper','\0',NULL,'676d9349-500c-4e73-8106-1589f2ac812a',NULL),('2b520ea6-b7eb-490b-ab9c-f0a2042e3024','username','openid-connect','oidc-usermodel-property-mapper','','${username}','63cee678-229d-4715-99b7-b319c4cec32d',NULL),('2be472c2-ad70-474c-9418-437707e15744','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','d66c079f-4e8a-4012-a2b7-6527ee6ae3cc',NULL),('2cee6730-1327-4382-a436-2e8e989ac8d2','username','openid-connect','oidc-usermodel-property-mapper','','${username}','c62bf86f-3741-4051-aea1-c5469a7d2575',NULL),('2d33f12c-76a5-4c8e-8656-2bf56deaf288','username','openid-connect','oidc-usermodel-property-mapper','','${username}','d66c079f-4e8a-4012-a2b7-6527ee6ae3cc',NULL),('3086492b-8817-4814-81f9-d7d1a4cf2922','role list','saml','saml-role-list-mapper','\0',NULL,'d015be82-be75-46cd-8f39-d9d6c303adff',NULL),('38e12486-0a0f-4347-8287-b96c9fdbf0c3','role list','saml','saml-role-list-mapper','\0',NULL,'354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL),('3a7b0d11-511f-462e-8986-6982ab439a4e','email','openid-connect','oidc-usermodel-property-mapper','','${email}','c62bf86f-3741-4051-aea1-c5469a7d2575',NULL),('3aae13a5-366a-405a-8ba9-45789c1c9dda','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL),('3b62877d-1176-4eb3-867c-adecc2bf5478','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','93635e6b-5f0f-4a37-b304-4337459122aa',NULL),('3c143752-9e5e-42e9-8092-5d613bbd6f44','username','openid-connect','oidc-usermodel-property-mapper','','${username}','93635e6b-5f0f-4a37-b304-4337459122aa',NULL),('44f4c31d-10cd-4daf-8e53-77508629bb89','full name','openid-connect','oidc-full-name-mapper','','${fullName}','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL),('45672cc2-e2b4-413d-bb0d-cb48bde24ad3','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','d66c079f-4e8a-4012-a2b7-6527ee6ae3cc',NULL),('471b104e-97e9-4932-babd-f97f54087f10','full name','openid-connect','oidc-full-name-mapper','','${fullName}','676d9349-500c-4e73-8106-1589f2ac812a',NULL),('471ecb92-2be5-4757-83df-d33174a36ae6','role list','saml','saml-role-list-mapper','\0',NULL,'de02f346-7bb9-4a9e-9871-722fe1381930',NULL),('4bba4226-a8ca-41cb-baee-3a73dbbbb9a1','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL),('4e3f0d1a-b5bf-4428-b57c-41a811e571c7','role list','saml','saml-role-list-mapper','\0',NULL,'63cee678-229d-4715-99b7-b319c4cec32d',NULL),('591fa463-509d-44d7-b529-aff760ac08c8','username','openid-connect','oidc-usermodel-property-mapper','','${username}','d015be82-be75-46cd-8f39-d9d6c303adff',NULL),('59d056b1-330b-42e6-ae4e-fba34bc28e80','role list','saml','saml-role-list-mapper','\0',NULL,'04236224-4e7a-42d0-ba57-821769bd0a6a',NULL),('5be3648c-210c-41f6-b5c6-10b18632c699','full name','openid-connect','oidc-full-name-mapper','','${fullName}','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL),('5f3cc3e4-c555-4f84-8384-0e367ebdf377','email','openid-connect','oidc-usermodel-property-mapper','','${email}','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL),('5feafbae-7c64-47e8-871f-07e6f1edf7a2','username','openid-connect','oidc-usermodel-property-mapper','','${username}','04236224-4e7a-42d0-ba57-821769bd0a6a',NULL),('71a8b7a4-a914-483e-85b4-75a2328ccb3f','role list','saml','saml-role-list-mapper','\0',NULL,'350a782b-05c8-4529-9314-2b5d31ef4918',NULL),('731dc5cd-2eae-47b6-b3ed-c935603b9c2a','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','676d9349-500c-4e73-8106-1589f2ac812a',NULL),('748ac6a4-3108-4693-a2bd-f282f7f92750','username','openid-connect','oidc-usermodel-property-mapper','','${username}','de02f346-7bb9-4a9e-9871-722fe1381930',NULL),('74ff5fad-f704-46e4-bb9f-a007b65a8a8b','locale','openid-connect','oidc-usermodel-attribute-mapper','\0','${locale}','676d9349-500c-4e73-8106-1589f2ac812a',NULL),('78cfc64d-0309-48a5-82f3-9eb5480f8862','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','de02f346-7bb9-4a9e-9871-722fe1381930',NULL),('7d466b27-0399-4daa-a180-13ff818559b9','email','openid-connect','oidc-usermodel-property-mapper','','${email}','63cee678-229d-4715-99b7-b319c4cec32d',NULL),('7e040557-e2e3-4878-96ba-bd86276f297e','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','04236224-4e7a-42d0-ba57-821769bd0a6a',NULL),('7f06bc19-add7-45f8-a761-99892972df56','role list','saml','saml-role-list-mapper','\0',NULL,'76d1b923-ed53-405b-b798-d1c8ea62d1fe',NULL),('81423ad7-57fe-4af6-a24e-a5e19e58d711','username','openid-connect','oidc-usermodel-property-mapper','','${username}','84284718-4123-4bee-a906-3ecd66bea3d1',NULL),('815f6c39-fee0-4ea7-a752-9ebd881b3017','full name','openid-connect','oidc-full-name-mapper','','${fullName}','04236224-4e7a-42d0-ba57-821769bd0a6a',NULL),('823aba6d-c8bb-4ee5-9fae-a5450b9d3964','full name','openid-connect','oidc-full-name-mapper','','${fullName}','63cee678-229d-4715-99b7-b319c4cec32d',NULL),('83320dee-8a87-4933-9efd-75ce1ff87e7d','email','openid-connect','oidc-usermodel-property-mapper','','${email}','d66c079f-4e8a-4012-a2b7-6527ee6ae3cc',NULL),('8389fbc7-f16e-4899-8953-c6dde428f014','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','84284718-4123-4bee-a906-3ecd66bea3d1',NULL),('859c905e-2e36-416e-81d0-01ae8c7fb9f7','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','93635e6b-5f0f-4a37-b304-4337459122aa',NULL),('866d3f1d-9431-484c-b8b5-4093f2d3217e','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','63cee678-229d-4715-99b7-b319c4cec32d',NULL),('8cd331a1-ebee-4740-b32b-d145abd6b4e1','role list','saml','saml-role-list-mapper','\0',NULL,'c62bf86f-3741-4051-aea1-c5469a7d2575',NULL),('8d699301-3c60-453a-86a1-a949bc031727','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','47b3eb38-8229-4bbb-a0b0-c37cf2ef6227',NULL),('93891677-ef0b-4fbb-b0b0-2b5c54fa8a3f','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','47b3eb38-8229-4bbb-a0b0-c37cf2ef6227',NULL),('9875f824-cb14-4e5a-95cc-5991d995d8d7','email','openid-connect','oidc-usermodel-property-mapper','','${email}','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL),('98a319dd-7c92-4c54-8c18-3b0c94f28b1f','username','openid-connect','oidc-usermodel-property-mapper','','${username}','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL),('9acd8fa5-61e4-4620-962e-1780946fee26','full name','openid-connect','oidc-full-name-mapper','','${fullName}','d015be82-be75-46cd-8f39-d9d6c303adff',NULL),('9bd71c2c-13b3-4ad7-99b7-78d85647d1ee','full name','openid-connect','oidc-full-name-mapper','','${fullName}','76d1b923-ed53-405b-b798-d1c8ea62d1fe',NULL),('9d200305-c1c3-4416-b07f-b29ca056e299','email','openid-connect','oidc-usermodel-property-mapper','','${email}','04236224-4e7a-42d0-ba57-821769bd0a6a',NULL),('a2816730-ae62-467d-bd3e-68f35b040888','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','76d1b923-ed53-405b-b798-d1c8ea62d1fe',NULL),('a4247f64-50f3-479f-9d09-4d3d9640d88a','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','04236224-4e7a-42d0-ba57-821769bd0a6a',NULL),('a457bcd0-504e-493c-b809-843949945528','role list','saml','saml-role-list-mapper','\0',NULL,'f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL),('a45c5fad-6239-454d-946d-e80822bca9ac','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL),('a650963b-d095-42d7-84fe-c6305a53d866','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','350a782b-05c8-4529-9314-2b5d31ef4918',NULL),('ad372798-8bd9-4a02-83ad-49b1a11f6960','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','350a782b-05c8-4529-9314-2b5d31ef4918',NULL),('b05b12d7-54e8-441e-b5b8-67c720fba3f5','username','openid-connect','oidc-usermodel-property-mapper','','${username}','76d1b923-ed53-405b-b798-d1c8ea62d1fe',NULL),('b0e42d51-6083-494d-994a-86e687b39300','role list','saml','saml-role-list-mapper','\0',NULL,'84284718-4123-4bee-a906-3ecd66bea3d1',NULL),('b19e7167-f8f5-44d7-a7ad-801791de34a0','full name','openid-connect','oidc-full-name-mapper','','${fullName}','de02f346-7bb9-4a9e-9871-722fe1381930',NULL),('b5901a18-9c45-4d65-9b2d-b2ca68d9360a','email','openid-connect','oidc-usermodel-property-mapper','','${email}','676d9349-500c-4e73-8106-1589f2ac812a',NULL),('b7dfcaeb-f00e-476a-b0cf-5fe01133a6aa','username','openid-connect','oidc-usermodel-property-mapper','','${username}','676d9349-500c-4e73-8106-1589f2ac812a',NULL),('b874d699-5124-4386-8eb8-aa662caf5c2d','Client ID','openid-connect','oidc-usersessionmodel-note-mapper','\0','','1baebc2e-3c0b-4a04-a067-39dac04320aa',NULL),('bf5e51df-8a30-4036-8ff7-3db7d82fc92f','email','openid-connect','oidc-usermodel-property-mapper','','${email}','de02f346-7bb9-4a9e-9871-722fe1381930',NULL),('ca1d3b68-2787-4adf-ad62-32da2cfe6a29','Client IP Address','openid-connect','oidc-usersessionmodel-note-mapper','\0','','1baebc2e-3c0b-4a04-a067-39dac04320aa',NULL),('cb6e134e-2d5f-4432-891b-03d8c2b2eb29','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','d015be82-be75-46cd-8f39-d9d6c303adff',NULL),('cb8f28a2-4129-455c-978e-78fbaf7246a3','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','63cee678-229d-4715-99b7-b319c4cec32d',NULL),('cdcac9f4-52ae-4518-87c4-df68d7eb53ae','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','d015be82-be75-46cd-8f39-d9d6c303adff',NULL),('d735922b-730b-453e-ab5b-558cd160738c','email','openid-connect','oidc-usermodel-property-mapper','','${email}','84284718-4123-4bee-a906-3ecd66bea3d1',NULL),('d79a9db1-c688-4ad4-923f-7f856932b61d','full name','openid-connect','oidc-full-name-mapper','','${fullName}','47b3eb38-8229-4bbb-a0b0-c37cf2ef6227',NULL),('da31388d-36a8-48dd-b936-5b52050315e3','email','openid-connect','oidc-usermodel-property-mapper','','${email}','d015be82-be75-46cd-8f39-d9d6c303adff',NULL),('daf91ce4-b9b6-4001-9cf2-9b190033c20c','full name','openid-connect','oidc-full-name-mapper','','${fullName}','c62bf86f-3741-4051-aea1-c5469a7d2575',NULL),('dff3db01-e080-4f77-9e2d-935bb9a09ab2','role list','saml','saml-role-list-mapper','\0',NULL,'93635e6b-5f0f-4a37-b304-4337459122aa',NULL),('e605c182-6c81-469a-b4ac-18f936176bcb','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','354f7145-851b-4a84-8d27-cb6622d4eb8a',NULL),('ecc69a33-feaf-4e38-9249-ad3c39e29bba','username','openid-connect','oidc-usermodel-property-mapper','','${username}','f91cb2fc-74db-4aa6-8524-4c42113e7b9f',NULL),('eebf94d8-fe10-498a-aa1d-986b7dd59a84','Client Host','openid-connect','oidc-usersessionmodel-note-mapper','\0','','1baebc2e-3c0b-4a04-a067-39dac04320aa',NULL),('f18681ff-bbf9-45de-ac2b-46394c69b1c5','email','openid-connect','oidc-usermodel-property-mapper','','${email}','76d1b923-ed53-405b-b798-d1c8ea62d1fe',NULL),('f6a219d3-18c5-4c99-9ba7-1c6e56160a69','full name','openid-connect','oidc-full-name-mapper','','${fullName}','93635e6b-5f0f-4a37-b304-4337459122aa',NULL),('f86eeb5e-040f-44ef-b096-8a2d9da2634d','username','openid-connect','oidc-usermodel-property-mapper','','${username}','47b3eb38-8229-4bbb-a0b0-c37cf2ef6227',NULL),('f8b63da0-cdbb-46f5-b1f9-ed86b616fd4b','username','openid-connect','oidc-usermodel-property-mapper','','${username}','350a782b-05c8-4529-9314-2b5d31ef4918',NULL),('fb00763c-652b-4c13-ad38-28e2e48eec01','given name','openid-connect','oidc-usermodel-property-mapper','','${givenName}','de02f346-7bb9-4a9e-9871-722fe1381930',NULL),('fc2e1ad8-49f7-469b-a94d-e3f544cd3d80','family name','openid-connect','oidc-usermodel-property-mapper','','${familyName}','76d1b923-ed53-405b-b798-d1c8ea62d1fe',NULL);
/*!40000 ALTER TABLE `PROTOCOL_MAPPER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `PROTOCOL_MAPPER_CONFIG`
--

DROP TABLE IF EXISTS `PROTOCOL_MAPPER_CONFIG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `PROTOCOL_MAPPER_CONFIG` (
  `PROTOCOL_MAPPER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` longtext COLLATE utf8_unicode_ci,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`PROTOCOL_MAPPER_ID`,`NAME`),
  CONSTRAINT `FK_PMCONFIG` FOREIGN KEY (`PROTOCOL_MAPPER_ID`) REFERENCES `PROTOCOL_MAPPER` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `PROTOCOL_MAPPER_CONFIG`
--

LOCK TABLES `PROTOCOL_MAPPER_CONFIG` WRITE;
/*!40000 ALTER TABLE `PROTOCOL_MAPPER_CONFIG` DISABLE KEYS */;
INSERT INTO `PROTOCOL_MAPPER_CONFIG` VALUES ('0183d784-732b-4124-a202-084c77e19877','true','access.token.claim'),('0183d784-732b-4124-a202-084c77e19877','email','claim.name'),('0183d784-732b-4124-a202-084c77e19877','true','id.token.claim'),('0183d784-732b-4124-a202-084c77e19877','String','jsonType.label'),('0183d784-732b-4124-a202-084c77e19877','email','user.attribute'),('0183d784-732b-4124-a202-084c77e19877','true','userinfo.token.claim'),('09e307f1-180b-4f63-82ce-23d394f0650b','true','access.token.claim'),('09e307f1-180b-4f63-82ce-23d394f0650b','given_name','claim.name'),('09e307f1-180b-4f63-82ce-23d394f0650b','true','id.token.claim'),('09e307f1-180b-4f63-82ce-23d394f0650b','String','jsonType.label'),('09e307f1-180b-4f63-82ce-23d394f0650b','firstName','user.attribute'),('09e307f1-180b-4f63-82ce-23d394f0650b','true','userinfo.token.claim'),('0a3fb536-0499-4bed-897a-439ae461b061','true','access.token.claim'),('0a3fb536-0499-4bed-897a-439ae461b061','family_name','claim.name'),('0a3fb536-0499-4bed-897a-439ae461b061','true','id.token.claim'),('0a3fb536-0499-4bed-897a-439ae461b061','String','jsonType.label'),('0a3fb536-0499-4bed-897a-439ae461b061','lastName','user.attribute'),('0a3fb536-0499-4bed-897a-439ae461b061','true','userinfo.token.claim'),('0eb1463b-9c61-4dbb-9479-f6e030d86bee','true','access.token.claim'),('0eb1463b-9c61-4dbb-9479-f6e030d86bee','true','id.token.claim'),('0eb1463b-9c61-4dbb-9479-f6e030d86bee','true','userinfo.token.claim'),('0f70f56a-154d-4ae2-af0c-1005f9f3d1c4','true','access.token.claim'),('0f70f56a-154d-4ae2-af0c-1005f9f3d1c4','true','id.token.claim'),('116420f9-aa65-4cbf-9db3-bff994deb440','true','access.token.claim'),('116420f9-aa65-4cbf-9db3-bff994deb440','locale','claim.name'),('116420f9-aa65-4cbf-9db3-bff994deb440','true','id.token.claim'),('116420f9-aa65-4cbf-9db3-bff994deb440','String','jsonType.label'),('116420f9-aa65-4cbf-9db3-bff994deb440','locale','user.attribute'),('116420f9-aa65-4cbf-9db3-bff994deb440','true','userinfo.token.claim'),('1822d0d9-98f1-4169-a17c-a81d8a9af8ed','true','access.token.claim'),('1822d0d9-98f1-4169-a17c-a81d8a9af8ed','userId','claim.name'),('1822d0d9-98f1-4169-a17c-a81d8a9af8ed','false','id.token.claim'),('1822d0d9-98f1-4169-a17c-a81d8a9af8ed','long','jsonType.label'),('1822d0d9-98f1-4169-a17c-a81d8a9af8ed','userId','user.attribute'),('1822d0d9-98f1-4169-a17c-a81d8a9af8ed','false','userinfo.token.claim'),('1aee067b-4ead-4a1a-9ae3-40cc1bf08a0d','true','access.token.claim'),('1aee067b-4ead-4a1a-9ae3-40cc1bf08a0d','family_name','claim.name'),('1aee067b-4ead-4a1a-9ae3-40cc1bf08a0d','true','id.token.claim'),('1aee067b-4ead-4a1a-9ae3-40cc1bf08a0d','String','jsonType.label'),('1aee067b-4ead-4a1a-9ae3-40cc1bf08a0d','lastName','user.attribute'),('1aee067b-4ead-4a1a-9ae3-40cc1bf08a0d','true','userinfo.token.claim'),('1ce52628-1303-4b0d-9b8c-b5a7d971c3fb','true','access.token.claim'),('1ce52628-1303-4b0d-9b8c-b5a7d971c3fb','family_name','claim.name'),('1ce52628-1303-4b0d-9b8c-b5a7d971c3fb','true','id.token.claim'),('1ce52628-1303-4b0d-9b8c-b5a7d971c3fb','String','jsonType.label'),('1ce52628-1303-4b0d-9b8c-b5a7d971c3fb','lastName','user.attribute'),('1ce52628-1303-4b0d-9b8c-b5a7d971c3fb','true','userinfo.token.claim'),('1e4b2167-d97e-42e2-909b-cc739794cf37','true','access.token.claim'),('1e4b2167-d97e-42e2-909b-cc739794cf37','true','id.token.claim'),('1fcfcee2-5948-41e9-a445-4df1c251894c','true','access.token.claim'),('1fcfcee2-5948-41e9-a445-4df1c251894c','email','claim.name'),('1fcfcee2-5948-41e9-a445-4df1c251894c','true','id.token.claim'),('1fcfcee2-5948-41e9-a445-4df1c251894c','String','jsonType.label'),('1fcfcee2-5948-41e9-a445-4df1c251894c','email','user.attribute'),('1fcfcee2-5948-41e9-a445-4df1c251894c','true','userinfo.token.claim'),('2151e6c3-22d6-46c9-be98-b61003e65e96','Role','attribute.name'),('2151e6c3-22d6-46c9-be98-b61003e65e96','Basic','attribute.nameformat'),('2151e6c3-22d6-46c9-be98-b61003e65e96','false','single'),('2514db02-88fd-4b62-81ad-8182d6cc0a30','true','access.token.claim'),('2514db02-88fd-4b62-81ad-8182d6cc0a30','email','claim.name'),('2514db02-88fd-4b62-81ad-8182d6cc0a30','true','id.token.claim'),('2514db02-88fd-4b62-81ad-8182d6cc0a30','String','jsonType.label'),('2514db02-88fd-4b62-81ad-8182d6cc0a30','email','user.attribute'),('2514db02-88fd-4b62-81ad-8182d6cc0a30','true','userinfo.token.claim'),('29a9338e-ef10-4df0-a0b6-84b4278d0f93','Role','attribute.name'),('29a9338e-ef10-4df0-a0b6-84b4278d0f93','Basic','attribute.nameformat'),('29a9338e-ef10-4df0-a0b6-84b4278d0f93','false','single'),('2b520ea6-b7eb-490b-ab9c-f0a2042e3024','true','access.token.claim'),('2b520ea6-b7eb-490b-ab9c-f0a2042e3024','preferred_username','claim.name'),('2b520ea6-b7eb-490b-ab9c-f0a2042e3024','true','id.token.claim'),('2b520ea6-b7eb-490b-ab9c-f0a2042e3024','String','jsonType.label'),('2b520ea6-b7eb-490b-ab9c-f0a2042e3024','username','user.attribute'),('2b520ea6-b7eb-490b-ab9c-f0a2042e3024','true','userinfo.token.claim'),('2be472c2-ad70-474c-9418-437707e15744','true','access.token.claim'),('2be472c2-ad70-474c-9418-437707e15744','given_name','claim.name'),('2be472c2-ad70-474c-9418-437707e15744','true','id.token.claim'),('2be472c2-ad70-474c-9418-437707e15744','String','jsonType.label'),('2be472c2-ad70-474c-9418-437707e15744','firstName','user.attribute'),('2be472c2-ad70-474c-9418-437707e15744','true','userinfo.token.claim'),('2cee6730-1327-4382-a436-2e8e989ac8d2','true','access.token.claim'),('2cee6730-1327-4382-a436-2e8e989ac8d2','preferred_username','claim.name'),('2cee6730-1327-4382-a436-2e8e989ac8d2','true','id.token.claim'),('2cee6730-1327-4382-a436-2e8e989ac8d2','String','jsonType.label'),('2cee6730-1327-4382-a436-2e8e989ac8d2','username','user.attribute'),('2cee6730-1327-4382-a436-2e8e989ac8d2','true','userinfo.token.claim'),('2d33f12c-76a5-4c8e-8656-2bf56deaf288','true','access.token.claim'),('2d33f12c-76a5-4c8e-8656-2bf56deaf288','preferred_username','claim.name'),('2d33f12c-76a5-4c8e-8656-2bf56deaf288','true','id.token.claim'),('2d33f12c-76a5-4c8e-8656-2bf56deaf288','String','jsonType.label'),('2d33f12c-76a5-4c8e-8656-2bf56deaf288','username','user.attribute'),('2d33f12c-76a5-4c8e-8656-2bf56deaf288','true','userinfo.token.claim'),('3086492b-8817-4814-81f9-d7d1a4cf2922','Role','attribute.name'),('3086492b-8817-4814-81f9-d7d1a4cf2922','Basic','attribute.nameformat'),('3086492b-8817-4814-81f9-d7d1a4cf2922','false','single'),('38e12486-0a0f-4347-8287-b96c9fdbf0c3','Role','attribute.name'),('38e12486-0a0f-4347-8287-b96c9fdbf0c3','Basic','attribute.nameformat'),('38e12486-0a0f-4347-8287-b96c9fdbf0c3','false','single'),('3a7b0d11-511f-462e-8986-6982ab439a4e','true','access.token.claim'),('3a7b0d11-511f-462e-8986-6982ab439a4e','email','claim.name'),('3a7b0d11-511f-462e-8986-6982ab439a4e','true','id.token.claim'),('3a7b0d11-511f-462e-8986-6982ab439a4e','String','jsonType.label'),('3a7b0d11-511f-462e-8986-6982ab439a4e','email','user.attribute'),('3a7b0d11-511f-462e-8986-6982ab439a4e','true','userinfo.token.claim'),('3aae13a5-366a-405a-8ba9-45789c1c9dda','true','access.token.claim'),('3aae13a5-366a-405a-8ba9-45789c1c9dda','given_name','claim.name'),('3aae13a5-366a-405a-8ba9-45789c1c9dda','true','id.token.claim'),('3aae13a5-366a-405a-8ba9-45789c1c9dda','String','jsonType.label'),('3aae13a5-366a-405a-8ba9-45789c1c9dda','firstName','user.attribute'),('3aae13a5-366a-405a-8ba9-45789c1c9dda','true','userinfo.token.claim'),('3b62877d-1176-4eb3-867c-adecc2bf5478','true','access.token.claim'),('3b62877d-1176-4eb3-867c-adecc2bf5478','family_name','claim.name'),('3b62877d-1176-4eb3-867c-adecc2bf5478','true','id.token.claim'),('3b62877d-1176-4eb3-867c-adecc2bf5478','String','jsonType.label'),('3b62877d-1176-4eb3-867c-adecc2bf5478','lastName','user.attribute'),('3b62877d-1176-4eb3-867c-adecc2bf5478','true','userinfo.token.claim'),('3c143752-9e5e-42e9-8092-5d613bbd6f44','true','access.token.claim'),('3c143752-9e5e-42e9-8092-5d613bbd6f44','preferred_username','claim.name'),('3c143752-9e5e-42e9-8092-5d613bbd6f44','true','id.token.claim'),('3c143752-9e5e-42e9-8092-5d613bbd6f44','String','jsonType.label'),('3c143752-9e5e-42e9-8092-5d613bbd6f44','username','user.attribute'),('3c143752-9e5e-42e9-8092-5d613bbd6f44','true','userinfo.token.claim'),('44f4c31d-10cd-4daf-8e53-77508629bb89','true','access.token.claim'),('44f4c31d-10cd-4daf-8e53-77508629bb89','true','id.token.claim'),('45672cc2-e2b4-413d-bb0d-cb48bde24ad3','true','access.token.claim'),('45672cc2-e2b4-413d-bb0d-cb48bde24ad3','family_name','claim.name'),('45672cc2-e2b4-413d-bb0d-cb48bde24ad3','true','id.token.claim'),('45672cc2-e2b4-413d-bb0d-cb48bde24ad3','String','jsonType.label'),('45672cc2-e2b4-413d-bb0d-cb48bde24ad3','lastName','user.attribute'),('45672cc2-e2b4-413d-bb0d-cb48bde24ad3','true','userinfo.token.claim'),('471b104e-97e9-4932-babd-f97f54087f10','true','access.token.claim'),('471b104e-97e9-4932-babd-f97f54087f10','true','id.token.claim'),('471ecb92-2be5-4757-83df-d33174a36ae6','Role','attribute.name'),('471ecb92-2be5-4757-83df-d33174a36ae6','Basic','attribute.nameformat'),('471ecb92-2be5-4757-83df-d33174a36ae6','false','single'),('4bba4226-a8ca-41cb-baee-3a73dbbbb9a1','true','access.token.claim'),('4bba4226-a8ca-41cb-baee-3a73dbbbb9a1','family_name','claim.name'),('4bba4226-a8ca-41cb-baee-3a73dbbbb9a1','true','id.token.claim'),('4bba4226-a8ca-41cb-baee-3a73dbbbb9a1','String','jsonType.label'),('4bba4226-a8ca-41cb-baee-3a73dbbbb9a1','lastName','user.attribute'),('4bba4226-a8ca-41cb-baee-3a73dbbbb9a1','true','userinfo.token.claim'),('4e3f0d1a-b5bf-4428-b57c-41a811e571c7','Role','attribute.name'),('4e3f0d1a-b5bf-4428-b57c-41a811e571c7','Basic','attribute.nameformat'),('4e3f0d1a-b5bf-4428-b57c-41a811e571c7','false','single'),('591fa463-509d-44d7-b529-aff760ac08c8','true','access.token.claim'),('591fa463-509d-44d7-b529-aff760ac08c8','preferred_username','claim.name'),('591fa463-509d-44d7-b529-aff760ac08c8','true','id.token.claim'),('591fa463-509d-44d7-b529-aff760ac08c8','String','jsonType.label'),('591fa463-509d-44d7-b529-aff760ac08c8','username','user.attribute'),('591fa463-509d-44d7-b529-aff760ac08c8','true','userinfo.token.claim'),('59d056b1-330b-42e6-ae4e-fba34bc28e80','Role','attribute.name'),('59d056b1-330b-42e6-ae4e-fba34bc28e80','Basic','attribute.nameformat'),('59d056b1-330b-42e6-ae4e-fba34bc28e80','false','single'),('5be3648c-210c-41f6-b5c6-10b18632c699','true','access.token.claim'),('5be3648c-210c-41f6-b5c6-10b18632c699','true','id.token.claim'),('5f3cc3e4-c555-4f84-8384-0e367ebdf377','true','access.token.claim'),('5f3cc3e4-c555-4f84-8384-0e367ebdf377','email','claim.name'),('5f3cc3e4-c555-4f84-8384-0e367ebdf377','true','id.token.claim'),('5f3cc3e4-c555-4f84-8384-0e367ebdf377','String','jsonType.label'),('5f3cc3e4-c555-4f84-8384-0e367ebdf377','email','user.attribute'),('5f3cc3e4-c555-4f84-8384-0e367ebdf377','true','userinfo.token.claim'),('5feafbae-7c64-47e8-871f-07e6f1edf7a2','true','access.token.claim'),('5feafbae-7c64-47e8-871f-07e6f1edf7a2','preferred_username','claim.name'),('5feafbae-7c64-47e8-871f-07e6f1edf7a2','true','id.token.claim'),('5feafbae-7c64-47e8-871f-07e6f1edf7a2','String','jsonType.label'),('5feafbae-7c64-47e8-871f-07e6f1edf7a2','username','user.attribute'),('5feafbae-7c64-47e8-871f-07e6f1edf7a2','true','userinfo.token.claim'),('71a8b7a4-a914-483e-85b4-75a2328ccb3f','Role','attribute.name'),('71a8b7a4-a914-483e-85b4-75a2328ccb3f','Basic','attribute.nameformat'),('71a8b7a4-a914-483e-85b4-75a2328ccb3f','false','single'),('731dc5cd-2eae-47b6-b3ed-c935603b9c2a','true','access.token.claim'),('731dc5cd-2eae-47b6-b3ed-c935603b9c2a','given_name','claim.name'),('731dc5cd-2eae-47b6-b3ed-c935603b9c2a','true','id.token.claim'),('731dc5cd-2eae-47b6-b3ed-c935603b9c2a','String','jsonType.label'),('731dc5cd-2eae-47b6-b3ed-c935603b9c2a','firstName','user.attribute'),('731dc5cd-2eae-47b6-b3ed-c935603b9c2a','true','userinfo.token.claim'),('748ac6a4-3108-4693-a2bd-f282f7f92750','true','access.token.claim'),('748ac6a4-3108-4693-a2bd-f282f7f92750','preferred_username','claim.name'),('748ac6a4-3108-4693-a2bd-f282f7f92750','true','id.token.claim'),('748ac6a4-3108-4693-a2bd-f282f7f92750','String','jsonType.label'),('748ac6a4-3108-4693-a2bd-f282f7f92750','username','user.attribute'),('748ac6a4-3108-4693-a2bd-f282f7f92750','true','userinfo.token.claim'),('74ff5fad-f704-46e4-bb9f-a007b65a8a8b','true','access.token.claim'),('74ff5fad-f704-46e4-bb9f-a007b65a8a8b','locale','claim.name'),('74ff5fad-f704-46e4-bb9f-a007b65a8a8b','true','id.token.claim'),('74ff5fad-f704-46e4-bb9f-a007b65a8a8b','String','jsonType.label'),('74ff5fad-f704-46e4-bb9f-a007b65a8a8b','locale','user.attribute'),('74ff5fad-f704-46e4-bb9f-a007b65a8a8b','true','userinfo.token.claim'),('78cfc64d-0309-48a5-82f3-9eb5480f8862','true','access.token.claim'),('78cfc64d-0309-48a5-82f3-9eb5480f8862','family_name','claim.name'),('78cfc64d-0309-48a5-82f3-9eb5480f8862','true','id.token.claim'),('78cfc64d-0309-48a5-82f3-9eb5480f8862','String','jsonType.label'),('78cfc64d-0309-48a5-82f3-9eb5480f8862','lastName','user.attribute'),('78cfc64d-0309-48a5-82f3-9eb5480f8862','true','userinfo.token.claim'),('7d466b27-0399-4daa-a180-13ff818559b9','true','access.token.claim'),('7d466b27-0399-4daa-a180-13ff818559b9','email','claim.name'),('7d466b27-0399-4daa-a180-13ff818559b9','true','id.token.claim'),('7d466b27-0399-4daa-a180-13ff818559b9','String','jsonType.label'),('7d466b27-0399-4daa-a180-13ff818559b9','email','user.attribute'),('7d466b27-0399-4daa-a180-13ff818559b9','true','userinfo.token.claim'),('7e040557-e2e3-4878-96ba-bd86276f297e','true','access.token.claim'),('7e040557-e2e3-4878-96ba-bd86276f297e','family_name','claim.name'),('7e040557-e2e3-4878-96ba-bd86276f297e','true','id.token.claim'),('7e040557-e2e3-4878-96ba-bd86276f297e','String','jsonType.label'),('7e040557-e2e3-4878-96ba-bd86276f297e','lastName','user.attribute'),('7e040557-e2e3-4878-96ba-bd86276f297e','true','userinfo.token.claim'),('7f06bc19-add7-45f8-a761-99892972df56','Role','attribute.name'),('7f06bc19-add7-45f8-a761-99892972df56','Basic','attribute.nameformat'),('7f06bc19-add7-45f8-a761-99892972df56','false','single'),('81423ad7-57fe-4af6-a24e-a5e19e58d711','true','access.token.claim'),('81423ad7-57fe-4af6-a24e-a5e19e58d711','preferred_username','claim.name'),('81423ad7-57fe-4af6-a24e-a5e19e58d711','true','id.token.claim'),('81423ad7-57fe-4af6-a24e-a5e19e58d711','String','jsonType.label'),('81423ad7-57fe-4af6-a24e-a5e19e58d711','username','user.attribute'),('81423ad7-57fe-4af6-a24e-a5e19e58d711','true','userinfo.token.claim'),('815f6c39-fee0-4ea7-a752-9ebd881b3017','true','access.token.claim'),('815f6c39-fee0-4ea7-a752-9ebd881b3017','true','id.token.claim'),('823aba6d-c8bb-4ee5-9fae-a5450b9d3964','true','access.token.claim'),('823aba6d-c8bb-4ee5-9fae-a5450b9d3964','true','id.token.claim'),('83320dee-8a87-4933-9efd-75ce1ff87e7d','true','access.token.claim'),('83320dee-8a87-4933-9efd-75ce1ff87e7d','email','claim.name'),('83320dee-8a87-4933-9efd-75ce1ff87e7d','true','id.token.claim'),('83320dee-8a87-4933-9efd-75ce1ff87e7d','String','jsonType.label'),('83320dee-8a87-4933-9efd-75ce1ff87e7d','email','user.attribute'),('83320dee-8a87-4933-9efd-75ce1ff87e7d','true','userinfo.token.claim'),('8389fbc7-f16e-4899-8953-c6dde428f014','true','access.token.claim'),('8389fbc7-f16e-4899-8953-c6dde428f014','given_name','claim.name'),('8389fbc7-f16e-4899-8953-c6dde428f014','true','id.token.claim'),('8389fbc7-f16e-4899-8953-c6dde428f014','String','jsonType.label'),('8389fbc7-f16e-4899-8953-c6dde428f014','firstName','user.attribute'),('8389fbc7-f16e-4899-8953-c6dde428f014','true','userinfo.token.claim'),('859c905e-2e36-416e-81d0-01ae8c7fb9f7','true','access.token.claim'),('859c905e-2e36-416e-81d0-01ae8c7fb9f7','given_name','claim.name'),('859c905e-2e36-416e-81d0-01ae8c7fb9f7','true','id.token.claim'),('859c905e-2e36-416e-81d0-01ae8c7fb9f7','String','jsonType.label'),('859c905e-2e36-416e-81d0-01ae8c7fb9f7','firstName','user.attribute'),('859c905e-2e36-416e-81d0-01ae8c7fb9f7','true','userinfo.token.claim'),('866d3f1d-9431-484c-b8b5-4093f2d3217e','true','access.token.claim'),('866d3f1d-9431-484c-b8b5-4093f2d3217e','given_name','claim.name'),('866d3f1d-9431-484c-b8b5-4093f2d3217e','true','id.token.claim'),('866d3f1d-9431-484c-b8b5-4093f2d3217e','String','jsonType.label'),('866d3f1d-9431-484c-b8b5-4093f2d3217e','firstName','user.attribute'),('866d3f1d-9431-484c-b8b5-4093f2d3217e','true','userinfo.token.claim'),('8cd331a1-ebee-4740-b32b-d145abd6b4e1','Role','attribute.name'),('8cd331a1-ebee-4740-b32b-d145abd6b4e1','Basic','attribute.nameformat'),('8cd331a1-ebee-4740-b32b-d145abd6b4e1','false','single'),('8d699301-3c60-453a-86a1-a949bc031727','true','access.token.claim'),('8d699301-3c60-453a-86a1-a949bc031727','family_name','claim.name'),('8d699301-3c60-453a-86a1-a949bc031727','true','id.token.claim'),('8d699301-3c60-453a-86a1-a949bc031727','String','jsonType.label'),('8d699301-3c60-453a-86a1-a949bc031727','lastName','user.attribute'),('8d699301-3c60-453a-86a1-a949bc031727','true','userinfo.token.claim'),('93891677-ef0b-4fbb-b0b0-2b5c54fa8a3f','true','access.token.claim'),('93891677-ef0b-4fbb-b0b0-2b5c54fa8a3f','given_name','claim.name'),('93891677-ef0b-4fbb-b0b0-2b5c54fa8a3f','true','id.token.claim'),('93891677-ef0b-4fbb-b0b0-2b5c54fa8a3f','String','jsonType.label'),('93891677-ef0b-4fbb-b0b0-2b5c54fa8a3f','firstName','user.attribute'),('93891677-ef0b-4fbb-b0b0-2b5c54fa8a3f','true','userinfo.token.claim'),('9875f824-cb14-4e5a-95cc-5991d995d8d7','true','access.token.claim'),('9875f824-cb14-4e5a-95cc-5991d995d8d7','email','claim.name'),('9875f824-cb14-4e5a-95cc-5991d995d8d7','true','id.token.claim'),('9875f824-cb14-4e5a-95cc-5991d995d8d7','String','jsonType.label'),('9875f824-cb14-4e5a-95cc-5991d995d8d7','email','user.attribute'),('9875f824-cb14-4e5a-95cc-5991d995d8d7','true','userinfo.token.claim'),('98a319dd-7c92-4c54-8c18-3b0c94f28b1f','true','access.token.claim'),('98a319dd-7c92-4c54-8c18-3b0c94f28b1f','preferred_username','claim.name'),('98a319dd-7c92-4c54-8c18-3b0c94f28b1f','true','id.token.claim'),('98a319dd-7c92-4c54-8c18-3b0c94f28b1f','String','jsonType.label'),('98a319dd-7c92-4c54-8c18-3b0c94f28b1f','username','user.attribute'),('98a319dd-7c92-4c54-8c18-3b0c94f28b1f','true','userinfo.token.claim'),('9acd8fa5-61e4-4620-962e-1780946fee26','true','access.token.claim'),('9acd8fa5-61e4-4620-962e-1780946fee26','true','id.token.claim'),('9bd71c2c-13b3-4ad7-99b7-78d85647d1ee','true','access.token.claim'),('9bd71c2c-13b3-4ad7-99b7-78d85647d1ee','true','id.token.claim'),('9d200305-c1c3-4416-b07f-b29ca056e299','true','access.token.claim'),('9d200305-c1c3-4416-b07f-b29ca056e299','email','claim.name'),('9d200305-c1c3-4416-b07f-b29ca056e299','true','id.token.claim'),('9d200305-c1c3-4416-b07f-b29ca056e299','String','jsonType.label'),('9d200305-c1c3-4416-b07f-b29ca056e299','email','user.attribute'),('9d200305-c1c3-4416-b07f-b29ca056e299','true','userinfo.token.claim'),('a2816730-ae62-467d-bd3e-68f35b040888','true','access.token.claim'),('a2816730-ae62-467d-bd3e-68f35b040888','given_name','claim.name'),('a2816730-ae62-467d-bd3e-68f35b040888','true','id.token.claim'),('a2816730-ae62-467d-bd3e-68f35b040888','String','jsonType.label'),('a2816730-ae62-467d-bd3e-68f35b040888','firstName','user.attribute'),('a2816730-ae62-467d-bd3e-68f35b040888','true','userinfo.token.claim'),('a4247f64-50f3-479f-9d09-4d3d9640d88a','true','access.token.claim'),('a4247f64-50f3-479f-9d09-4d3d9640d88a','given_name','claim.name'),('a4247f64-50f3-479f-9d09-4d3d9640d88a','true','id.token.claim'),('a4247f64-50f3-479f-9d09-4d3d9640d88a','String','jsonType.label'),('a4247f64-50f3-479f-9d09-4d3d9640d88a','firstName','user.attribute'),('a4247f64-50f3-479f-9d09-4d3d9640d88a','true','userinfo.token.claim'),('a457bcd0-504e-493c-b809-843949945528','Role','attribute.name'),('a457bcd0-504e-493c-b809-843949945528','Basic','attribute.nameformat'),('a457bcd0-504e-493c-b809-843949945528','false','single'),('a45c5fad-6239-454d-946d-e80822bca9ac','true','access.token.claim'),('a45c5fad-6239-454d-946d-e80822bca9ac','given_name','claim.name'),('a45c5fad-6239-454d-946d-e80822bca9ac','true','id.token.claim'),('a45c5fad-6239-454d-946d-e80822bca9ac','String','jsonType.label'),('a45c5fad-6239-454d-946d-e80822bca9ac','firstName','user.attribute'),('a45c5fad-6239-454d-946d-e80822bca9ac','true','userinfo.token.claim'),('a650963b-d095-42d7-84fe-c6305a53d866','true','access.token.claim'),('a650963b-d095-42d7-84fe-c6305a53d866','given_name','claim.name'),('a650963b-d095-42d7-84fe-c6305a53d866','true','id.token.claim'),('a650963b-d095-42d7-84fe-c6305a53d866','String','jsonType.label'),('a650963b-d095-42d7-84fe-c6305a53d866','firstName','user.attribute'),('a650963b-d095-42d7-84fe-c6305a53d866','true','userinfo.token.claim'),('ad372798-8bd9-4a02-83ad-49b1a11f6960','true','access.token.claim'),('ad372798-8bd9-4a02-83ad-49b1a11f6960','family_name','claim.name'),('ad372798-8bd9-4a02-83ad-49b1a11f6960','true','id.token.claim'),('ad372798-8bd9-4a02-83ad-49b1a11f6960','String','jsonType.label'),('ad372798-8bd9-4a02-83ad-49b1a11f6960','lastName','user.attribute'),('ad372798-8bd9-4a02-83ad-49b1a11f6960','true','userinfo.token.claim'),('b05b12d7-54e8-441e-b5b8-67c720fba3f5','true','access.token.claim'),('b05b12d7-54e8-441e-b5b8-67c720fba3f5','preferred_username','claim.name'),('b05b12d7-54e8-441e-b5b8-67c720fba3f5','true','id.token.claim'),('b05b12d7-54e8-441e-b5b8-67c720fba3f5','String','jsonType.label'),('b05b12d7-54e8-441e-b5b8-67c720fba3f5','username','user.attribute'),('b05b12d7-54e8-441e-b5b8-67c720fba3f5','true','userinfo.token.claim'),('b0e42d51-6083-494d-994a-86e687b39300','Role','attribute.name'),('b0e42d51-6083-494d-994a-86e687b39300','Basic','attribute.nameformat'),('b0e42d51-6083-494d-994a-86e687b39300','false','single'),('b19e7167-f8f5-44d7-a7ad-801791de34a0','true','access.token.claim'),('b19e7167-f8f5-44d7-a7ad-801791de34a0','true','id.token.claim'),('b19e7167-f8f5-44d7-a7ad-801791de34a0','true','userinfo.token.claim'),('b5901a18-9c45-4d65-9b2d-b2ca68d9360a','true','access.token.claim'),('b5901a18-9c45-4d65-9b2d-b2ca68d9360a','email','claim.name'),('b5901a18-9c45-4d65-9b2d-b2ca68d9360a','true','id.token.claim'),('b5901a18-9c45-4d65-9b2d-b2ca68d9360a','String','jsonType.label'),('b5901a18-9c45-4d65-9b2d-b2ca68d9360a','email','user.attribute'),('b5901a18-9c45-4d65-9b2d-b2ca68d9360a','true','userinfo.token.claim'),('b7dfcaeb-f00e-476a-b0cf-5fe01133a6aa','true','access.token.claim'),('b7dfcaeb-f00e-476a-b0cf-5fe01133a6aa','preferred_username','claim.name'),('b7dfcaeb-f00e-476a-b0cf-5fe01133a6aa','true','id.token.claim'),('b7dfcaeb-f00e-476a-b0cf-5fe01133a6aa','String','jsonType.label'),('b7dfcaeb-f00e-476a-b0cf-5fe01133a6aa','username','user.attribute'),('b7dfcaeb-f00e-476a-b0cf-5fe01133a6aa','true','userinfo.token.claim'),('b874d699-5124-4386-8eb8-aa662caf5c2d','true','access.token.claim'),('b874d699-5124-4386-8eb8-aa662caf5c2d','clientId','claim.name'),('b874d699-5124-4386-8eb8-aa662caf5c2d','true','id.token.claim'),('b874d699-5124-4386-8eb8-aa662caf5c2d','String','jsonType.label'),('b874d699-5124-4386-8eb8-aa662caf5c2d','clientId','user.session.note'),('b874d699-5124-4386-8eb8-aa662caf5c2d','true','userinfo.token.claim'),('bf5e51df-8a30-4036-8ff7-3db7d82fc92f','true','access.token.claim'),('bf5e51df-8a30-4036-8ff7-3db7d82fc92f','email','claim.name'),('bf5e51df-8a30-4036-8ff7-3db7d82fc92f','true','id.token.claim'),('bf5e51df-8a30-4036-8ff7-3db7d82fc92f','String','jsonType.label'),('bf5e51df-8a30-4036-8ff7-3db7d82fc92f','email','user.attribute'),('bf5e51df-8a30-4036-8ff7-3db7d82fc92f','true','userinfo.token.claim'),('ca1d3b68-2787-4adf-ad62-32da2cfe6a29','true','access.token.claim'),('ca1d3b68-2787-4adf-ad62-32da2cfe6a29','clientAddress','claim.name'),('ca1d3b68-2787-4adf-ad62-32da2cfe6a29','true','id.token.claim'),('ca1d3b68-2787-4adf-ad62-32da2cfe6a29','String','jsonType.label'),('ca1d3b68-2787-4adf-ad62-32da2cfe6a29','clientAddress','user.session.note'),('ca1d3b68-2787-4adf-ad62-32da2cfe6a29','true','userinfo.token.claim'),('cb6e134e-2d5f-4432-891b-03d8c2b2eb29','true','access.token.claim'),('cb6e134e-2d5f-4432-891b-03d8c2b2eb29','family_name','claim.name'),('cb6e134e-2d5f-4432-891b-03d8c2b2eb29','true','id.token.claim'),('cb6e134e-2d5f-4432-891b-03d8c2b2eb29','String','jsonType.label'),('cb6e134e-2d5f-4432-891b-03d8c2b2eb29','lastName','user.attribute'),('cb6e134e-2d5f-4432-891b-03d8c2b2eb29','true','userinfo.token.claim'),('cb8f28a2-4129-455c-978e-78fbaf7246a3','true','access.token.claim'),('cb8f28a2-4129-455c-978e-78fbaf7246a3','family_name','claim.name'),('cb8f28a2-4129-455c-978e-78fbaf7246a3','true','id.token.claim'),('cb8f28a2-4129-455c-978e-78fbaf7246a3','String','jsonType.label'),('cb8f28a2-4129-455c-978e-78fbaf7246a3','lastName','user.attribute'),('cb8f28a2-4129-455c-978e-78fbaf7246a3','true','userinfo.token.claim'),('cdcac9f4-52ae-4518-87c4-df68d7eb53ae','true','access.token.claim'),('cdcac9f4-52ae-4518-87c4-df68d7eb53ae','given_name','claim.name'),('cdcac9f4-52ae-4518-87c4-df68d7eb53ae','true','id.token.claim'),('cdcac9f4-52ae-4518-87c4-df68d7eb53ae','String','jsonType.label'),('cdcac9f4-52ae-4518-87c4-df68d7eb53ae','firstName','user.attribute'),('cdcac9f4-52ae-4518-87c4-df68d7eb53ae','true','userinfo.token.claim'),('d735922b-730b-453e-ab5b-558cd160738c','true','access.token.claim'),('d735922b-730b-453e-ab5b-558cd160738c','email','claim.name'),('d735922b-730b-453e-ab5b-558cd160738c','true','id.token.claim'),('d735922b-730b-453e-ab5b-558cd160738c','String','jsonType.label'),('d735922b-730b-453e-ab5b-558cd160738c','email','user.attribute'),('d735922b-730b-453e-ab5b-558cd160738c','true','userinfo.token.claim'),('d79a9db1-c688-4ad4-923f-7f856932b61d','true','access.token.claim'),('d79a9db1-c688-4ad4-923f-7f856932b61d','true','id.token.claim'),('da31388d-36a8-48dd-b936-5b52050315e3','true','access.token.claim'),('da31388d-36a8-48dd-b936-5b52050315e3','email','claim.name'),('da31388d-36a8-48dd-b936-5b52050315e3','true','id.token.claim'),('da31388d-36a8-48dd-b936-5b52050315e3','String','jsonType.label'),('da31388d-36a8-48dd-b936-5b52050315e3','email','user.attribute'),('da31388d-36a8-48dd-b936-5b52050315e3','true','userinfo.token.claim'),('daf91ce4-b9b6-4001-9cf2-9b190033c20c','true','access.token.claim'),('daf91ce4-b9b6-4001-9cf2-9b190033c20c','true','id.token.claim'),('daf91ce4-b9b6-4001-9cf2-9b190033c20c','true','userinfo.token.claim'),('dff3db01-e080-4f77-9e2d-935bb9a09ab2','Role','attribute.name'),('dff3db01-e080-4f77-9e2d-935bb9a09ab2','Basic','attribute.nameformat'),('dff3db01-e080-4f77-9e2d-935bb9a09ab2','false','single'),('e605c182-6c81-469a-b4ac-18f936176bcb','true','access.token.claim'),('e605c182-6c81-469a-b4ac-18f936176bcb','family_name','claim.name'),('e605c182-6c81-469a-b4ac-18f936176bcb','true','id.token.claim'),('e605c182-6c81-469a-b4ac-18f936176bcb','String','jsonType.label'),('e605c182-6c81-469a-b4ac-18f936176bcb','lastName','user.attribute'),('e605c182-6c81-469a-b4ac-18f936176bcb','true','userinfo.token.claim'),('ecc69a33-feaf-4e38-9249-ad3c39e29bba','true','access.token.claim'),('ecc69a33-feaf-4e38-9249-ad3c39e29bba','preferred_username','claim.name'),('ecc69a33-feaf-4e38-9249-ad3c39e29bba','true','id.token.claim'),('ecc69a33-feaf-4e38-9249-ad3c39e29bba','String','jsonType.label'),('ecc69a33-feaf-4e38-9249-ad3c39e29bba','username','user.attribute'),('ecc69a33-feaf-4e38-9249-ad3c39e29bba','true','userinfo.token.claim'),('eebf94d8-fe10-498a-aa1d-986b7dd59a84','true','access.token.claim'),('eebf94d8-fe10-498a-aa1d-986b7dd59a84','clientHost','claim.name'),('eebf94d8-fe10-498a-aa1d-986b7dd59a84','true','id.token.claim'),('eebf94d8-fe10-498a-aa1d-986b7dd59a84','String','jsonType.label'),('eebf94d8-fe10-498a-aa1d-986b7dd59a84','clientHost','user.session.note'),('eebf94d8-fe10-498a-aa1d-986b7dd59a84','true','userinfo.token.claim'),('f18681ff-bbf9-45de-ac2b-46394c69b1c5','true','access.token.claim'),('f18681ff-bbf9-45de-ac2b-46394c69b1c5','email','claim.name'),('f18681ff-bbf9-45de-ac2b-46394c69b1c5','true','id.token.claim'),('f18681ff-bbf9-45de-ac2b-46394c69b1c5','String','jsonType.label'),('f18681ff-bbf9-45de-ac2b-46394c69b1c5','email','user.attribute'),('f18681ff-bbf9-45de-ac2b-46394c69b1c5','true','userinfo.token.claim'),('f6a219d3-18c5-4c99-9ba7-1c6e56160a69','true','access.token.claim'),('f6a219d3-18c5-4c99-9ba7-1c6e56160a69','true','id.token.claim'),('f86eeb5e-040f-44ef-b096-8a2d9da2634d','true','access.token.claim'),('f86eeb5e-040f-44ef-b096-8a2d9da2634d','preferred_username','claim.name'),('f86eeb5e-040f-44ef-b096-8a2d9da2634d','true','id.token.claim'),('f86eeb5e-040f-44ef-b096-8a2d9da2634d','String','jsonType.label'),('f86eeb5e-040f-44ef-b096-8a2d9da2634d','username','user.attribute'),('f86eeb5e-040f-44ef-b096-8a2d9da2634d','true','userinfo.token.claim'),('f8b63da0-cdbb-46f5-b1f9-ed86b616fd4b','true','access.token.claim'),('f8b63da0-cdbb-46f5-b1f9-ed86b616fd4b','preferred_username','claim.name'),('f8b63da0-cdbb-46f5-b1f9-ed86b616fd4b','true','id.token.claim'),('f8b63da0-cdbb-46f5-b1f9-ed86b616fd4b','String','jsonType.label'),('f8b63da0-cdbb-46f5-b1f9-ed86b616fd4b','username','user.attribute'),('f8b63da0-cdbb-46f5-b1f9-ed86b616fd4b','true','userinfo.token.claim'),('fb00763c-652b-4c13-ad38-28e2e48eec01','true','access.token.claim'),('fb00763c-652b-4c13-ad38-28e2e48eec01','given_name','claim.name'),('fb00763c-652b-4c13-ad38-28e2e48eec01','true','id.token.claim'),('fb00763c-652b-4c13-ad38-28e2e48eec01','String','jsonType.label'),('fb00763c-652b-4c13-ad38-28e2e48eec01','firstName','user.attribute'),('fb00763c-652b-4c13-ad38-28e2e48eec01','true','userinfo.token.claim'),('fc2e1ad8-49f7-469b-a94d-e3f544cd3d80','true','access.token.claim'),('fc2e1ad8-49f7-469b-a94d-e3f544cd3d80','family_name','claim.name'),('fc2e1ad8-49f7-469b-a94d-e3f544cd3d80','true','id.token.claim'),('fc2e1ad8-49f7-469b-a94d-e3f544cd3d80','String','jsonType.label'),('fc2e1ad8-49f7-469b-a94d-e3f544cd3d80','lastName','user.attribute'),('fc2e1ad8-49f7-469b-a94d-e3f544cd3d80','true','userinfo.token.claim');
/*!40000 ALTER TABLE `PROTOCOL_MAPPER_CONFIG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REALM`
--

DROP TABLE IF EXISTS `REALM`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REALM` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ACCESS_CODE_LIFESPAN` int(11) DEFAULT NULL,
  `USER_ACTION_LIFESPAN` int(11) DEFAULT NULL,
  `ACCESS_TOKEN_LIFESPAN` int(11) DEFAULT NULL,
  `ACCOUNT_THEME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ADMIN_THEME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `EMAIL_THEME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `EVENTS_ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `EVENTS_EXPIRATION` bigint(20) DEFAULT NULL,
  `LOGIN_THEME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NOT_BEFORE` int(11) DEFAULT NULL,
  `PASSWORD_POLICY` varchar(2550) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REGISTRATION_ALLOWED` bit(1) NOT NULL DEFAULT b'0',
  `REMEMBER_ME` bit(1) NOT NULL DEFAULT b'0',
  `RESET_PASSWORD_ALLOWED` bit(1) NOT NULL DEFAULT b'0',
  `SOCIAL` bit(1) NOT NULL DEFAULT b'0',
  `SSL_REQUIRED` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `SSO_IDLE_TIMEOUT` int(11) DEFAULT NULL,
  `SSO_MAX_LIFESPAN` int(11) DEFAULT NULL,
  `UPDATE_PROFILE_ON_SOC_LOGIN` bit(1) NOT NULL DEFAULT b'0',
  `VERIFY_EMAIL` bit(1) NOT NULL DEFAULT b'0',
  `MASTER_ADMIN_CLIENT` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `LOGIN_LIFESPAN` int(11) DEFAULT NULL,
  `INTERNATIONALIZATION_ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `DEFAULT_LOCALE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REG_EMAIL_AS_USERNAME` bit(1) NOT NULL DEFAULT b'0',
  `ADMIN_EVENTS_ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `ADMIN_EVENTS_DETAILS_ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `EDIT_USERNAME_ALLOWED` bit(1) NOT NULL DEFAULT b'0',
  `OTP_POLICY_COUNTER` int(11) DEFAULT '0',
  `OTP_POLICY_WINDOW` int(11) DEFAULT '1',
  `OTP_POLICY_PERIOD` int(11) DEFAULT '30',
  `OTP_POLICY_DIGITS` int(11) DEFAULT '6',
  `OTP_POLICY_ALG` varchar(36) COLLATE utf8_unicode_ci DEFAULT 'HmacSHA1',
  `OTP_POLICY_TYPE` varchar(36) COLLATE utf8_unicode_ci DEFAULT 'totp',
  `BROWSER_FLOW` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REGISTRATION_FLOW` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `DIRECT_GRANT_FLOW` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `RESET_CREDENTIALS_FLOW` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `CLIENT_AUTH_FLOW` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `OFFLINE_SESSION_IDLE_TIMEOUT` int(11) DEFAULT '0',
  `REVOKE_REFRESH_TOKEN` bit(1) NOT NULL DEFAULT b'0',
  `ACCESS_TOKEN_LIFE_IMPLICIT` int(11) DEFAULT '0',
  `LOGIN_WITH_EMAIL_ALLOWED` bit(1) NOT NULL DEFAULT b'1',
  `DUPLICATE_EMAILS_ALLOWED` bit(1) NOT NULL DEFAULT b'0',
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_ORVSDMLA56612EAEFIQ6WL5OI` (`NAME`),
  KEY `FK_TRAF444KK6QRKMS7N56AIWQ5Y` (`MASTER_ADMIN_CLIENT`),
  CONSTRAINT `FK_TRAF444KK6QRKMS7N56AIWQ5Y` FOREIGN KEY (`MASTER_ADMIN_CLIENT`) REFERENCES `CLIENT` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REALM`
--

LOCK TABLES `REALM` WRITE;
/*!40000 ALTER TABLE `REALM` DISABLE KEYS */;
INSERT INTO `REALM` VALUES ('master',60,300,60,NULL,NULL,NULL,'','\0',0,NULL,'master',0,'hashIterations(20000)','\0','\0','\0','\0','EXTERNAL',1800,36000,'\0','\0','354f7145-851b-4a84-8d27-cb6622d4eb8a',1800,'\0',NULL,'\0','\0','\0','\0',0,1,30,6,'HmacSHA1','totp','9a9a6d2f-e6ef-4445-84cb-d712aeca2ebb','f1ae4295-3036-4d28-a1ce-d377afa73cdb','62cce624-2e2e-458b-a4f5-5bec4ccd747b','c3aa4d33-c17b-4609-a07a-4d56b98b0f92','61cc90b3-b42d-422e-a116-0f584fc76df3',2592000,'\0',900,'','\0'),('shanoir-ng',60,300,300,NULL,NULL,NULL,'','\0',0,'shanoir-theme','shanoir-ng',0,'hashIterations(20000)','\0','\0','','\0','EXTERNAL',1800,36000,'\0','\0','93635e6b-5f0f-4a37-b304-4337459122aa',1800,'\0',NULL,'\0','\0','\0','\0',0,1,30,6,'HmacSHA1','totp','e622576b-2e09-42be-bf8c-71ff925e3f6e','dbeeb75d-afb2-46e8-8260-f1daae2b3a7a','0e20ce2d-379e-4b57-bedd-43075a4b4327','cf00062f-34c3-416e-9cd4-4abe6c77f029','408934cc-53ff-47e0-b438-046fd293e13f',2592000,'\0',900,'','\0');
/*!40000 ALTER TABLE `REALM` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REALM_ATTRIBUTE`
--

DROP TABLE IF EXISTS `REALM_ATTRIBUTE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REALM_ATTRIBUTE` (
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`NAME`,`REALM_ID`),
  KEY `FK_8SHXD6L3E9ATQUKACXGPFFPTW` (`REALM_ID`),
  CONSTRAINT `FK_8SHXD6L3E9ATQUKACXGPFFPTW` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REALM_ATTRIBUTE`
--

LOCK TABLES `REALM_ATTRIBUTE` WRITE;
/*!40000 ALTER TABLE `REALM_ATTRIBUTE` DISABLE KEYS */;
INSERT INTO `REALM_ATTRIBUTE` VALUES ('_browser_header.contentSecurityPolicy','frame-src \'self\'','master'),('_browser_header.contentSecurityPolicy','frame-src \'self\'','shanoir-ng'),('_browser_header.xContentTypeOptions','nosniff','master'),('_browser_header.xContentTypeOptions','nosniff','shanoir-ng'),('_browser_header.xFrameOptions','SAMEORIGIN','master'),('_browser_header.xFrameOptions','SAMEORIGIN','shanoir-ng'),('bruteForceProtected','false','master'),('bruteForceProtected','false','shanoir-ng'),('displayName','Keycloak','master'),('displayNameHtml','<div class=\"kc-logo-text\"><span>Keycloak</span></div>','master'),('failureFactor','30','master'),('failureFactor','30','shanoir-ng'),('maxDeltaTimeSeconds','43200','master'),('maxDeltaTimeSeconds','43200','shanoir-ng'),('maxFailureWaitSeconds','900','master'),('maxFailureWaitSeconds','900','shanoir-ng'),('minimumQuickLoginWaitSeconds','60','master'),('minimumQuickLoginWaitSeconds','60','shanoir-ng'),('quickLoginCheckMilliSeconds','1000','master'),('quickLoginCheckMilliSeconds','1000','shanoir-ng'),('waitIncrementSeconds','60','master'),('waitIncrementSeconds','60','shanoir-ng');
/*!40000 ALTER TABLE `REALM_ATTRIBUTE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REALM_DEFAULT_GROUPS`
--

DROP TABLE IF EXISTS `REALM_DEFAULT_GROUPS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REALM_DEFAULT_GROUPS` (
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `GROUP_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  UNIQUE KEY `CON_GROUP_ID_DEF_GROUPS` (`GROUP_ID`),
  KEY `FK_DEF_GROUPS_REALM` (`REALM_ID`),
  CONSTRAINT `FK_DEF_GROUPS_GROUP` FOREIGN KEY (`GROUP_ID`) REFERENCES `KEYCLOAK_GROUP` (`ID`),
  CONSTRAINT `FK_DEF_GROUPS_REALM` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REALM_DEFAULT_GROUPS`
--

LOCK TABLES `REALM_DEFAULT_GROUPS` WRITE;
/*!40000 ALTER TABLE `REALM_DEFAULT_GROUPS` DISABLE KEYS */;
/*!40000 ALTER TABLE `REALM_DEFAULT_GROUPS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REALM_DEFAULT_ROLES`
--

DROP TABLE IF EXISTS `REALM_DEFAULT_ROLES`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REALM_DEFAULT_ROLES` (
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ROLE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  UNIQUE KEY `UK_H4WPD7W4HSOOLNI3H0SW7BTJE` (`ROLE_ID`),
  KEY `FK_EVUDB1PPW84OXFAX2DRS03ICC` (`REALM_ID`),
  CONSTRAINT `FK_EVUDB1PPW84OXFAX2DRS03ICC` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`),
  CONSTRAINT `FK_H4WPD7W4HSOOLNI3H0SW7BTJE` FOREIGN KEY (`ROLE_ID`) REFERENCES `KEYCLOAK_ROLE` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REALM_DEFAULT_ROLES`
--

LOCK TABLES `REALM_DEFAULT_ROLES` WRITE;
/*!40000 ALTER TABLE `REALM_DEFAULT_ROLES` DISABLE KEYS */;
INSERT INTO `REALM_DEFAULT_ROLES` VALUES ('master','04d19404-ccdd-46b9-85e2-abf3242f41ac'),('master','da56af07-3865-4e5b-b661-07ecc3db7413'),('shanoir-ng','d83f8de8-036b-4b41-940a-85202fe6a860'),('shanoir-ng','dd741a05-d506-4842-aa4c-7311c25b844d');
/*!40000 ALTER TABLE `REALM_DEFAULT_ROLES` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REALM_ENABLED_EVENT_TYPES`
--

DROP TABLE IF EXISTS `REALM_ENABLED_EVENT_TYPES`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REALM_ENABLED_EVENT_TYPES` (
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  KEY `FK_H846O4H0W8EPX5NWEDRF5Y69J` (`REALM_ID`),
  CONSTRAINT `FK_H846O4H0W8EPX5NWEDRF5Y69J` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REALM_ENABLED_EVENT_TYPES`
--

LOCK TABLES `REALM_ENABLED_EVENT_TYPES` WRITE;
/*!40000 ALTER TABLE `REALM_ENABLED_EVENT_TYPES` DISABLE KEYS */;
/*!40000 ALTER TABLE `REALM_ENABLED_EVENT_TYPES` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REALM_EVENTS_LISTENERS`
--

DROP TABLE IF EXISTS `REALM_EVENTS_LISTENERS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REALM_EVENTS_LISTENERS` (
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  KEY `FK_H846O4H0W8EPX5NXEV9F5Y69J` (`REALM_ID`),
  CONSTRAINT `FK_H846O4H0W8EPX5NXEV9F5Y69J` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REALM_EVENTS_LISTENERS`
--

LOCK TABLES `REALM_EVENTS_LISTENERS` WRITE;
/*!40000 ALTER TABLE `REALM_EVENTS_LISTENERS` DISABLE KEYS */;
INSERT INTO `REALM_EVENTS_LISTENERS` VALUES ('master','jboss-logging'),('shanoir-ng','jboss-logging');
/*!40000 ALTER TABLE `REALM_EVENTS_LISTENERS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REALM_REQUIRED_CREDENTIAL`
--

DROP TABLE IF EXISTS `REALM_REQUIRED_CREDENTIAL`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REALM_REQUIRED_CREDENTIAL` (
  `TYPE` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `FORM_LABEL` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `INPUT` bit(1) NOT NULL DEFAULT b'0',
  `SECRET` bit(1) NOT NULL DEFAULT b'0',
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`REALM_ID`,`TYPE`),
  CONSTRAINT `FK_5HG65LYBEVAVKQFKI3KPONH9V` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REALM_REQUIRED_CREDENTIAL`
--

LOCK TABLES `REALM_REQUIRED_CREDENTIAL` WRITE;
/*!40000 ALTER TABLE `REALM_REQUIRED_CREDENTIAL` DISABLE KEYS */;
INSERT INTO `REALM_REQUIRED_CREDENTIAL` VALUES ('password','password','','','master'),('password','password','','','shanoir-ng');
/*!40000 ALTER TABLE `REALM_REQUIRED_CREDENTIAL` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REALM_SMTP_CONFIG`
--

DROP TABLE IF EXISTS `REALM_SMTP_CONFIG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REALM_SMTP_CONFIG` (
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`REALM_ID`,`NAME`),
  CONSTRAINT `FK_70EJ8XDXGXD0B9HH6180IRR0O` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REALM_SMTP_CONFIG`
--

LOCK TABLES `REALM_SMTP_CONFIG` WRITE;
/*!40000 ALTER TABLE `REALM_SMTP_CONFIG` DISABLE KEYS */;
/*!40000 ALTER TABLE `REALM_SMTP_CONFIG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REALM_SUPPORTED_LOCALES`
--

DROP TABLE IF EXISTS `REALM_SUPPORTED_LOCALES`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REALM_SUPPORTED_LOCALES` (
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  KEY `FK_SUPPORTED_LOCALES_REALM` (`REALM_ID`),
  CONSTRAINT `FK_SUPPORTED_LOCALES_REALM` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REALM_SUPPORTED_LOCALES`
--

LOCK TABLES `REALM_SUPPORTED_LOCALES` WRITE;
/*!40000 ALTER TABLE `REALM_SUPPORTED_LOCALES` DISABLE KEYS */;
/*!40000 ALTER TABLE `REALM_SUPPORTED_LOCALES` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REDIRECT_URIS`
--

DROP TABLE IF EXISTS `REDIRECT_URIS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REDIRECT_URIS` (
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  KEY `FK_1BURS8PB4OUJ97H5WUPPAHV9F` (`CLIENT_ID`),
  CONSTRAINT `FK_1BURS8PB4OUJ97H5WUPPAHV9F` FOREIGN KEY (`CLIENT_ID`) REFERENCES `CLIENT` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REDIRECT_URIS`
--

LOCK TABLES `REDIRECT_URIS` WRITE;
/*!40000 ALTER TABLE `REDIRECT_URIS` DISABLE KEYS */;
INSERT INTO `REDIRECT_URIS` VALUES ('63cee678-229d-4715-99b7-b319c4cec32d','/auth/realms/master/account/*'),('04236224-4e7a-42d0-ba57-821769bd0a6a','/auth/admin/master/console/*'),('84284718-4123-4bee-a906-3ecd66bea3d1','/auth/realms/shanoir-ng/account/*'),('676d9349-500c-4e73-8106-1589f2ac812a','/auth/admin/shanoir-ng/console/*'),('de02f346-7bb9-4a9e-9871-722fe1381930','http://localhost:8081/*'),('de02f346-7bb9-4a9e-9871-722fe1381930','http://localhost/*'),('1baebc2e-3c0b-4a04-a067-39dac04320aa','http://localhost:9901/*'),('1baebc2e-3c0b-4a04-a067-39dac04320aa','http://localhost:9900/*');
/*!40000 ALTER TABLE `REDIRECT_URIS` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REQUIRED_ACTION_CONFIG`
--

DROP TABLE IF EXISTS `REQUIRED_ACTION_CONFIG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REQUIRED_ACTION_CONFIG` (
  `REQUIRED_ACTION_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` longtext COLLATE utf8_unicode_ci,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`REQUIRED_ACTION_ID`,`NAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REQUIRED_ACTION_CONFIG`
--

LOCK TABLES `REQUIRED_ACTION_CONFIG` WRITE;
/*!40000 ALTER TABLE `REQUIRED_ACTION_CONFIG` DISABLE KEYS */;
/*!40000 ALTER TABLE `REQUIRED_ACTION_CONFIG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `REQUIRED_ACTION_PROVIDER`
--

DROP TABLE IF EXISTS `REQUIRED_ACTION_PROVIDER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `REQUIRED_ACTION_PROVIDER` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ALIAS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `DEFAULT_ACTION` bit(1) NOT NULL DEFAULT b'0',
  `PROVIDER_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_REQ_ACT_REALM` (`REALM_ID`),
  CONSTRAINT `FK_REQ_ACT_REALM` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `REQUIRED_ACTION_PROVIDER`
--

LOCK TABLES `REQUIRED_ACTION_PROVIDER` WRITE;
/*!40000 ALTER TABLE `REQUIRED_ACTION_PROVIDER` DISABLE KEYS */;
INSERT INTO `REQUIRED_ACTION_PROVIDER` VALUES ('18d0acb3-f308-415a-b7b7-d18afdbde750','UPDATE_PROFILE','Update Profile','master','','\0','UPDATE_PROFILE'),('3d32ed76-3d51-434a-b367-1e4c0ce43324','CONFIGURE_TOTP','Configure OTP','master','','\0','CONFIGURE_TOTP'),('3e9a82f2-1eb0-4dab-adb0-b0735854801a','UPDATE_PROFILE','Update Profile','shanoir-ng','','\0','UPDATE_PROFILE'),('470608e8-4cec-4df5-af47-4689fc1fe3f0','UPDATE_PASSWORD','Update Password','master','','\0','UPDATE_PASSWORD'),('731ae06b-0ed6-4346-b653-3e9c3e54658d','VERIFY_EMAIL','Verify Email','master','','\0','VERIFY_EMAIL'),('8848a8fa-6ac9-41f2-b969-807c45b86771','CONFIGURE_TOTP','Configure OTP','shanoir-ng','','\0','CONFIGURE_TOTP'),('8bed46a5-d24a-45ea-adfa-aa3bb707df94','terms_and_conditions','Terms and Conditions','master','\0','\0','terms_and_conditions'),('a92a47ae-1160-44d2-98af-6e5baa14f1a9','UPDATE_PASSWORD','Update Password','shanoir-ng','','\0','UPDATE_PASSWORD'),('a9b05bc2-39f7-4f38-bbb9-6c5f589bd2d4','record-login-date-action','Record Login Date Action','shanoir-ng','','','record-login-date-action'),('e635b7f8-68a3-461b-bc44-0247382d6c35','VERIFY_EMAIL','Verify Email','shanoir-ng','','\0','VERIFY_EMAIL'),('f4aac2b7-b5e9-4b00-a66d-1623e93ff325','terms_and_conditions','Terms and Conditions','shanoir-ng','\0','\0','terms_and_conditions');
/*!40000 ALTER TABLE `REQUIRED_ACTION_PROVIDER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RESOURCE_POLICY`
--

DROP TABLE IF EXISTS `RESOURCE_POLICY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RESOURCE_POLICY` (
  `RESOURCE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `POLICY_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`RESOURCE_ID`,`POLICY_ID`),
  KEY `FK_FRSRPP213XCX4WNKOG82SSRFY` (`POLICY_ID`),
  CONSTRAINT `FK_FRSRPOS53XCX4WNKOG82SSRFY` FOREIGN KEY (`RESOURCE_ID`) REFERENCES `RESOURCE_SERVER_RESOURCE` (`ID`),
  CONSTRAINT `FK_FRSRPP213XCX4WNKOG82SSRFY` FOREIGN KEY (`POLICY_ID`) REFERENCES `RESOURCE_SERVER_POLICY` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RESOURCE_POLICY`
--

LOCK TABLES `RESOURCE_POLICY` WRITE;
/*!40000 ALTER TABLE `RESOURCE_POLICY` DISABLE KEYS */;
/*!40000 ALTER TABLE `RESOURCE_POLICY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RESOURCE_SCOPE`
--

DROP TABLE IF EXISTS `RESOURCE_SCOPE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RESOURCE_SCOPE` (
  `RESOURCE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `SCOPE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`RESOURCE_ID`,`SCOPE_ID`),
  KEY `FK_FRSRPS213XCX4WNKOG82SSRFY` (`SCOPE_ID`),
  CONSTRAINT `FK_FRSRPOS13XCX4WNKOG82SSRFY` FOREIGN KEY (`RESOURCE_ID`) REFERENCES `RESOURCE_SERVER_RESOURCE` (`ID`),
  CONSTRAINT `FK_FRSRPS213XCX4WNKOG82SSRFY` FOREIGN KEY (`SCOPE_ID`) REFERENCES `RESOURCE_SERVER_SCOPE` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RESOURCE_SCOPE`
--

LOCK TABLES `RESOURCE_SCOPE` WRITE;
/*!40000 ALTER TABLE `RESOURCE_SCOPE` DISABLE KEYS */;
/*!40000 ALTER TABLE `RESOURCE_SCOPE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RESOURCE_SERVER`
--

DROP TABLE IF EXISTS `RESOURCE_SERVER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RESOURCE_SERVER` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ALLOW_RS_REMOTE_MGMT` bit(1) NOT NULL DEFAULT b'0',
  `POLICY_ENFORCE_MODE` varchar(15) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_AU8TT6T700S9V50BU18WS5HA6` (`CLIENT_ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RESOURCE_SERVER`
--

LOCK TABLES `RESOURCE_SERVER` WRITE;
/*!40000 ALTER TABLE `RESOURCE_SERVER` DISABLE KEYS */;
INSERT INTO `RESOURCE_SERVER` VALUES ('ddcf25ad-5f44-4c92-a06e-ec466ca7bb81','1baebc2e-3c0b-4a04-a067-39dac04320aa','\0','0');
/*!40000 ALTER TABLE `RESOURCE_SERVER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RESOURCE_SERVER_POLICY`
--

DROP TABLE IF EXISTS `RESOURCE_SERVER_POLICY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RESOURCE_SERVER_POLICY` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `DESCRIPTION` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `TYPE` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `DECISION_STRATEGY` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `LOGIC` varchar(20) COLLATE utf8_unicode_ci DEFAULT NULL,
  `RESOURCE_SERVER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_FRSRPT700S9V50BU18WS5HA6` (`NAME`,`RESOURCE_SERVER_ID`),
  KEY `FK_FRSRPO213XCX4WNKOG82SSRFY` (`RESOURCE_SERVER_ID`),
  CONSTRAINT `FK_FRSRPO213XCX4WNKOG82SSRFY` FOREIGN KEY (`RESOURCE_SERVER_ID`) REFERENCES `RESOURCE_SERVER` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RESOURCE_SERVER_POLICY`
--

LOCK TABLES `RESOURCE_SERVER_POLICY` WRITE;
/*!40000 ALTER TABLE `RESOURCE_SERVER_POLICY` DISABLE KEYS */;
INSERT INTO `RESOURCE_SERVER_POLICY` VALUES ('13d91fcc-ccb0-4a1f-8a47-6de7d53198de','Default Permission','A permission that applies to the default resource type','resource','1','0','ddcf25ad-5f44-4c92-a06e-ec466ca7bb81'),('c9e01425-988f-4385-ace5-f8591f4b3199','Default Policy','A policy that grants access only for users within this realm','js','0','0','ddcf25ad-5f44-4c92-a06e-ec466ca7bb81');
/*!40000 ALTER TABLE `RESOURCE_SERVER_POLICY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RESOURCE_SERVER_RESOURCE`
--

DROP TABLE IF EXISTS `RESOURCE_SERVER_RESOURCE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RESOURCE_SERVER_RESOURCE` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `URI` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `TYPE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `ICON_URI` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `OWNER` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `RESOURCE_SERVER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_FRSR6T700S9V50BU18WS5HA6` (`NAME`,`OWNER`,`RESOURCE_SERVER_ID`),
  KEY `FK_FRSRHO213XCX4WNKOG82SSRFY` (`RESOURCE_SERVER_ID`),
  CONSTRAINT `FK_FRSRHO213XCX4WNKOG82SSRFY` FOREIGN KEY (`RESOURCE_SERVER_ID`) REFERENCES `RESOURCE_SERVER` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RESOURCE_SERVER_RESOURCE`
--

LOCK TABLES `RESOURCE_SERVER_RESOURCE` WRITE;
/*!40000 ALTER TABLE `RESOURCE_SERVER_RESOURCE` DISABLE KEYS */;
INSERT INTO `RESOURCE_SERVER_RESOURCE` VALUES ('104d7b0b-80cd-47d5-9354-b5e736029e9a','Default Resource','/*','urn:shanoir-ng-users:resources:default',NULL,'1baebc2e-3c0b-4a04-a067-39dac04320aa','ddcf25ad-5f44-4c92-a06e-ec466ca7bb81');
/*!40000 ALTER TABLE `RESOURCE_SERVER_RESOURCE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `RESOURCE_SERVER_SCOPE`
--

DROP TABLE IF EXISTS `RESOURCE_SERVER_SCOPE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `RESOURCE_SERVER_SCOPE` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `ICON_URI` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `RESOURCE_SERVER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_FRSRST700S9V50BU18WS5HA6` (`NAME`,`RESOURCE_SERVER_ID`),
  KEY `FK_FRSRSO213XCX4WNKOG82SSRFY` (`RESOURCE_SERVER_ID`),
  CONSTRAINT `FK_FRSRSO213XCX4WNKOG82SSRFY` FOREIGN KEY (`RESOURCE_SERVER_ID`) REFERENCES `RESOURCE_SERVER` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `RESOURCE_SERVER_SCOPE`
--

LOCK TABLES `RESOURCE_SERVER_SCOPE` WRITE;
/*!40000 ALTER TABLE `RESOURCE_SERVER_SCOPE` DISABLE KEYS */;
/*!40000 ALTER TABLE `RESOURCE_SERVER_SCOPE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SCOPE_MAPPING`
--

DROP TABLE IF EXISTS `SCOPE_MAPPING`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SCOPE_MAPPING` (
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ROLE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`CLIENT_ID`,`ROLE_ID`),
  KEY `FK_P3RH9GRKU11KQFRS4FLTT7RNQ` (`ROLE_ID`),
  CONSTRAINT `FK_OUSE064PLMLR732LXJCN1Q5F1` FOREIGN KEY (`CLIENT_ID`) REFERENCES `CLIENT` (`ID`),
  CONSTRAINT `FK_P3RH9GRKU11KQFRS4FLTT7RNQ` FOREIGN KEY (`ROLE_ID`) REFERENCES `KEYCLOAK_ROLE` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SCOPE_MAPPING`
--

LOCK TABLES `SCOPE_MAPPING` WRITE;
/*!40000 ALTER TABLE `SCOPE_MAPPING` DISABLE KEYS */;
INSERT INTO `SCOPE_MAPPING` VALUES ('04236224-4e7a-42d0-ba57-821769bd0a6a','699b0dd7-6f4b-40fb-bf41-f4030ff1c343'),('350a782b-05c8-4529-9314-2b5d31ef4918','699b0dd7-6f4b-40fb-bf41-f4030ff1c343'),('676d9349-500c-4e73-8106-1589f2ac812a','e1979e00-5057-4dc4-beaf-5bf19532b8cc'),('d015be82-be75-46cd-8f39-d9d6c303adff','e1979e00-5057-4dc4-beaf-5bf19532b8cc');
/*!40000 ALTER TABLE `SCOPE_MAPPING` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `SCOPE_POLICY`
--

DROP TABLE IF EXISTS `SCOPE_POLICY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `SCOPE_POLICY` (
  `SCOPE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `POLICY_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`SCOPE_ID`,`POLICY_ID`),
  KEY `FK_FRSRASP13XCX4WNKOG82SSRFY` (`POLICY_ID`),
  CONSTRAINT `FK_FRSRASP13XCX4WNKOG82SSRFY` FOREIGN KEY (`POLICY_ID`) REFERENCES `RESOURCE_SERVER_POLICY` (`ID`),
  CONSTRAINT `FK_FRSRPASS3XCX4WNKOG82SSRFY` FOREIGN KEY (`SCOPE_ID`) REFERENCES `RESOURCE_SERVER_SCOPE` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `SCOPE_POLICY`
--

LOCK TABLES `SCOPE_POLICY` WRITE;
/*!40000 ALTER TABLE `SCOPE_POLICY` DISABLE KEYS */;
/*!40000 ALTER TABLE `SCOPE_POLICY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `TEMPLATE_SCOPE_MAPPING`
--

DROP TABLE IF EXISTS `TEMPLATE_SCOPE_MAPPING`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `TEMPLATE_SCOPE_MAPPING` (
  `TEMPLATE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ROLE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`TEMPLATE_ID`,`ROLE_ID`),
  KEY `FK_TEMPL_SCOPE_ROLE` (`ROLE_ID`),
  CONSTRAINT `FK_TEMPL_SCOPE_ROLE` FOREIGN KEY (`ROLE_ID`) REFERENCES `KEYCLOAK_ROLE` (`ID`),
  CONSTRAINT `FK_TEMPL_SCOPE_TEMPL` FOREIGN KEY (`TEMPLATE_ID`) REFERENCES `CLIENT_TEMPLATE` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `TEMPLATE_SCOPE_MAPPING`
--

LOCK TABLES `TEMPLATE_SCOPE_MAPPING` WRITE;
/*!40000 ALTER TABLE `TEMPLATE_SCOPE_MAPPING` DISABLE KEYS */;
/*!40000 ALTER TABLE `TEMPLATE_SCOPE_MAPPING` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_ATTRIBUTE`
--

DROP TABLE IF EXISTS `USER_ATTRIBUTE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_ATTRIBUTE` (
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `USER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL DEFAULT 'sybase-needs-something-here',
  PRIMARY KEY (`ID`),
  KEY `IDX_USER_ATTRIBUTE` (`USER_ID`),
  CONSTRAINT `FK_5HRM2VLF9QL5FU043KQEPOVBR` FOREIGN KEY (`USER_ID`) REFERENCES `USER_ENTITY` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_ATTRIBUTE`
--

LOCK TABLES `USER_ATTRIBUTE` WRITE;
/*!40000 ALTER TABLE `USER_ATTRIBUTE` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER_ATTRIBUTE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_CONSENT`
--

DROP TABLE IF EXISTS `USER_CONSENT`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_CONSENT` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `USER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CREATED_DATE` bigint(20) DEFAULT NULL,
  `LAST_UPDATED_DATE` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_JKUWUVD56ONTGSUHOGM8UEWRT` (`CLIENT_ID`,`USER_ID`),
  KEY `IDX_USER_CONSENT` (`USER_ID`),
  CONSTRAINT `FK_GRNTCSNT_USER` FOREIGN KEY (`USER_ID`) REFERENCES `USER_ENTITY` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_CONSENT`
--

LOCK TABLES `USER_CONSENT` WRITE;
/*!40000 ALTER TABLE `USER_CONSENT` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER_CONSENT` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_CONSENT_PROT_MAPPER`
--

DROP TABLE IF EXISTS `USER_CONSENT_PROT_MAPPER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_CONSENT_PROT_MAPPER` (
  `USER_CONSENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `PROTOCOL_MAPPER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`USER_CONSENT_ID`,`PROTOCOL_MAPPER_ID`),
  KEY `IDX_CONSENT_PROTMAPPER` (`USER_CONSENT_ID`),
  CONSTRAINT `FK_GRNTCSNT_PRM_GR` FOREIGN KEY (`USER_CONSENT_ID`) REFERENCES `USER_CONSENT` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_CONSENT_PROT_MAPPER`
--

LOCK TABLES `USER_CONSENT_PROT_MAPPER` WRITE;
/*!40000 ALTER TABLE `USER_CONSENT_PROT_MAPPER` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER_CONSENT_PROT_MAPPER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_CONSENT_ROLE`
--

DROP TABLE IF EXISTS `USER_CONSENT_ROLE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_CONSENT_ROLE` (
  `USER_CONSENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `ROLE_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`USER_CONSENT_ID`,`ROLE_ID`),
  KEY `IDX_CONSENT_ROLE` (`USER_CONSENT_ID`),
  CONSTRAINT `FK_GRNTCSNT_ROLE_GR` FOREIGN KEY (`USER_CONSENT_ID`) REFERENCES `USER_CONSENT` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_CONSENT_ROLE`
--

LOCK TABLES `USER_CONSENT_ROLE` WRITE;
/*!40000 ALTER TABLE `USER_CONSENT_ROLE` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER_CONSENT_ROLE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_ENTITY`
--

DROP TABLE IF EXISTS `USER_ENTITY`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_ENTITY` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `EMAIL` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `EMAIL_CONSTRAINT` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `EMAIL_VERIFIED` bit(1) NOT NULL DEFAULT b'0',
  `ENABLED` bit(1) NOT NULL DEFAULT b'0',
  `FEDERATION_LINK` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `FIRST_NAME` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `LAST_NAME` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `REALM_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `USERNAME` varchar(255) CHARACTER SET utf8 DEFAULT NULL,
  `CREATED_TIMESTAMP` bigint(20) DEFAULT NULL,
  `SERVICE_ACCOUNT_CLIENT_LINK` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  UNIQUE KEY `UK_DYKN684SL8UP1CRFEI6ECKHD7` (`REALM_ID`,`EMAIL_CONSTRAINT`),
  UNIQUE KEY `UK_RU8TT6T700S9V50BU18WS5HA6` (`REALM_ID`,`USERNAME`),
  KEY `IDX_USER_EMAIL` (`EMAIL`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_ENTITY`
--

LOCK TABLES `USER_ENTITY` WRITE;
/*!40000 ALTER TABLE `USER_ENTITY` DISABLE KEYS */;
INSERT INTO `USER_ENTITY` VALUES ('82840b5a-f7c7-4a69-b688-d64f129b746a','service-account-shanoir-ng-users@placeholder.org','service-account-shanoir-ng-users@placeholder.org','\0','',NULL,NULL,NULL,'shanoir-ng','service-account-shanoir-ng-users',1493135610383,'1baebc2e-3c0b-4a04-a067-39dac04320aa'),('a8a3601d-83bf-4f71-ac25-48c1a4cd06c0',NULL,'1e1c10c0-237b-4c9a-9d26-5c84d3f39d32','\0','',NULL,NULL,NULL,'master','admin',1493135540695,NULL);
/*!40000 ALTER TABLE `USER_ENTITY` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_FEDERATION_CONFIG`
--

DROP TABLE IF EXISTS `USER_FEDERATION_CONFIG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_FEDERATION_CONFIG` (
  `USER_FEDERATION_PROVIDER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`USER_FEDERATION_PROVIDER_ID`,`NAME`),
  CONSTRAINT `FK_T13HPU1J94R2EBPEKR39X5EU5` FOREIGN KEY (`USER_FEDERATION_PROVIDER_ID`) REFERENCES `USER_FEDERATION_PROVIDER` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_FEDERATION_CONFIG`
--

LOCK TABLES `USER_FEDERATION_CONFIG` WRITE;
/*!40000 ALTER TABLE `USER_FEDERATION_CONFIG` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER_FEDERATION_CONFIG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_FEDERATION_MAPPER`
--

DROP TABLE IF EXISTS `USER_FEDERATION_MAPPER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_FEDERATION_MAPPER` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `FEDERATION_PROVIDER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `FEDERATION_MAPPER_TYPE` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_FEDMAPPERPM_REALM` (`REALM_ID`),
  KEY `FK_FEDMAPPERPM_FEDPRV` (`FEDERATION_PROVIDER_ID`),
  CONSTRAINT `FK_FEDMAPPERPM_FEDPRV` FOREIGN KEY (`FEDERATION_PROVIDER_ID`) REFERENCES `USER_FEDERATION_PROVIDER` (`ID`),
  CONSTRAINT `FK_FEDMAPPERPM_REALM` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_FEDERATION_MAPPER`
--

LOCK TABLES `USER_FEDERATION_MAPPER` WRITE;
/*!40000 ALTER TABLE `USER_FEDERATION_MAPPER` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER_FEDERATION_MAPPER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_FEDERATION_MAPPER_CONFIG`
--

DROP TABLE IF EXISTS `USER_FEDERATION_MAPPER_CONFIG`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_FEDERATION_MAPPER_CONFIG` (
  `USER_FEDERATION_MAPPER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`USER_FEDERATION_MAPPER_ID`,`NAME`),
  CONSTRAINT `FK_FEDMAPPER_CFG` FOREIGN KEY (`USER_FEDERATION_MAPPER_ID`) REFERENCES `USER_FEDERATION_MAPPER` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_FEDERATION_MAPPER_CONFIG`
--

LOCK TABLES `USER_FEDERATION_MAPPER_CONFIG` WRITE;
/*!40000 ALTER TABLE `USER_FEDERATION_MAPPER_CONFIG` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER_FEDERATION_MAPPER_CONFIG` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_FEDERATION_PROVIDER`
--

DROP TABLE IF EXISTS `USER_FEDERATION_PROVIDER`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_FEDERATION_PROVIDER` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `CHANGED_SYNC_PERIOD` int(11) DEFAULT NULL,
  `DISPLAY_NAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `FULL_SYNC_PERIOD` int(11) DEFAULT NULL,
  `LAST_SYNC` int(11) DEFAULT NULL,
  `PRIORITY` int(11) DEFAULT NULL,
  `PROVIDER_NAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`),
  KEY `FK_1FJ32F6PTOLW2QY60CD8N01E8` (`REALM_ID`),
  CONSTRAINT `FK_1FJ32F6PTOLW2QY60CD8N01E8` FOREIGN KEY (`REALM_ID`) REFERENCES `REALM` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_FEDERATION_PROVIDER`
--

LOCK TABLES `USER_FEDERATION_PROVIDER` WRITE;
/*!40000 ALTER TABLE `USER_FEDERATION_PROVIDER` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER_FEDERATION_PROVIDER` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_GROUP_MEMBERSHIP`
--

DROP TABLE IF EXISTS `USER_GROUP_MEMBERSHIP`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_GROUP_MEMBERSHIP` (
  `GROUP_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `USER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`GROUP_ID`,`USER_ID`),
  KEY `IDX_USER_GROUP_MAPPING` (`USER_ID`),
  CONSTRAINT `FK_USER_GROUP_USER` FOREIGN KEY (`USER_ID`) REFERENCES `USER_ENTITY` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_GROUP_MEMBERSHIP`
--

LOCK TABLES `USER_GROUP_MEMBERSHIP` WRITE;
/*!40000 ALTER TABLE `USER_GROUP_MEMBERSHIP` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER_GROUP_MEMBERSHIP` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_REQUIRED_ACTION`
--

DROP TABLE IF EXISTS `USER_REQUIRED_ACTION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_REQUIRED_ACTION` (
  `USER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `REQUIRED_ACTION` varchar(255) COLLATE utf8_unicode_ci NOT NULL DEFAULT ' ',
  PRIMARY KEY (`REQUIRED_ACTION`,`USER_ID`),
  KEY `IDX_USER_REQACTIONS` (`USER_ID`),
  CONSTRAINT `FK_6QJ3W1JW9CVAFHE19BWSIUVMD` FOREIGN KEY (`USER_ID`) REFERENCES `USER_ENTITY` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_REQUIRED_ACTION`
--

LOCK TABLES `USER_REQUIRED_ACTION` WRITE;
/*!40000 ALTER TABLE `USER_REQUIRED_ACTION` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER_REQUIRED_ACTION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_ROLE_MAPPING`
--

DROP TABLE IF EXISTS `USER_ROLE_MAPPING`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_ROLE_MAPPING` (
  `ROLE_ID` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `USER_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  PRIMARY KEY (`ROLE_ID`,`USER_ID`),
  KEY `IDX_USER_ROLE_MAPPING` (`USER_ID`),
  CONSTRAINT `FK_C4FQV34P1MBYLLOXANG7B1Q3L` FOREIGN KEY (`USER_ID`) REFERENCES `USER_ENTITY` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_ROLE_MAPPING`
--

LOCK TABLES `USER_ROLE_MAPPING` WRITE;
/*!40000 ALTER TABLE `USER_ROLE_MAPPING` DISABLE KEYS */;
INSERT INTO `USER_ROLE_MAPPING` VALUES ('096032b5-1c7a-4b2f-a290-49b0709e82a1','82840b5a-f7c7-4a69-b688-d64f129b746a'),('0dd34304-14e1-47c5-9750-52fb0deef76d','82840b5a-f7c7-4a69-b688-d64f129b746a'),('6a764793-5b5d-4d7c-9ba2-56215cfad5ff','82840b5a-f7c7-4a69-b688-d64f129b746a'),('d83f8de8-036b-4b41-940a-85202fe6a860','82840b5a-f7c7-4a69-b688-d64f129b746a'),('dd741a05-d506-4842-aa4c-7311c25b844d','82840b5a-f7c7-4a69-b688-d64f129b746a'),('04d19404-ccdd-46b9-85e2-abf3242f41ac','a8a3601d-83bf-4f71-ac25-48c1a4cd06c0'),('0b9edc64-bb37-447f-960c-0fc2c3c18563','a8a3601d-83bf-4f71-ac25-48c1a4cd06c0'),('699b0dd7-6f4b-40fb-bf41-f4030ff1c343','a8a3601d-83bf-4f71-ac25-48c1a4cd06c0'),('6b80ad80-48e1-4a3c-b478-7f53c8329a55','a8a3601d-83bf-4f71-ac25-48c1a4cd06c0'),('da56af07-3865-4e5b-b661-07ecc3db7413','a8a3601d-83bf-4f71-ac25-48c1a4cd06c0');
/*!40000 ALTER TABLE `USER_ROLE_MAPPING` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_SESSION`
--

DROP TABLE IF EXISTS `USER_SESSION`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_SESSION` (
  `ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `AUTH_METHOD` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `IP_ADDRESS` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `LAST_SESSION_REFRESH` int(11) DEFAULT NULL,
  `LOGIN_USERNAME` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REALM_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `REMEMBER_ME` bit(1) NOT NULL DEFAULT b'0',
  `STARTED` int(11) DEFAULT NULL,
  `USER_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `USER_SESSION_STATE` int(11) DEFAULT NULL,
  `BROKER_SESSION_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `BROKER_USER_ID` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_SESSION`
--

LOCK TABLES `USER_SESSION` WRITE;
/*!40000 ALTER TABLE `USER_SESSION` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER_SESSION` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USER_SESSION_NOTE`
--

DROP TABLE IF EXISTS `USER_SESSION_NOTE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USER_SESSION_NOTE` (
  `USER_SESSION` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `NAME` varchar(255) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(2048) COLLATE utf8_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`USER_SESSION`,`NAME`),
  CONSTRAINT `FK5EDFB00FF51D3472` FOREIGN KEY (`USER_SESSION`) REFERENCES `USER_SESSION` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USER_SESSION_NOTE`
--

LOCK TABLES `USER_SESSION_NOTE` WRITE;
/*!40000 ALTER TABLE `USER_SESSION_NOTE` DISABLE KEYS */;
/*!40000 ALTER TABLE `USER_SESSION_NOTE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `USERNAME_LOGIN_FAILURE`
--

DROP TABLE IF EXISTS `USERNAME_LOGIN_FAILURE`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `USERNAME_LOGIN_FAILURE` (
  `REALM_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `USERNAME` varchar(255) CHARACTER SET utf8 NOT NULL,
  `FAILED_LOGIN_NOT_BEFORE` int(11) DEFAULT NULL,
  `LAST_FAILURE` bigint(20) DEFAULT NULL,
  `LAST_IP_FAILURE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  `NUM_FAILURES` int(11) DEFAULT NULL,
  PRIMARY KEY (`REALM_ID`,`USERNAME`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `USERNAME_LOGIN_FAILURE`
--

LOCK TABLES `USERNAME_LOGIN_FAILURE` WRITE;
/*!40000 ALTER TABLE `USERNAME_LOGIN_FAILURE` DISABLE KEYS */;
/*!40000 ALTER TABLE `USERNAME_LOGIN_FAILURE` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `WEB_ORIGINS`
--

DROP TABLE IF EXISTS `WEB_ORIGINS`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!40101 SET character_set_client = utf8 */;
CREATE TABLE `WEB_ORIGINS` (
  `CLIENT_ID` varchar(36) COLLATE utf8_unicode_ci NOT NULL,
  `VALUE` varchar(255) COLLATE utf8_unicode_ci DEFAULT NULL,
  KEY `FK_LOJPHO213XCX4WNKOG82SSRFY` (`CLIENT_ID`),
  CONSTRAINT `FK_LOJPHO213XCX4WNKOG82SSRFY` FOREIGN KEY (`CLIENT_ID`) REFERENCES `CLIENT` (`ID`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `WEB_ORIGINS`
--

LOCK TABLES `WEB_ORIGINS` WRITE;
/*!40000 ALTER TABLE `WEB_ORIGINS` DISABLE KEYS */;
INSERT INTO `WEB_ORIGINS` VALUES ('de02f346-7bb9-4a9e-9871-722fe1381930','http://localhost:8081'),('de02f346-7bb9-4a9e-9871-722fe1381930','http://localhost');
/*!40000 ALTER TABLE `WEB_ORIGINS` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2017-04-26  7:22:07
