Installation on using docker-compose

* Install docker and docker-compose:
    * https://docs.docker.com/install/
    * https://docs.docker.com/compose/install/
* Install Maven 3 on your machine
* Get access to GitHub repository and clone the shanoir-ng repository
* Please change for the moment manually the following file:
    * shanoir-ng-keycloak-auth/src/main/resources/application.properties
        * ms.users.server.address=http://users:9901/login
* Execute Maven build on parent project
    * cd shanoir-ng-parent/
    * mvn install -DskipTests
        * the tests will have to be clean up later
    * The build creates all .jars and copies them into the /docker-compose structure to be
used from there for the docker-compose startup
* configure your local /etc/hosts and add:
    * 127.0.0.1       shanoir-ng-nginx
* docker-compose up --build
* If your microservices (studies, users etc.) exit, check if the databases are created. If not, execute the scripts manually in the databases container:
	* docker-compose exec database sh
	* cd docker-entrypoint-initdb.d
	* ./1_create_databases.sh
	* ./2_add_users.sql
	* Exit the docker and rebuild from your docker-compose file
* Connect to keycloak admin interface:
    * http://localhost:8080/auth/admin/
* Add users for the moment manually into keycloak on using the admin interface
(in a later version the users from ms users will be copied into keycloak)
* access and use shanoir-ng: http://shanoir-ng-nginx
* access to dcm4chee 5 arc-light: http://localhost:8081/dcm4chee-arc/ui2/

* this installation uses Docker named volumes, find more here to handle your local data:
https://docs.docker.com/storage/volumes/
