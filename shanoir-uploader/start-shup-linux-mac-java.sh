# Shanoir NG - Import, manage and share neuroimaging data
# Copyright (C) 2009-2019 Inria - https://www.inria.fr/
# Contact us on https://project.inria.fr/shanoir/
# 
# This program is free software: you can redistribute it and/or modify
# it under the terms of the GNU General Public License as published by
# the Free Software Foundation, either version 3 of the License, or
# (at your option) any later version.
# 
# You should have received a copy of the GNU General Public License
# along with this program. If not, see https://www.gnu.org/licenses/gpl-3.0.html

JAR_FILE=$(ls shanoir-uploader-*-jar-with-dependencies.jar 2>/dev/null | head -n 1)

if [ -z "$JAR_FILE" ]; then
  echo "ERROR: No JAR file found with the pattern shanoir-uploader-*-jar-with-dependencies.jar"
  exit 1
fi

APP_VERSION=$(echo "$JAR_FILE" | sed -E 's/shanoir-uploader-([0-9.]+)-jar-with-dependencies\.jar/v\1/')

if [ -n "$JAVA_HOME" ];
then
echo "Starting ShanoirUploader... (JAVA_HOME IS SET TO '$JAVA_HOME')";
$JAVA_HOME/bin/java -Dhttps.protocols=TLSv1.2 -Xms512m -Xmx2g -Xnoclassgc -Dapp.version="$APP_VERSION" -jar "$JAR_FILE"
else
java -version >/dev/null 2>&1
if [ $? -ne 0 ]; then echo "ERROR";
else
echo "Starting ShanoirUploader... (without JAVA_HOME)";
java -Dhttps.protocols=TLSv1.2 -Xms512m -Xmx2g -Xnoclassgc -Dapp.version="$APP_VERSION" -jar "$JAR_FILE"
fi
fi
