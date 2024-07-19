##############################################
Hints on working with ShanoirUploader
##############################################

####### Version update #############
For all new versions (e.g. if you want to upgrade the version from v6.0.4 to v7.0.1):
Search with "6.0.4" on the folder shanoir-uploader and replace all occurrences with "7.0.1".
This is important, that all scripts .sh or .bat find the correct version to start and create
the correct folder.

####### Maven: build a new Executable Jar version #############
Build latest version for delivery using the below mvn command:
Use "mvn package", to create one big zip containing everything.
You find a shanoir-uploader-{version}-{timestamp}.zip in /target,
that contains the new distribution.jar, containing everything.