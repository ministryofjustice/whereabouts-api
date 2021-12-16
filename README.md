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

Whereabouts api is best tested by the front end (https://digital-preprod.prison.service.justice.gov.uk).  To manually smoke test / regression test whereabouts api prior to release:

1. Choose a residential wing and click continue
   1. Check sub locations are shown - selecting one should limit listed prisoners to that location
   1. Click attended - select yes reason and enter case note
   1. Click attended - select no reason and choose unacceptable so that IEP functionality triggered
1. Choose activity / appointment and click continue
   1. Click not attended - select yes reason and enter case note
1. Click [View all appointments](https://digital-preprod.prison.service.justice.gov.uk/appointments) and check page loads with data
1. Click [View prisoners unaccounted for](https://digital-preprod.prison.service.justice.gov.uk/manage-prisoner-whereabouts/prisoners-unaccounted-for) and check page loads with data
1. Click [View attendance reason statistics](https://digital-preprod.prison.service.justice.gov.uk/manage-prisoner-whereabouts/attendance-reason-statistics) and check page loads with data (generated from yes / no above)

### Starting localstack

Localstack has been introduced for some integration tests and it is also possible to run the application against localstack.

* In the root of the project, to clear down and then bring up localstack, run:
```
rm -rf /tmp/localstack && docker-compose -f docker-compose-localstack.yaml down && TMPDIR=/private$TMPDIR docker-compose -f docker-compose-localstack.yaml up
```

* You can now use the aws CLI to send messages to the queue
* When running the service, the queue's health status should appear as a local healthcheck: http://localhost:8082/health

### Running the tests

With localstack now up and running (see previous section), run:
```bash
./gradlew test
```

### Running the service:

As well as localstack, this service also requires oauth, prison-api and the offender-case-notes services running to work. 

Bootstrap these services by running:
```bash
docker-compose up
```

To run the app, the following profiles need to be enabled: 'dev,localstack,local'
therefore with gradle, run:
```bash
./gradlew bootRun --args='--spring.profiles.active=dev,localstack,local'
```