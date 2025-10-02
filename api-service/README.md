# REST API Service for LDAP and iRODS registration

This repository provides a REST API that can register a User to an LDAP server, iRODS DB or CyVerse User Portal DB.
Also provides a Docker image to run the iRODS (Integrated Rule-Oriented Data System) command line client (iCommands) version **4.3.1** on Ubuntu 22.04.  
It allows you to connect to an iRODS server from outside your local environment using environment variables to configure your connection.

---

## Features

- Java REST API based on Javalin library for triggering specific LDAP and iRODS actions
- Ubuntu 22.04 base image
- iRODS iCommands version 4.3.1 installed via the official iRODS apt repository
- Dynamically generates `irods_environment.json` from environment variables at container start
- Interactive bash shell with iRODS commands ready to use

---

## Supported iRODS Version

- iCommands client version: **4.3.1**  
- Compatible with iRODS server versions 4.3.x and above

---

## Usage

### Build the Docker image

Clone this repository and run:

```bash
docker build --build-arg APP_VERSION=0.0.1 -t api-service .
```

### Run the Docker Container 
Run the container with your iRODS server credentials passed via environment variables:

- Option 1: To start the REST API with the repository default configuration:

```bash
docker run -it \
  -e IRODS_HOST=irods.ies.example.com \
  -e IRODS_USER_NAME=your_username \
  -e IRODS_ZONE_NAME=your_zone \
  -e AUTH_SECRET=your_secret \ # if auth is enabled
  api-service api
```

- Option 2: To start the REST API with a custom configuration:

```bash
docker run -it \
  -e IRODS_HOST=irods.ies.example.com \
  -e IRODS_USER_NAME=your_username \
  -e IRODS_ZONE_NAME=your_zone \
  -e AUTH_SECRET=your_secret \ # if auth is enabled
  api-service api <your-config-file>.yml
```

- Option 3: To get into the interactive shell with access to the iRODS server:
```bash
docker run -it \
  -e IRODS_HOST=irods.ies.example.com \
  -e IRODS_USER_NAME=your_username \
  -e IRODS_ZONE_NAME=your_zone \
  api-service
```

The Dockerfile is based on the Dockerfile of this repository https://github.com/cyverse-austria/irods-client. More information
about iRODS workflows there.

## Logs
Audit logs for api-service are enabled with this plugin: https://javalin.io/plugins/devlogging.
Logs are configured in _resources/simplelogger.properties_. Choose there your desired log level.

In Keycloak, look for "_Event triggered login-listener_" log to check that the plugin is working as expected.

## Swagger and ReDoc
The API comes with [Swagger](https://swagger.io/) and [API documentation](https://swagger.io/blog/api-development/redoc-openapi-powered-documentation/) integrated. Once running, visit _http://<your_api_host>/swagger_ or _http://<your_api_host>/redoc_

## Authentication
API supports authentication for users stored in the configuration, which through an API-Key would generate a JWT Token with an expiration time.
The authentication can be enabled/disabled through this API's config file. Simply adding or removing ```authConfig:``` section will enable/disable it.
Additionally, when the authentication is enabled, it requires **AUTH_SECRET** environment variable, which should be a generated long hash.

**Example**: Generate a hash secret using: ```openssl rand -base64 32```.
