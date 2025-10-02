# de-register

Services needed to fully register a new user that logs through keycloak in cyverse

## [event-listener](event-listener/README.md)
Keycloak plugin that captures user data and sends it to a REST API

## [api-service](api-service/README.md)
API that receives users data and registers it in LDAP and iRODS

## [register-test](register-test/README.md)
For now a basic script that sets up all services needed to locally test this service

## Flow

<img width="552" height="501" alt="flow2 drawio(1)" src="https://github.com/user-attachments/assets/5424e8e3-6d4c-4842-84a7-c6a41b42be06" />

A diagram that illustrates the basic flow of data for a CyVerse service login flow using this service.

**Example usecase**:
1. User tries to login in CyVerse User portal
2. Redirect to Keycloak -> Login through SSO
3. LDAP Account with only basic attributes is created automatically by Keycloak. In Keycloak the LDAP Account and IDP are now linked
4. Event-listener is triggered -> actions are sent via HTTP to api-service
5. Api-service does the following
    - updates the newly created LDAP account with CyVerse specific attributes
    - creates new iRODS user
    - sends HTTP request to CyVerse User portal to create the CyVerse User in the database
6. Once the user is in the CyVerse database, the home page should appear
7. The user gets the Welcome email from CyVerse. In the email there is a link used to set a password.
8. CyVerse user portal sets the password chosen by the user to LDAP and iRODS
9. Now user has the account from SSO linked to all the needed user storages in CyVerse


## Keycloak LDAP User Federation configuration
The IDP and LDAP Account need to be linked. The way to achieve this is to configure Keycloak LDAP to **WRITABLE** and some extra steps:

Keycloak instance -> your working realm -> User Federation -> Ldap
- Edit mode: **WRITABLE**
  <img width="1260" height="208" alt="image" src="https://github.com/user-attachments/assets/1e65de6d-1807-4dc5-80c0-34508601a10c" />
- Sync Registration: **ON**
  <img width="1165" height="267" alt="image" src="https://github.com/user-attachments/assets/165e66e8-6e67-4cda-bce1-a5fc3344a24d" />


Ldap -> Mappers
<img width="1856" height="874" alt="image" src="https://github.com/user-attachments/assets/4ad307c1-9018-4c59-a4a4-3ad67e8ef453" />

- mappers **first name** and **last name**: 
   - Always Read Value from LDAP **OFF**
    <img width="518" height="133" alt="image" src="https://github.com/user-attachments/assets/7ae24126-5fa5-41de-8f09-d627ed2d382d" />

  This will force the **event-listener** to capture the User data coming from SSO session, otherwise first name and last name would be empty, because Keycloak expects to read them from LDAP storage, but WRITABLE option does not automatically write them.
