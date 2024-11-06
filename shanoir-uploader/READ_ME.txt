##############################################
Hints on working with ShanoirUploader
##############################################

####### Version update #############
For all new versions (e.g. if you want to upgrade the version from v9.0.0 to v9.0.1):
Search with "9.0.0" on the folder shanoir-uploader and replace all occurrences with "9.0.1".
This is important, that all scripts .sh or .bat find the correct version to start and create
the correct folder.

Attention: do the above to not forget the logback.xml hard coded file path for logs.

Attention: to deliver for Java 17 in the hospitals: switch back to Java 17 in two pom.xml:
1) shanoir-uploader/pom.xml and 2) shanoir-ng-back/pom.xml
2) run a full Maven clean install on shanoir-ng-parent to avoid,
that e.g. shanoir-ng-import remains still compiled with Java 21

####### Maven: build a new Executable Jar version #############
Build latest version for delivery using the below mvn command:
Use "mvn package", to create one big zip containing everything.
You find a shanoir-uploader-{version}-{timestamp}.zip in /target,
that contains the new distribution.jar, containing everything.
