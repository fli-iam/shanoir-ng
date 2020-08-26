if [ -n "$JAVA_HOME" ];
then
echo "JAVA_HOME IS SET TO '$JAVA_HOME'";
$JAVA_HOME/bin/java -jar shanoir-uploader-7.0.0-jar-with-dependencies.jar -Xms128m -Xmx512m -Xnoclassgc
else
java -version >/dev/null 2>&1
if [ $? -ne 0 ]; then echo "ERROR";
else
java -jar shanoir-uploader-7.0.0-jar-with-dependencies.jar -Xms128m -Xmx512m -Xnoclassgc
fi
fi