FROM java:8

VOLUME /tmp

ADD kms-5.14.2.war kms.war

EXPOSE 8080

ENTRYPOINT ["java","-jar","kms.war"]