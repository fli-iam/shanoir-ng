##############################################
Hints on working with ShanoirUploader
##############################################

####### Version update #############
For all new versions (e.g. evolution from v6.0.4 to v7.0.1):
Search with "6.0.4" on the folder shanoir-uploader and replace all occurrences with 7.0.1.
This is important, that all scripts .sh or .bat find the correct version to start and create
the correct folder.

####### Maven: build a new Executable Jar version #############
1) Copy the library from src/main/resources/dcm4che2-tool-dcmqr-custom-1.0.0.jar into your
local Maven repository: /home/XXX/.m2/repository/dcm4che2/tool/dcm4che2-tool-dcmqr-custom
2) Remove the below TWO LINES from your pom.xml (only locally and do not commit it please):
-- <scope>system</scope>
-- <systemPath>${project.basedir}/src/main/resources/dcm4che2-tool-dcmqr-custom-1.0.0.jar</systemPath>
3) Build latest version for delivery using the below mvn command:
Use "mvn clean compile assembly:single", to create one big jar containing everything
