#!/bin/bash

set -e

# Server port - avoid clash with prison-api
export SERVER_PORT=8089

export OAUTH_CLIENT_ID=$(kubectl -n whereabouts-api-dev get secrets whereabouts-api -o json  | jq -r '.data.OAUTH_CLIENT_ID | @base64d')
export OAUTH_CLIENT_SECRET=$(kubectl -n whereabouts-api-dev get secrets whereabouts-api -o json  | jq -r '.data.OAUTH_CLIENT_SECRET | @base64d')

export OAUTH_ENDPOINT_URL=https://sign-in-dev.hmpps.service.justice.gov.uk/auth
export ELITE2API_ENDPOINT_URL=https://prison-api-dev.prison.service.justice.gov.uk
export CASENOTES_ENDPOINT_URL=https://dev.offender-case-notes.service.justice.gov.uk

export SPRING_SECURITY_OAUTH2_RESOURCESERVER_JWT_JWK_SET_URI=https://sign-in-dev.hmpps.service.justice.gov.uk/auth/.well-known/jwks.json

# Stop the back end containers
echo "Bringing down current containers ..."
docker compose down --remove-orphans

#Prune existing containers
#Comment in if you wish to perform a fresh install of all containers where all containers are removed and deleted
#You will be prompted to continue with the deletion in the terminal
#docker system prune --all

echo "Pulling back end containers ..."
docker-compose -f docker-compose-localstack.yaml pull
rm -rf /tmp/localstack && docker-compose -f docker-compose-localstack.yaml down && TMPDIR=/private$TMPDIR docker-compose -f docker-compose-localstack.yaml up -d

echo "Waiting for back end containers to be ready ..."
until [ "$(docker inspect -f \{\{.State.Health.Status\}\} localstack-whereabouts-api)" == "healthy" ]; do
    sleep 0.1;
done;

echo "Localstack now ready"

# Run the application with stdout and dev profiles active
echo "Starting the API locally"
SPRING_PROFILES_ACTIVE=stdout,dev,localstack ./gradlew bootRun
