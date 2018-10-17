About Shanoir-NG (next generation)

Shanoir-NG is based on a microservice architecture, that heavily uses Docker.
Each Docker container integrates one microservice, mostly based on Spring Boot.
Each microservice exposes a REST interface on using Swagger 2, as definition format.
The front-end/web interface is implemented on using "Angular 2" (now 5) technology.
Nginx and Keycloak (on using OpenID-Connect) are used to glue everything together.
Internally dcm4che(e) is used to handle all DICOM concerns and dcm4chee-arc as backup PACS.

Many thanks to all these giants: on their shoulders we are standing to develop Shanoir-NG!!!

Installation of Shanoir-NG

The installation of Shanoir-NG is based on two components:
    * BUILD (COMPILE): with Maven 3
    * DEPLOY: with docker-compose, version 3

--------------- BUILD (COMPILE) ---------------

* Install Maven 3 on your machine/on the server
* Get access to the GitHub repository and clone the shanoir-ng repository
* Execute the Maven build on the parent project with the following commands:
    * cd shanoir-ng-parent/
    * mvn install -DskipTests
        * the tests will have to be cleaned up soon
* The build creates all .jar and .js executable files and copies them
into the folder /docker-compose to be used from there by docker-compose

--------------- DEPLOY ------------------------


* Install docker and docker-compose:
    * https://docs.docker.com/install/
    * https://docs.docker.com/compose/install/
* If you are on your developer machine:
    * Configure your local /etc/hosts (for windows, C:/Windows/System32/drivers/etc/hosts) and add:
	* 127.0.0.1       shanoir-ng-nginx
    * For windows 7, increase your RAM and set the port redirection for the virtual box.
* If you are on a dedicated server (e.g. shanoir-ng.irisa.fr):

* Go to the root folder and execute "docker-compose up --build"
    * If your microservices (studies, users etc.) exit, check if the databases are created.
      If not, execute the scripts manually in the databases container:
	* docker-compose exec database sh
	* cd docker-entrypoint-initdb.d
	* ./1_create_databases.sh
	* ./2_add_users.sql
	* Exit the docker container and rebuild from your docker-compose file


* Access and use shanoir-ng: http://shanoir-ng-nginx
* Access to dcm4chee 5 arc-light: http://localhost:8081/dcm4chee-arc/ui2/
* Access to Keycloak admin interface: http://localhost:8080/auth/admin/

* This installation uses Docker named volumes, find more here to handle your local data:
https://docs.docker.com/storage/volumes/
