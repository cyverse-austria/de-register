# register-test

In this module, CyVerse services bound with Keycloak login are configured and deployed
automatically for testing purposes.

## Local testing

Run `./run-tests.sh <service> <client-secret>`. Where:

 - service: the CyVerse service to test - currently only **User Portal** supported
 - client-secret: the client secret for the Keycloak client that is configured for the service

The script will:

- setup the database for the chosen service as well as for Keycloak.
- setup the 2 de-register services present in this repository and start them:
api-service, event-listener.
- additionally, the Java Application script will create a testing realm, a client for the
service, testing users and an LDAP User federation based on the configuration file.
- will start the configured service.
- additional manual steps may still be required. These will be prompted at the end of the script.

## Structure

This directory structure must be respected in order for the script to work.

```
root-dir
|- de-register
|    |- event-listener
|    |- api-service
|    |- register-test
|- portal2
|- portal2-db
```

portal2: https://github.com/cyverse-austria/portal2 OR https://github.com/cyverse-de/portal2

portal2-db: https://github.com/cyverse-austria/portal2-db

## Long term
For now this module is used only to setup and configure the requiered services and plugins 
in order to test the whole de-register workflow. In the future this module may evolve in a
fully automated integration testing module.