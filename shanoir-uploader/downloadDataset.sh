#!/bin/sh

# ************** BEGIN LICENSE BLOCK **********
#
# Version: CeCILL-C
# Copyright INRIA
#
# Contributor(s):
# Guillaume RENARD <guyome.renard@googlemail.com>
# Adrien FERIAL <adrien.ferial@gmail.com>
#
# This software is governed by the CeCILL-C license under French law and
# abiding by the rules of distribution of free software. You can use, modify
# and/ or redistribute the software under the terms of the CeCILL-C license as
# circulated by CEA, CNRS and INRIA at the following URL
# "http://www.cecill.info".
#
# As a counterpart to the access to the source code and rights to copy, modify
# and redistribute granted by the license, users are provided only with a
# limited warranty and the software's author, the holder of the economic
# rights, and the successive licensors have only limited liability.
#
# In this respect, the user's attention is drawn to the risks associated with
# loading, using, modifying and/or developing or reproducing the software by
# the user in light of its specific status of free software, that may mean that
# it is complicated to manipulate, and that also therefore means that it is
# reserved for developers and experienced professionals having in-depth
# computer knowledge. Users are therefore encouraged to load and test the
# software's suitability as regards their requirements in conditions enabling
# the security of their systems and/or data to be ensured and, more generally,
# to use and operate it in the same conditions as regards security.
#
# The fact that you are presently reading this means that you have had
# knowledge of the CeCILL-C license and that you accept its terms.
#
# ************** END LICENSE BLOCK **********
#

### ====================================================================== ###
##                                                                          ##
##  shanoir/downloadDataset Launcher                                        ##
##                                                                          ##
### ====================================================================== ###

MAIN_CLASS=classes.org.shanoir.downloader.ShanoirDownloader
MAIN_JAR=./target/shanoir-downloader-6.0.3-jar-with-dependencies.jar

# $JAVA_HOME/bin/java -cp $MAIN_CLASS -jar $MAIN_JAR
$JAVA_HOME/bin/java -cp $MAIN_JAR $MAIN_CLASS
# $JAVA_HOME/bin/java -jar $MAIN_JAR  -Xms128m -Xmx512m -Xnoclassgc

# DIRNAME="`dirname "$0"`"

# # OS specific support (must be 'true' or 'false').
# cygwin=false;
# case "`uname`" in
#     CYGWIN*)
#         cygwin=true
#         ;;
# esac

# # For Cygwin, ensure paths are in UNIX format before anything is touched
# if $cygwin
# then
#     JAVA_HOME=`cygpath --unix "$JAVA_HOME"` &&
# 	SHANOIR_DOWNLOADER=`cygpath --unix "$DIRNAME"/..`
# fi

# # Setup SHANOIR_DOWNLOADER
# if test -z "$SHANOIR_DOWNLOADER"
# then SHANOIR_DOWNLOADER="$DIRNAME"/..;
# fi
# # SHANOIR_DOWNLOADER_VM
# if test -n "$JAVA_HOME"
# then
#     JAVA=$JAVA_HOME/bin/java
# else
#     JAVA="java"
# fi

# # Setup the classpath

# if $cygwin
# then
# 	CP="$CP;$SHANOIR_DOWNLOADER/lib/$MAIN_JAR"
# else
# 	CP="$CP:$SHANOIR_DOWNLOADER/lib/$MAIN_JAR"
# fi

# # For Cygindows format before running java
# if $cygwin
# then
#     JAVA="\""`cygpath --path --unix "$JAVA"`"\""
#     CP=`cygpath --path --unix "$CP"`
# fi


# # Execute the JVM
# if $cygwin
# then
# 	eval $JAVA $JAVA_OPTS -Xms128m -Xmx512m -Xnoclassgc -cp "$CP" $MAIN_CLASS "$@"
# else
# 	exec $JAVA $JAVA_OPTS -Xms128m -Xmx512m -Xnoclassgc -cp "$CP" $MAIN_CLASS "$@"
# fi

# if [ -n "$JAVA_HOME" ];
# then
# echo "JAVA_HOME IS SET TO '$JAVA_HOME'";
# $JAVA_HOME/bin/java -jar shanoir-uploader-6.0.3-jar-with-dependencies.jar -Xms128m -Xmx512m -Xnoclassgc -cp $MAIN_CLASS
# else
# java -version >/dev/null 2>&1
# if [ $? -ne 0 ]; then echo "ERROR";
# else
# java -jar shanoir-uploader-6.0.3-jar-with-dependencies.jar -Xms128m -Xmx512m -Xnoclassgc -cp $MAIN_CLASS
# fi
# fi

