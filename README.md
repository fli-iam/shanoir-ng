# Shanoir NG - Import, manage and share neuroimaging data

Shanoir NG (Next Generation) is a software that helps neuroimaging analysis researchers, radiologists, and MRI operators to organize and share neuroimaging datasets. 

_Here we need to present the main functionnalities of Shanoir (import formats, data model, security and roles, ...)_

Shanoir NG is a complete technological remake of the first version of the Shanoir application, maintaining the key concepts of Shanoir, enhancing the functionalities and the user interface and providing a great flexibility for further improvements.

Shanoir NG is copyrighted by [Inria](https://www.inria.fr/) and is now open source under the GNU General Public License v3.0. See the LICENCE file at the root of this project. If you want to contribute, please see the following page : [Contribution Workflow](https://github.com/fli-iam/shanoir-ng/wiki/Contribution-Workflow).

GitHub is a tool for developers if you are seeking information at a user level view please visit http://shanoir.org.

# :warning: Disclaimers :warning:

* The latest version of Shanoir-NG is currently on the branch "develop", or on the branch "appning" for the preclinical solution,
that will soon be merged into the "develop" branch. The "develop" branch is operational, we wait until our go-live in April/May
2020 is finished before merging "develop" into the "master", to have a real stable production version 1.0 "master" afterwards.

* You can find the installation instruction for "develop" branch below

* Shanoir NG is still in the developement phase. While many functionalities work well, some are not developed yet and some might be unstable. Also It still misses production features like database backup.

* A few .env files in the docker-compose layer contain clear Keycloak password. Please choose your own password (check Keycloak password format policy first).

# About Shanoir NG's architecture

* Shanoir-NG is based on a microservice architecture, that heavily uses Docker.
* Each Docker container integrates one microservice, mostly based on Spring Boot.
* Each microservice exposes a REST interface on using Swagger 2, as definition format.
* The front-end/web interface is implemented on using "Angular 2" (now 5) technology.
* Nginx and Keycloak (on using OpenID-Connect) are used to glue everything together.
* Internally dcm4che3 is used to handle all DICOM concerns and dcm4chee 5 arc-light as backup PACS.
* Furthermore dcm2niix is used for the DICOM to NIfTI conversion and Papaya Viewer for DICOM/NIfTI web view.

Many thanks to all these giants, on their shoulders we are standing to develop Shanoir-NG !

# Installation of Shanoir NG

The installation of Shanoir NG has two steps :
* BUILD (COMPILE): with Maven 3
* DEPLOY: with docker-compose, version 3

## BUILD (COMPILE)
* Install Maven 3 on your machine/on the server
* Download or git clone the shanoir-ng code. The `master` branch should be the most stable while `develop` will contain the newests functionalities if you are interested in testing thoses.
* Execute the Maven build on the parent project with the following commands:
    * cd shanoir-ng-parent/
    * **mvn install -DskipTests**
        * the tests will have to be cleaned up soon
* The build creates all .jar and .js executable files and copies them
into the folder /docker-compose to be used from there by docker-compose

## DEPLOY
* Install docker and docker-compose:
    * https://docs.docker.com/install/
    * https://docs.docker.com/compose/install/
* If you are on your **developer/local machine**:
    * Configure your local **/etc/hosts** (for windows, C:/Windows/System32/drivers/etc/hosts) and add:
	* 127.0.0.1       shanoir-ng-nginx
    * For windows 7, increase your RAM and set the port redirection (8080 and 443) for the virtual box.
* If you are on a **dedicated server** (e.g. shanoir-ng.irisa.fr):
    * By default Shanoir-NG is installed with the host shanoir-ng-nginx and the scheme http (dev setup)
    * If you are on a dedicated server (e.g. shanoir-ng.irisa.fr) you will have to do manual adaptions (we tried to automate as much as possible in a given time and there is still a way to go, but here we are currently)
        1. Keycloak: Open **/docker-compose/keycloak/cfg/shanoir-ng-realm.json** and change **redirectUris** and **webOrigins**
	    2. Spring Boot: Open **/.env** and change the host and scheme of all three properties in the file
	    3. Docker Compose: Open **/docker-compose.yml** and change the **container_name** of Nginx to e.g. shanoir-ng.irisa.fr. This is necessary, that e.g. ms users and the Keycloak CLI client can access to Keycloak (resolve the host name)
	    4. Angular: Open **/shanoir-ng-front/config/webpack.config.js** and change **SHANOIR_NG_URL_SCHEME** and **SHANOIR_NG_URL_HOST**
    * **Attention:** you will have to re-compile your code after these changes with Maven!!!
* Just in case you have some old stuff of Shanoir-NG in your docker environment:
    * **docker system prune -a**
    * **docker volume prune**
    * **Attention:** this will clean your entire docker system!
* Go to the root folder (/shanoir-ng) and execute **docker-compose up --build**
    * **Attention:** the file .env in the root folder is used to set environment variables
and will not be found if you run docker-compose elsewhere; results in errors after
* Access to shanoir-ng: https://shanoir-ng-nginx

If you want to login, please configure a user in Keycloak :

## CONFIGURE

### Configure a user in Keycloak

* Access to Keycloak admin interface: http://localhost:8080/auth/admin/

By default, new user accounts have been created in Keycloak by ms users with temporary passwords.
Please access to Keycloak admin interface below to reset the password, when you want to login (Manage users - Edit your desired user - Credentials - Reset password and Temporary password: No). When a SMTP server has been configured properly, emails with a temporary password will have been sent to each user (not the case in dev environment).

Please use the flags **kcAdminClientUsername**, **kcAdminClientPassword**, **syncAllUsersToKeycloak**
in the file **/docker-compose/users/Dockerfile** of ms users, to configure in production environment
the behaviour of user account creation with Keycloak. E.g. if you want to start with an empty users
database in Keycloak in production, please set the flag **syncAllUsersToKeycloak** to false.

**Attention:** for security reasons please change in a production environment the Keycloak admin password
in the file **/docker-compose/keycloak/variables.env** and in the file **/docker-compose/users/Dockerfile**
adapt as well the credentials.

### Configure a mail server

Users and admin may receive mails from Shanoir when they request an account or when they have forgotten their password. To make those functions work, there are two places where you need to set your mail server config.

#### In keycloak

In keycloak, with Shanoir-ng realm selected, go to "Realm Settings", then tab "Email" and simply set your config.

#### In .env

Edit the .env file at the root of the Shanoir source directory. At the line 
```
spring.mail.host=SMTP_HOST
```
simply replace SMTP_HOST by your mail server url.
**Attention:** After that you will have to restart the application (docker-compose down && docker-compose up --build) 

### PACS dcm4chee

Access to the backup PACS dcm4chee 5 arc-light: http://localhost:8081/dcm4chee-arc/ui2/

### Local data

This installation uses Docker named volumes, find more here to handle your local data:
https://docs.docker.com/storage/volumes/
