# de-register
Keycloak Event-Listener custom implementation that creates LDAP and iRODS accounts based on information received from the user that authenticates through Keycloak.

## Configure inside Keycloak
Select the target _realm_, go to _Realm settings_ -> _Events_, search and add **login-listener**. 

## Local testing
- clone this repo
- ``mvn clean install``
- ``docker compose up --build``
- Have Cyverse Discovery Environment or User Portal running
- Create a test _realm_ in the deployed Keycloak instance
- Create new users _OR_ setup a new identity provider (e.g Github)

**Note**: For User Portal, in order for the setup to work, the user created in Keycloak **must** be also registered in the User Portal DB before logging in (to achieve this, go through the registration steps.). More details: https://cyverse-austria.github.io/docs/database/portal-db/

