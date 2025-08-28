#!/bin/bash

if [ $# != 2 ]; then
  echo "USAGE: ./run-tests.sh <service> <client-secret>"
  exit 1
fi

api_container_id=$(docker ps | grep api | awk '{print $1}')
if [ -n "$api_container_id" ]; then
  echo "Stopping previous api-service"
  docker stop $api_container_id
fi

echo "Start api-service"
docker build --build-arg APP_VERSION=0.0.1 -t api-service ../api-service/
docker run --rm -it \
 -e IRODS_HOST=qa-ies.cyverse.at -e IRODS_USER_NAME=portal -e IRODS_ZONE_NAME=TUG -e AUTH_SECRET=test_secret -p 7000:7000 -d api-service api

docker compose down
docker compose up -d --build

db_container_id=$(docker ps | grep db | awk '{print $1}')

if [ -z "$db_container_id" ]; then
  echo "Containers did not start correctly."
  exit 1
fi

until docker exec -i $db_container_id psql -U postgres -c '\l' > /dev/null 2>&1; do
  echo "Waiting for PostgreSQL to be ready..."
  sleep 1
done

echo "Creating Keycloak DB"
docker exec -i $db_container_id psql -U postgres -c "create user keycloak with password 'keycloak';"
docker exec -i $db_container_id psql -U postgres -c "create database keycloak with owner keycloak;"
docker exec -i $db_container_id psql -U postgres -c "grant postgres to keycloak;"

echo "Waiting for Keycloak to initialize"

for i in {1..30}; do
  echo -n "."
  sleep 1
done

mvn clean package


if [ "$1" = "portal" ]; then

  echo "Creating Keycloak setup: realm, client, users."
  java -jar target/register-test.jar ./src/main/resources/test-config.yml portal 3000 $2

  docker exec -i $db_container_id psql -U postgres -c "create user portal_db_reader with password 'admin';"
  docker exec -i $db_container_id psql -U postgres -c "create database portal with owner portal_db_reader;"
  docker exec -i $db_container_id psql -U postgres -c "grant postgres to portal_db_reader;"

  cd ../../portal2
  docker run --rm \
    -v ./migrations:/migrations \
    --network host \
    migrate/migrate \
    --database "postgres://portal_db_reader:admin@localhost/portal?sslmode=disable" \
    -path /migrations \
    up

  echo "Importing portal institutions"
  cd ./src/scripts
  rm -rf .venv
  mkdir .venv
  uv venv .venv
  source .venv/bin/activate
  uv pip install psycopg2-binary
  python import_grid_institutions.py --host localhost --user portal_db_reader --database portal grid.csv
  deactivate

  echo "Importing portal metadata"
  cd ../../../portal2-db
  cd sqls
  for file in ./*.sql; do
    docker exec -i $db_container_id psql -U portal_db_reader -d portal < $file
  done

  cd ../../portal2

  echo "Will start portal app. To finish the setup, please register users with usernames: test_user1, test_user2"
  echo "Please use this script with care. Calling it again will drop all changes and start the setup from scratch."
  echo "If you stop this run, then go to portal2 directory. There run <npm run dev>."
  npm run dev
fi
# TODO Add discovery-environment workflow?
