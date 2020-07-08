##############################################
Getting Started
##############################################

####### Before delivery to OFSEP #############
Remove the 3 system path library dependencies as they do not be contained in the assembly.

####### Build latest version for delivery ####
Use "mvn clean compile assembly:single", to create one big jar containing everything

####### Complete your Maven repository #######

http://shanoir.gforge.inria.fr/doku.php?id=intern:shanoiruploader

## In the Shanoir project:
First, you need to get the WSDL files from the Shanoir server. To do so, you can
	* execute the Ant task ant build-server-java : This will create the wanted 
	wsdl files in the directory ${Shanoir_project_src}/resources/WEB-INF/wsdl.
	* execute the Ant task ant archive : This will generate the Shanoir.jar archive

All in one : build the following ant tasks : clean, clean_jboss, build-server-java, 
archive, explode

## In the Shanoirws project:
	* in the pom.xml file, you must define where are located the WSDL files. To do so, 
	just modify the “wsdlSrcDir” property at the end of the pom.xml file.
	* execute the command line 
			>>> mvn clean compile license:format license:check package 
			at the root of the Shanoirws project. This will create a jar file into 
			the “target” directory.
	* execute the command line 
			>>> mvn install
			This will install your ws librairy on your local maven repository, by 
			copying ~/workspace/Shanoirws/target/shanoir-ws-0.3.jar into ~/.m2/repository/org/shanoir/shanoir-ws/0.3/

Now retrieve Shanoir librairies and copy them in your maven repository
	* copy (and rename) the Shanoir.jar archive 
			>>> cp ~/workspace/Shanoir/dist-dev/Shanoir.jar ~/.m2/repository/org/shanoir/shanoir/1.0.0/shanoir-1.0.0.jar
	* copy (and rename) 
			>>> cp ~/workspace/Shanoir/lib/dcm4che2-tool-dcmqr-custom.jar ~/.m2/repository/dcm4che2/tool/dcm4che2-tool-dcmqr-custom/1.0.0/dcm4che2-tool-dcmqr-custom-1.0.0.jar

And copy the web service from Shanoir that you need for ShanoirUploader
	* copy 
			>>> cp ~/workspace/Shanoir/resources/WEB-INF/wsdl/UploadFileService.wsdl ~/workspace/shanoir-uploader/src/main/resources/

## In the shanoir-uploader project:
-----------------------------------------------
!!! WARNING !!! For your very first compilation, 
please read the "First build of ShanoirUploader" section
-----------------------------------------------
	* execute mvn clean install in order to build your project
			>>> mvn clean install
	
	
####### First build of ShanoirUploader #######  

-----------------------------------------------
NB : Build ShanoirUploader (called "mvn install" below) =
		- command line : cd ~/workspace/shanoir-uploader &&  mvn install
		- or in Eclipse : right-click on the pom.xml and select Run As... Maven install
	 Sometimes it can be helpful to run a Maven clean before an Maven install.
-----------------------------------------------
	  
	* Comment the plugin <artifactId>webstart-maven-plugin</artifactId> and 
		<artifactId>maven-jarsigner-plugin</artifactId> in pom.xml file
	* mvn install
	* Uncomment the plugin <artifactId>webstart-maven-plugin</artifactId> and 
		<artifactId>maven-jarsigner-plugin</artifactId> in pom.xml file
	* mvn install