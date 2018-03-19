# OpenAM Dockerfile
#
# Copyright (c) 2016-2017 ForgeRock AS. Use of this source code is subject to the
# Common Development and Distribution License (CDDL) that can be found in the LICENSE file

FROM tomcat:8.5-jre8

ENV CATALINA_HOME /usr/local/tomcat
ENV PATH $CATALINA_HOME/bin:$PATH
WORKDIR $CATALINA_HOME

# Note: By default, Tomcat does NOT listen on 8443. You must add a custom server.xml file to change this.
EXPOSE 8080 8443

ENV OPENAM_VERSION=14.0.0-SNAPSHOT

RUN wget ###_REPLACE_THIS_WITH_VALID_URL_TO_OPENAM_DEPENDENCY_### -O /tmp/openam.war \
  && rm -fr /usr/local/tomcat/webapps/* \
  && unzip -q /tmp/openam.war -d /usr/local/tomcat/webapps/openam \
  && rm /tmp/openam.war

RUN wget ###_REPLACE_THIS_WITH_VALID_URL_TO_OPENAM_DEPENDENCY_### -O /tmp/amster.zip \
  && unzip -q /tmp/amster.zip -d /tmp/amster \
  && rm /tmp/amster.zip

# Premake the path and configuration directory.
RUN mkdir -p /root/openam/openam && mkdir -p /root/.openamcfg \
  && echo "/root/openam" > /root/.openamcfg/AMConfig_usr_local_tomcat_webapps_openam_

# Generate a default keystore for SSL - only needed if you want SSL inside the cluster
# You can mount your own keystore on the ssl/ directory to override this.
#RUN mkdir -p /usr/local/tomcat/ssl && \
#   keytool -genkey -noprompt \
#     -keyalg RSA \
#     -alias tomcat \
#     -dname "CN=forgerock.com, OU=ID, O=FORGEROCK, L=Calgary, S=AB, C=CA" \
#     -keystore /usr/local/tomcat/ssl/keystore \
#     -storepass password \
#     -keypass password

# Custom server.xml: use this if OpenAM is behind SSL termination.
# See the server.xml file for details.
COPY server.xml /usr/local/tomcat/conf/

# For debugging OpenAM in a container, uncomment this.
# Use something like  kubectl port-forward POD 5005:5005
# ENV CATALINA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=5005"

# Settings for Tomcat cache.
COPY context.xml /usr/local/tomcat/conf/

# ---------------------------------------------------------------------------------
# Tomcat setup complete - anything that needs to start OpenAM should be after here
# ---------------------------------------------------------------------------------

COPY configure-am.sh \
  install.amster \
  /tmp/

RUN /tmp/configure-am.sh install 1
