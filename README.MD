## Debugging sample for TeamCity LDAP integration

Use following command to run users sync with your ldap-config.properties
```bash
mvn spring-boot:run -Dspring-boot.run.arguments="--configurationFile=ldap-config.properties"
```