FROM openjdk:8-jdk-alpine

# Se a imagem for ALPINE não vai ser preciso usar o "USER root"
USER root
EXPOSE 8080
RUN addgroup -S api && adduser -S api -G api
#RUN echo "deb http://packages.cloud.google.com/apt gcsfuse-bionic main" |  tee /etc/apt/sources.list.d/gcsfuse.list
#RUN curl -k https://packages.cloud.google.com/apt/doc/apt-key.gpg | apt-key add -

#RUN apt-get update && apt-get install -y gcsfuse

#RUN apt-get install ca-certificates -y

# Bucket files will be mounted here
RUN mkdir -p /opt/corda/node/api

COPY --chown=api:api ./utils/entry.sh /opt/corda/node/api/entry
COPY --chown=api:api ./clients/build/libs/clients-0.1-all.jar /opt/corda/node/api/clients-0.1-all.jar
RUN chmod +x /opt/corda/node/api/entry

WORKDIR /opt/corda/node/api
USER api

ENTRYPOINT ["/bin/sh", "/opt/corda/node/api/entry"]