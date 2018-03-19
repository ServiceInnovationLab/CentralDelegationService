#!/bin/bash

# start openam
/usr/local/tomcat/bin/catalina.sh start

echo "Sleeping for $2 seconds, waiting for tomcat to start"
sleep $2

# initial config
cd /tmp/amster
sh amster /tmp/$1.amster

/usr/local/tomcat/bin/catalina.sh stop
