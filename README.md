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

Because of the way Keycloak sessions work, creating the LDAP and iRODS accounts **might not be sufficient** for the CyVerse service to work properly. If this is the case, when authenticating with Keycloak **the first time** follow these steps:

1. Enter you Keycloak username and password OR Click on the already setup IDP
2. LDAP and iRODS accounts are being created in the background
3. You will be either redirected back to the home page (still not logged in) OR prompted with an error
4. Refresh the page or exit and load it up again
5. Login the same way as at step 1
   
   <img width="706" height="692" alt="image" src="https://github.com/user-attachments/assets/627d48e3-78eb-4d08-9c23-1d88dcd6fcdd" />
   
6. Keycloak will prompt **"User already exists"** - because now the LDAP User Federation has imported the user, but the keycloak/session account is not linked to it yet. Click on **Add to existing account**.
7. Enter the LDAP password
8. Now the session account and LDAP account are linked and the user has the correct attributes. The CyVerse service should work as expected

Subsequent logins will work automatically just by clicking on SSO or entering the credentials, there won't be a need to follow these steps.
