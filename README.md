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
* docker-compose up —build
* Connect to keycloak admin interface:
    * http://localhost:8080/auth/admin/
* Adapt in dev profile in application.yml the credentials of all microservices,
that you want to connect with your local keycloak instance
* docker-compose down
* docker-compose up —build
