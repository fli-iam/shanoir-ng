#!/bin/sh

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
#

### ====================================================================== ###
##                                                                          ##
##  shanoir/downloadDataset Launcher                                        ##
##                                                                          ##
### ====================================================================== ###

MAIN_CLASS=org.shanoir.downloader.ShanoirDownloader
MAIN_JAR=./target/shanoir-downloader-6.0.3-jar-with-dependencies.jar

$JAVA_HOME/bin/java -cp $MAIN_JAR $MAIN_CLASS
