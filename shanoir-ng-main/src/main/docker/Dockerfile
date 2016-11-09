FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD shanoir-ng-main-0.0.1-SNAPSHOT.jar app.jar
RUN sh -c 'touch /app.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app.jar","--spring.redis.host=redis"]