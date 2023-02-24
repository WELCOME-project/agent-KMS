FROM adoptopenjdk/openjdk11:alpine

VOLUME /tmp

ADD kms-5.14.2.war kms.war

EXPOSE 8080

ENTRYPOINT ["java","-jar","kms.war"]