# Shanoir clients

Clients (tools which use the Shanoir API) are added to the main Shanoir repository as [git submodules](https://git-scm.com/book/en/v2/Git-Tools-Submodules).

## Submodules

### Initialize git submodules when cloning the shanoir-ng repository

When we clone the shanoir-ng repository, the shanoir-downloader/ folder will be empty ; two commands must be run to get the code:

   - `git submodule init to initialize your local configuration file`
   - `git submodule updateto fetch all the data from shanoir and check out the appropriate commit listed in shanoir_downloader`

Then the shanoir-downloader project can be simply managed as a normal git repo (as if it were a separated project) ; meaning that once your are in the shanoir-downloader/ folder, you can just git pull to get the latest changes, and commit some changes.

### Add a git submodule

 - `cd` to the `clients` directory 
 - `git submodule add git@github.com:Inria-Empenn/shanoir_downloader.git`

This will create the following entry in the `.gitmodules` file:
```
[submodule "clients/shanoir-downloader"]
	path = clients/shanoir-downloader
	url = git@github.com:Inria-Empenn/shanoir_downloader.git
```

## Shanoir Downloader

Shanoir downloader enables to download datasets, check that the downloaded DICOM content is correct, anonymize, archive and encrypt them.
The `convert_dicoms_to_niftis.py` script enables to convert DICOMS to nifti using different converters and logs the conversion results in a .csv file.

## Download data on Shanoir

This script uses previous script (shanoir_downloader.py) to interact more easily with Solr and reorganize data as BIDS-like specification. Of note, the script needs to be modify to fit your needs as described in the header. Indeed, you need to define how you want to organize your data, the ID of your study, the data you want to retrieve, where you want to download the data, you Shanoir ID and the type of file you want to download (either DICOM or NIFTI, conversion done with dcm2niix directly by Shanoir).