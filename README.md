# Shanoir-NG - Import, manage and share neuroimaging data

Shanoir-NG (Next Generation) is a software that helps neuroimaging
analysis researchers, radiologists, and MRI operators to organize 
and share neuroimaging datasets. 

Shanoir-NG is copyrighted by [Inria](https://www.inria.fr/) and is open source under 
the [GNU General Public License v3.0](LICENSE). 

The Shanoir NG website is available at:  https://project.inria.fr/shanoir/

>[!Important]
> Shanoir NG is still in the development phase. 
> While many functionalities work well, some are not developed yet 
> and some might be unstable. 
> Also it still misses production features like database backup.

## Shanoir-NG User documentation

## Shanoir-NG repository 

###  Branches

* The latest _stable version_ of Shanoir-NG is on `master` 
* The latest _dev version_ of Shanoir-NG is on `develop` (if mature -> merged into master)

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
* maven 3
* at least 10GB of available RAM


## 

