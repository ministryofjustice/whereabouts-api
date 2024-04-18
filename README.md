# whereabouts-api

[![CircleCI](https://circleci.com/gh/ministryofjustice/whereabouts-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/whereabouts-api)

A Spring Boot app to manage the location (whereabouts) of prisoners for the Digital Prison Services.  Backend services for https://github.com/ministryofjustice/prisonstaffhub/.

Swagger API documentation is available (here)[https://whereabouts-api-dev.service.justice.gov.uk/swagger-ui/index.html] 

### Code style & formatting
```bash
./gradlew ktlintApplyToIdea addKtlintFormatGitPreCommitHook
```
will apply ktlint styles to intellij and also add a pre-commit hook to format all changed kotlin files.

### Health

- `/health/ping`: will respond `{"status":"UP"}` to all requests.  This should be used by dependent systems to check connectivity to whereabouts,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by whereabouts health monitoring (e.g. pager duty) and not other systems who wish to find out the state of whereabouts.
- `/info`: provides information about the version of deployed application.

### Pre Release Testing

Whereabouts api is best tested using the DPS front end (https://dps-preprod.prison.service.justice.gov.uk).  

To manually test whereabouts API prior to release, and must be in a prison not enabled for Activities and Appointments:

1. Select the Prisoner Whereabouts -> View by Residential Location tiles 
2. Choose a residential wing and click continue
   1. Check sub locations are shown - selecting one should limit listed prisoners to that location
   1. Click attended - select yes reason and enter case note
   1. Click attended - select no reason and choose unacceptable so that IEP functionality triggered
1. Choose activity / appointment and click continue
   1. Click not attended - select yes reason and enter case note
1. Click [View all appointments](https://digital-preprod.prison.service.justice.gov.uk/appointments) and check page loads with data
1. Click [View prisoners unaccounted for](https://digital-preprod.prison.service.justice.gov.uk/manage-prisoner-whereabouts/prisoners-unaccounted-for) and check page loads with data
1. Click [View attendance reason statistics](https://digital-preprod.prison.service.justice.gov.uk/manage-prisoner-whereabouts/attendance-reason-statistics) and check page loads with data (generated from yes / no above)

### Running the tests

Localstack has been introduced and is required for some of the integration tests

* To start a localstack container for testing
```
$ docker-compose -f docker-compose-test.yaml up -d 
```
Then run the tests with
```bash
$ ./gradlew test
```
Then shutdown the localstack container
```
$ docker-compose -f docker-compose-test.yaml down 
```

### Running the service locally

As well as localstack, the service requires oauth, prison-api and the offender-case-notes services. 

Bootstrap these services by running:
```bash
$ docker-compose -f docker-compose-local.yml up -d
```
* You can now use the aws CLI to send messages to the localstack queue
* When running the service, the queue's health status should appear as a local healthcheck: http://localhost:8082/health

To run the app itself the following profiles need to be enabled: 'dev,localstack,local'
therefore with gradle, run:
```bash
$ ./gradlew bootRun --args='--spring.profiles.active=dev,localstack,local'
```