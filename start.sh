#!/bin/bash

CATALINA_BASE=`pwd`/apache-tomcat
CATALINA_HOME=$CATALINA_BASE/bin

echo "Compiling..." &&
javac -classpath /usr/share/java/tomcat-servlet-api-3.0.jar -d $CATALINA_BASE/webapps/pleak/WEB-INF/classes/ \
            $CATALINA_BASE/webapps/pleak/src/com/naples/servlets/*.java &&

echo "Starting..." &&
$CATALINA_HOME/startup.sh
