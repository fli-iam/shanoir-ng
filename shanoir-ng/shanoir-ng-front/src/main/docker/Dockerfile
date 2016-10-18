FROM frolvlad/alpine-oraclejdk8:slim
VOLUME /tmp
ADD shanoir-ng-front-0.0.1-SNAPSHOT.jar app-front.jar
RUN sh -c 'touch /app-front.jar'
ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-jar","/app-front.jar","--spring.redis.host=redis"]