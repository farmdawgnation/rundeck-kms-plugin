FROM rundeck/rundeck:3.3.9

COPY rundeck-config-storage.properties /etc/remco/templates/rundeck-config-storage.properties

USER root

RUN chown rundeck:root /etc/remco/templates/rundeck-config-storage.properties

USER rundeck
