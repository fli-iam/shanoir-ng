##############################################
Hints on working with ShanoirUploader
##############################################

####### Version update #############
For all new versions (e.g. if you want to upgrade the version from v9.0.0 to v9.0.1):
Search with "9.0.0" on the folder shanoir-uploader and replace all occurrences with "9.0.1".
This is important, that all scripts .sh or .bat find the correct version to start and create
the correct folder.

Attention: do the above, that you might not forget the logback.xml hard coded file path for logs

Attention: to deliver for Java 17 in the hospitals: switch back to Java 17 in two pom.xml:
1) shanoir-uploader/pom.xml and 2) shanoir-ng-back/pom.xml
2) run a full Maven clean install on shanoir-ng-parent to avoid,
that e.g. shanoir-ng-import remains still compiled with Java 21

Attention: when you made changes in ms-import, e.g. how to query the PACS, as the code is
unified, you have to compile both projects 1) shanoir-ng-import 2) shanoir-uploader, that
the change, e.g. in PACSQueryService is part of the new version of ShUp.

####### OFSEP ######################
Attention: now a build profile for ofsep (to integrate the pseudonymus keys into the profiles)
has been introduced. If you deliver now a new version for OFSEP, please use
"mvn clean install -Pofsep"
If not, it will not work for OFSEP and an error will be thrown during imports.

####### Maven: build a new Executable Jar version #############
Build latest version for delivery using the below mvn command:
Use "mvn package", to create one big zip containing everything.
You find a shanoir-uploader-{version}-{timestamp}.zip in /target,
that contains the new distribution.jar, containing everything.
