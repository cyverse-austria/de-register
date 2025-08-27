# event-listener
Keycloak Event-Listener custom implementation that acts as a REST API Client and sends requests to an external service for
creation of LDAP and iRODS accounts based on information received from the user that authenticates through Keycloak.

## Configure inside Keycloak
Select the target _realm_, go to _Realm settings_ -> _Events_, search and add **login-listener**.
<img width="1505" height="280" alt="image" src="https://github.com/user-attachments/assets/fd95f376-0e95-4e40-b2e5-a8878dd8784e" />

## Docker
When building an image to a docker namespace, use **Dockerfile** (context: .). That is just a busybox container with the JAR copied inside of it.
Then, use the container to extract the jar inside _/opt/keycloak/providers/_ directory **BEFORE** starting the Keycloak service. Only having the
plugin JAR available at that path will make this flow work as expected.

Make sure to set this environment variable for the keycloak container: 
```KC_SPI_EVENTS_LISTENER_LOGIN_LISTENER_API_SERVICE_HOST: <URL_OF_API_SERVICE>```.

## Authentication
The API service has authentication implemented. To successfully login with the event-listener in the api-service, make sure to configure these
variables:
```
KC_SPI_EVENTS_LISTENER_LOGIN_LISTENER_API_KEY: <api_key>
KC_SPI_EVENTS_LISTENER_LOGIN_LISTENER_SERVICE_MAIL: test.user@example.com
KC_SPI_EVENTS_LISTENER_LOGIN_LISTENER_SERVICE_PASSWORD: testpass
```
API service should have a user configured for event-listener, then the user credentials should be the same as here.


### Portal user creation
Event-listener plugin will automatically try and create the user that logs in through Keycloak in the User Portal DB. This happens if the following
configs are set in the Keycloak instance:
```
KC_SPI_EVENTS_LISTENER_LOGIN_LISTENER_USER_PORTAL_HOST: http://192.168.31.115:3000
KC_SPI_EVENTS_LISTENER_LOGIN_LISTENER_HMAC_KEY: <ADD SECRET HERE>
KC_SPI_EVENTS_LISTENER_LOGIN_LISTENER_PORTAL_DIVISOR: 7
```
These values should be the same as their respective counterparts in the actual running Portal service.
The plugin will still work even when not providing these values, just that user creation in portal DB will be skipped.


## Local testing
Testing should be done by using register-test module to start all necessary services.

- clone this repo
- ``mvn clean install``
- check [register-test](../register-test/README.md) for next steps
