#!/bin/bash

CATALINA_BASE=`pwd`/apache-tomcat
CATALINA_HOME=$CATALINA_BASE/bin
WEB_APP_LIB=$CATALINA_BASE/webapps/pleak/WEB-INF/lib

echo "Compiling..." &&
javac -classpath $CATALINA_BASE/lib/servlet-api.jar:$WEB_APP_LIB/*: \
      -d         $CATALINA_BASE/webapps/pleak/WEB-INF/classes/ \
                 $CATALINA_BASE/webapps/pleak/src/com/naples/servlets/*.java \
                 $CATALINA_BASE/webapps/pleak/src/com/naples/helpers/*.java \
                 $CATALINA_BASE/webapps/pleak/src/com/naples/responses/*.java &&

echo "Starting..." &&
$CATALINA_HOME/startup.sh
