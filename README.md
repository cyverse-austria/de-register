# de-register

Services needed to fully register a new user that logs through keycloak in cyverse

## [event-listener](event-listener/README.md)
Keycloak plugin that captures user data and sends it to a REST API

## [api-service](api-service/README.md)
API that receives users data and registers it in LDAP and iRODS

## [register-test](register-test/README.md)
For now a basic script that sets up all services needed to locally test this service

## Flow

<img width="747" height="491" alt="flow2 drawio" src="https://github.com/user-attachments/assets/2ebb1908-b310-4c5f-9c7f-ce6137b44896" />

A simple diagram that illustrates the basic flow of data for a CyVerse service login flow using this service.

Because of the way Keycloak sessions work, creating the LDAP account **might not be sufficient** for the CyVerse service to work properly. To address this, make sure to have these setups in your Keycloak LDAP User federation:

- Edit mode: **WRITABLE**
- Sync Registration: **ON**
- LDAP mapper first name, last name: 
   - READ-ONLY **OFF**
   - Always Read Value from LDAP **OFF**