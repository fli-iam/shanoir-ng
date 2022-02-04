# Shanoir-NG - Import, manage and share neuroimaging data

Shanoir-NG (Next Generation) is a software that helps neuroimaging analysis researchers, radiologists, and MRI operators to organize and share neuroimaging datasets. 

GitHub is a tool for developers if you are seeking information at a user level view please visit http://shanoir.org.

Shanoir-NG is a complete technological remake of the first version of the Shanoir application, maintaining the key concepts of Shanoir, enhancing the functionalities and the user interface and providing a great flexibility for further improvements.

Shanoir-NG is copyrighted by [Inria](https://www.inria.fr/) and is now open source under the GNU General Public License v3.0. See the LICENCE file at the root of this project. If you want to contribute, please see the following page : [Contribution Workflow](https://github.com/fli-iam/shanoir-ng/wiki/Contribution-Workflow).


# :warning: Disclaimers :warning:

* The latest stable version of Shanoir-NG is on the branch "master".
* The latest dev version of Shanoir-NG is on "develop" (if mature -> merged into master)
* You can find the installation instructions for "master" branch below.

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

# Access to REST-API on using Swagger2

You can easily connect and investigate the REST-interface of Shanoir-NG using Swagger2.
Depending on your server domain just call (e.g. for Neurinfo server):

* MS Studies: https://shanoir.irisa.fr/shanoir-ng/studies/swagger-ui.html
* MS Import: https://shanoir.irisa.fr/shanoir-ng/import/swagger-ui.html
* MS Datasets: https://shanoir.irisa.fr/shanoir-ng/datasets/swagger-ui.html
* MS Preclinical: https://shanoir.irisa.fr/shanoir-ng/preclinical/swagger-ui.html

## Intialize Shanoir Downloader

Clone the shanoir-ng repository, the shanoir-downloader/ folder will be empty ; two commands must be run to get the code:

  - `git submodule init` to initialize your local configuration file
  - `git submodule update` to fetch all the data from shanoir and check out the appropriate commit listed in `shanoir_downloader`

Then the shanoir-downloader project can be simply managed as a normal git repo (as if it were a separated project) ; meaning that once your are in the shanoir-downloader/ folder, you can just `git pull` to get the latest changes, and commit some changes.

## DEPLOY
* Install docker and docker-compose:
    * https://docs.docker.com/install/
    * https://docs.docker.com/compose/install/
* Make sur docker has enough memory to run Shanoir (6Gb should be enough)
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

Please note, that the MS Users does for security reasons not publicly expose his REST-interface.

# Requirements

To build and deploy Shanoir, you will need:
* Java 8 (very soon 11, after Spring Boot 2 Migration)
* docker (https://docs.docker.com/install/)
* docker-compose 3 (https://docs.docker.com/compose/install/)
* maven 3
* at least 10GB of available RAM

# Installation of Shanoir NG

The installation of Shanoir NG happens in three steps :
* BUILD
* CONFIGURE 
* DEPLOY

The TL;DR section gives the minimal for bootstrapping a development environment.
The following sections give detailed informations about each step.

### TL;DR

The default docker-compose configuration is well-suited for a development
environment. Each microservice is hosted in a separate container and the
application data are stored in named volumes.

Before deploying, some configuration is required:

* add the following line in your **/etc/hosts** (so that
  https://shanoir-ng-nginx/ is reachable from your web browser):
  ```
  127.0.0.1  shanoir-ng-nginx viewer.shanoir-ng-nginx
  ```
* if docker is not running natively and thus you are using docker-machine
  (windows/macos users), you will need to tune the virtualbox machine:
    * increase the amount of allocated RAM
    * set up tcp port redirections (at least for 8080 and 443)

The **bootstrap.sh** script automates the build and the deployment of shanoir.

**WARNING: this script is destructive** (as it will wipe out the external
volumes configured in the docker-compose.yml). It is not recommended to use on a
production host.

To deploy shanoir from scratch on a development machine you can just launch the
following command and have a coffee.
```
./bootstrap.sh --clean
```

Once the bootstrap is complete, go on to the [FIRST RUN](#first-run) section to
create the initial users.

## BUILD

The build consists of two stages: build the microservices and build the docker
images.

In the source tree, each microservice is located in a separate `shanoir-ng-*/`
directory containing a maven project. `shanoir-ng-parent` is a meta-project that
includes all the other projects. The contextes of the docker images are located
in the `docker-compose/*/` directories.

Procedure:

* Download or git clone the shanoir-ng code. The `master` branch should be the
  most stable while `develop` will contain the newests functionalities if you
  are interested in testing thoses.
* Execute the Maven build on the parent project with the following commands:
  ```
  cd shanoir-ng-parent/ && mvn install
  ```

  The build creates all .jar and .js executable files and stores a copy into the
  docker-compose/ folder to be used for building the docker images


* Build the docker images:
  ```
  docker-compose build
  ```

## CONFIGURE

Shanoir is configured with environment variables. It is mostly handled with a a
set of *facade* variables named `SHANOIR_*` (which cover the most typical
setups).

Name                  | Value             | Description                             |
--------------------- | ----------------- | --------------------------------------- | 
`SHANOIR_URL_HOST`    | *hostname*        | hostname where shanoir is reachable     |
`SHANOIR_URL_SCHEME`  | `http\|https`      | https (over TLS), http (plain text, NOT RECOMMENDED) |
`SHANOIR_SMTP_HOST`   | *hostname*        | SMTP relay for outgoing e-mails         |
`SHANOIR_ADMIN_EMAIL` | *e-mail address*  | contact address of the administrator (for outgoing e-mails) |
`SHANOIR_ADMIN_NAME`  | *name*            | name of the administrator (for outgoing e-mails) |
`SHANOIR_PREFIX`      | *slug* (optional) | prefix for container names (needed if you deploy multiple shanoir instances on the same host) |
`SHANOIR_X_FORWARDED` | `generate\|trust`  | configures whether the nginx container generates the `X-Forwarded-*` HTTP headers (if running stand-alone) or trusts the existing headers (if located behind another reverse-proxy) |
`SHANOIR_CERTIFICATE` | `auto\|manual`     | auto-generates a self-signed TLS certificate (NOT RECOMMENDED) or use a manually installed certificate |
`SHANOIR_MIGRATION`   | `auto\|init\|never\|manual\|export\|import` | Normal runs should use `auto` in development and `never` in production. Other values are for controlling deployment and migrations (see below). |
`SHANOIR_KEYCLOAK_USER`<br>`SHANOIR_KEYCLOAK_PASSWORD` | *username/password* | Keycloak admin account used by shanoir for managing user accounts |
`VIEWER_URL_SCHEME`  | `http\|https`      | https (over TLS), http (plain text, NOT RECOMMENDED) |
`VIEWER_URL_HOST`    | *hostname*         | hostname where the OHFI-Viewer is reachable     |

**Notes**
* You must ensure that the hostname `SHANOIR_URL_HOST` can be resolved from the
  clients using shanoir (the users) and from each microservice (running in the
  containers)
  * in development this is achieved by:
    * adding the following line to your **/etc/hosts**
      ```
      127.0.0.1  shanoir-ng-nginx viewer
      ```
    * ensuring that the nginx container name is equals to the value of
      `SHANOIR_URL_HOST` (in the default setup, they both use:
      `shanoir-ng-nginx`)
  * in production, you should have a the relevant A/AAAA configured in your DNS
    zone
* If docker is not running natively and thus you are using docker-machine
  (windows/macos users), you will need to tune the virtualbox machine:
    * increase the amount of allocated RAM
    * set up tcp port redirections (at least for 8080 and 443)
* The TLS configuration provided by the nginx container is permissive and not
  guaranteed to be up-to-date. You should not rely on it for facing the public
  internet. In production you should rather have a separate HTTP reverse-proxy,
  properly administrated according to your security policies.
* Upon config changes, for most cases you will just need to re-create the
  affected containers (run: `docker-compose up -d`). However if the changes also
  affect the *keycloak* container, then you will also need to re-deploy the
  shanoir-ng realm (see below).
  

## DEPLOY

0. ensure all containers are stopped and all volumes are destroyed (**CAUTION:
   this destroys all external volumes defined in docker-compose.yml**) 
   ```
   fig down -v
   ```

1. deploy the database containers and wait until they are ready to accept
   incoming connections
   ```
   docker-compose up -d database keycloak-database
   ```

2. initialise the keycloak container, then start it
   ```
   docker-compose run --rm -e SHANOIR_MIGRATION=init keycloak
   docker-compose up -d keycloak
   ```

3. initialise each microservice
   ```
   for ms in users studies datasets import preclinical ; do
       docker-compose run --rm -e SHANOIR_MIGRATION=init "$ms"
   done
   ```

4. start the remaining containers
   ```
   docker-compose up -d
   ```

## FIRST RUN

New user accounts need to be validated by a shanoir admin. However, on the first
run, there is not admin account so you will need to create it on the keycloak
server directly:

1. go to Keycloak admin interface: http://localhost:8080/auth/admin/
2. sign in with the credentials configured in
   `SHANOIR_KEYCLOAK_USER`/`SHANOIR_KEYCLOAK_PASSWORD' (default is `admin`/`&a1A&a1A`)
3. go to the **shanoir-ng** realm
4. create/edit the new user and grant the relevant role (eg. `ROLE_ADMIN`). By
   default, new user accounts are created in Keycloak by the users microservice
   with temporary passwords, you may reset the password in keycloak's admin
   interface and receive the new password is by e-mail. In development, if you
   do hot have a configured SMTP relay, then you may choose to overide the
   password manually and set `Temporary password: No` to make it persistent.


### PACS dcm4chee

Access to the backup PACS dcm4chee 5 arc-light: http://localhost:8081/dcm4chee-arc/ui2/

### Local data

This installation uses Docker named volumes, find more here to handle your local data:
https://docs.docker.com/storage/volumes/



## Migrations

TODO

