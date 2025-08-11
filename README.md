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

Keycloak instance -> your working realm -> User Federation -> Ldap
- Edit mode: **WRITABLE**
  <img width="1260" height="208" alt="image" src="https://github.com/user-attachments/assets/1e65de6d-1807-4dc5-80c0-34508601a10c" />
- Sync Registration: **ON**
  <img width="1165" height="267" alt="image" src="https://github.com/user-attachments/assets/165e66e8-6e67-4cda-bce1-a5fc3344a24d" />


Ldap -> Mappers
<img width="1856" height="874" alt="image" src="https://github.com/user-attachments/assets/4ad307c1-9018-4c59-a4a4-3ad67e8ef453" />

- mappers **first name** and **last name**: 
   - READ-ONLY **OFF**
   - Always Read Value from LDAP **OFF**
     <img width="738" height="200" alt="image" src="https://github.com/user-attachments/assets/e001d17b-3d9a-477d-8c43-374d6b465fee" />

  This will force the **event-listener** to capture the User data coming from SSO session, otherwise first name and last name would be empty, because Keycloak expects to read them from LDAP storage, but WRITABLE option does not automatically write them.
