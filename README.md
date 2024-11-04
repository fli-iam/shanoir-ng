# Shanoir-NG - Import, manage and share neuroimaging data

Shanoir-NG (Next Generation) is a software that helps neuroimaging
analysis researchers, radiologists, and MRI operators to organize 
and share neuroimaging datasets. 

Shanoir-NG is copyrighted by [Inria](https://www.inria.fr/) and is open source under 
the [GNU General Public License v3.0](LICENSE). 

The Shanoir-NG website is available at:  https://project.inria.fr/shanoir/

>[!Important]
> Shanoir-NG is still in the development phase. 
> While many functionalities work well, some are not developed yet 
> and some might be unstable. 
> Also it still misses production features like database backup.


## Shanoir-NG User documentation
Documentation for Shanoir users is available at https://github.com/fli-iam/shanoir-ng/wiki#users

##  The Shanoir-NG repository
### Structure
Official source code repository:https://github.com/fli-iam/shanoir-ng

* The latest _stable version_ of Shanoir-NG is on `master` 
* The latest _development version_ of Shanoir-NG is on `develop` 
* The default branch of the repository is `develop`

### Cloning the Shanoir-NG repository

1. Clone the shanoir-ng repository
2. Go into the shanoir-downloader subfolder (it should be empty)
3. Initialise the shanoir-downloader submodule:
  - `git submodule init` to initialize your local configuration file
  - `git submodule update` to fetch all the data from shanoir 
and check out the appropriate commit listed in `shanoir_downloader`

Then the shanoir-downloader project can be simply managed as a normal
git repo (as if it were a separated project)  meaning that once
you are in the shanoir-downloader/ folder, you can just `git pull` 
to get the latest changes, and commit some changes.

## Shanoir-NG Installation
###  Requirements

To build and deploy Shanoir, you will need:
* Java 21 (since migration to Spring Boot 3.1.2)
* docker (https://docs.docker.com/install/)
* docker-compose 3 (https://docs.docker.com/compose/install/)
* maven 3 (https://maven.apache.org/download.cgi)
* at least 10GB of available RAM

### Installation instructions
Detailed installation instructions are available at https://github.com/fli-iam/shanoir-ng/wiki#users. 



## Help and Support

### Documentation

>[!Important]
>The [Shanoir-NG Wiki](https://github.com/fli-iam/shanoir-ng/wiki) is 
>the main documentation source. 

Hereunder are some direct links to 
main Shanoir-NG Wiki entries:
   + [User Documentation](https://github.com/fli-iam/shanoir-ng/wiki#users)
   + [Developer documentation](https://github.com/fli-iam/shanoir-ng/wiki#developers-dev)
   + [REST API](https://github.com/fli-iam/shanoir-ng/wiki#shanoir-ng-rest-api)
   + [Maintainer Documentation](https://github.com/fli-iam/shanoir-ng/wiki#operations-ops-installation-updates-and-maintenance-administration)
   + [Project Management documentation](https://github.com/fli-iam/shanoir-ng/wiki)
   + [Shanoir-NG website](https://project.inria.fr/shanoir/)  
     
### Communication
+ [News](https://project.inria.fr/shanoir/news/)  
+ [Youtube](https://www.youtube.com/watch?v=_Lpb3Pvw6e8)



