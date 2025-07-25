# REST API Service for LDAP and iRODS registration

This repository provides a REST API that can register a User to an LDAP server or iRODS.
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
  api-service api
```

- Option 2: To start the REST API with a custom configuration:

```bash
docker run -it \
  -e IRODS_HOST=irods.ies.example.com \
  -e IRODS_USER_NAME=your_username \
  -e IRODS_ZONE_NAME=your_zone \
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
