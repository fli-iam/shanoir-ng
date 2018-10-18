CREATE USER 'keycloak'@'localhost' IDENTIFIED BY 'password';
CREATE USER 'keycloak'@'%' IDENTIFIED BY 'password';
GRANT ALL ON *.* TO 'keycloak'@'localhost';
GRANT ALL ON *.* TO 'keycloak'@'%';