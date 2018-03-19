FROM jboss/wildfly:latest

COPY standalone-with-debug.conf /opt/jboss/wildfly/bin/standalone.conf

RUN /opt/jboss/wildfly/bin/add-user.sh admin Admin#007 --silent

EXPOSE 8080 9990 8787

CMD ["/opt/jboss/wildfly/bin/standalone.sh", "-b", "0.0.0.0", "-bmanagement", "0.0.0.0"]

