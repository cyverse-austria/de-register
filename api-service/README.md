# iRODS CLI Docker Client

This repository provides a Docker image to run the iRODS (Integrated Rule-Oriented Data System) command line client (iCommands) version **4.3.x** on Ubuntu 22.04.  
It allows you to connect to an iRODS server from outside your local environment using environment variables to configure your connection.
It can also deploy an iRODS REST API that can be queried for various tasks.

---

## Features

- Ubuntu 22.04 base image
- iRODS iCommands version 4.3.x installed via the official iRODS apt repository
- Dynamically generates `irods_environment.json` from environment variables at container start
- Interactive bash shell with iRODS commands ready to use
- Java REST API based on Javalin library for triggering specific iRODS actions

---

## Supported iRODS Version

- iCommands client version: **4.3.x**  
- Compatible with iRODS server versions 4.3.x and above

---

## Usage

### Build the Docker image

Clone this repository and run:

```bash
docker build -t irods-client-slim .
```

### Run the Docker Container 
Run the container with your iRODS server credentials passed via environment variables:

- Option 1: To get into the interactive shell with access to the iRODS server:
```bash
docker run -it \
  -e IRODS_HOST=irods.ies.example.com \
  -e IRODS_USER_NAME=your_username \
  -e IRODS_ZONE_NAME=your_zone \
  irods-client-slim
```

- Option 2: To start the REST API - the same command as before with the argument **api**

```bash
docker run -it \
  -e IRODS_HOST=irods.ies.example.com \
  -e IRODS_USER_NAME=your_username \
  -e IRODS_ZONE_NAME=your_zone \
  irods-client-slim api
```

### Authenticate

Inside the container, run:

```bash
iinit
```

### Example iRODS commands
```bash
ils             # List files and directories in current iRODS directory
iput file.txt   # Upload a file to iRODS
iget file.txt   # Download a file from iRODS
icd /path       # Change directory in iRODS
```

### Optional: Mount a local directory
To easily upload or download files between your host and the container, mount a local directory:

```bash
docker run -it \
  -v $PWD:/data \
  -e IRODS_HOST=your.irods.server \
  -e IRODS_USER_NAME=your_username \
  -e IRODS_ZONE_NAME=your_zone \
  irods-client-slim
```

Inside the container:

```bash
cd /data
iput your_local_file.txt 
```