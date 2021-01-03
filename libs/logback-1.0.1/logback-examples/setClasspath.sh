#!/bin/sh

##
# This script will add logback jars to your classpath.
##

LB_HOME=/SET/THIS/PARAMETER/TO/THE/DIRECTORY/WHERE/YOU/INSTALLED/LOGBACK

CLASSPATH="${CLASSPATH}:${LB_HOME}/logback-classic-1.0.1.jar"
CLASSPATH="${CLASSPATH}:${LB_HOME}/logback-core-1.0.1.jar"
CLASSPATH="${CLASSPATH}:${LB_HOME}/logback-examples/logback-examples-1.0.1.jar"
CLASSPATH="${CLASSPATH}:${LB_HOME}/logback-examples/lib/slf4j-api-1.6.4.jar"

export CLASSPATH

echo $CLASSPATH
