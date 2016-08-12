# Dropwizard tutorial

## Simple tutorial
http://www.dropwizard.io/0.9.2/docs/getting-started.html

- Compile project
    - mvn package
- Launch server (executable jar)
    - java -jar target\hello-world-1.0-SNAPSHOT.jar server hello-world.yml

This command launch jar file as server.
hello-world.yml is the project configuration file. It is loaded by HelloWorldConfiguration class, which is loaded at server startup by HelloWorldApplication class.
HelloWorldResource defines REST resource (jersey).

## Hibernate tutorial
based on https://dzone.com/articles/getting-started-with-dropwizard-connecting-to-a-da
     and http://www.dropwizard.io/0.7.1/docs/manual/hibernate.html

- Modify pom.xml
    - comment line 59
    - uncomment line 60 (indicating main class)
- Configure hibernate.yml (used to define database configuration)
- Compile project
    - mvn package
- Launch server (executable jar)
    - java -jar target\hello-world-1.0-SNAPSHOT.jar server hibernate.yml

This command launch jar file as server.
hibernate.yml is the project configuration file. It is loaded by DWHibernateConfiguration class, which is loaded at server startup by DWHibernateApplication class.
User class is entity class (hibernate).
UserDAO defines DAO for User entity.
UserResource defines REST resource (jersey).
