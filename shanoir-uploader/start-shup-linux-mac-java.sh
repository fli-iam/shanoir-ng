if [ -n "$JAVA_HOME" ];
then
echo "Starting ShanoirUploader... (JAVA_HOME IS SET TO '$JAVA_HOME')";
$JAVA_HOME/bin/java -Dhttps.protocols=TLSv1.2 -Xms128m -Xmx512m -Xnoclassgc -jar shanoir-uploader-7.0.1-jar-with-dependencies.jar
else
java -version >/dev/null 2>&1
if [ $? -ne 0 ]; then echo "ERROR";
else
echo "Starting ShanoirUploader... (without JAVA_HOME)";
java -Dhttps.protocols=TLSv1.2 -Xms128m -Xmx512m -Xnoclassgc -jar shanoir-uploader-7.0.1-jar-with-dependencies.jar
fi
fi