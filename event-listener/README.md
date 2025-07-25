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

## Local testing
For local testing, use **docker-compose.yml** and **Dockerfile.test**

- clone this repo
- ``mvn clean install``
- ``docker compose up --build``
- Have Cyverse Discovery Environment or User Portal running
- Create a test _realm_ in the deployed Keycloak instance
- Create new users _OR_ setup a new identity provider (e.g Github)

**Note**: For User Portal, in order for the setup to work, the user created in Keycloak **must** be also registered in the User Portal DB before logging in (to achieve this, go through the registration steps.). More details: https://cyverse-austria.github.io/docs/database/portal-db/

