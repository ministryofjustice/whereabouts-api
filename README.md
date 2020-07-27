# whereabouts-api

[![CircleCI](https://circleci.com/gh/ministryofjustice/whereabouts-api/tree/main.svg?style=svg)](https://circleci.com/gh/ministryofjustice/whereabouts-api)

A Spring Boot app to manage the location (whereabouts) of prisoners for the Digital Prison Services.  Backend services for https://github.com/ministryofjustice/prisonstaffhub/.

### To build:

```bash
./gradlew build
```

### To run:
This service requires oauth and elite2 to work. Bootstrap these services by running. 
```bash
docker-compose up
```
Then run:
```bash
./gradlew bootRun
```

### Health

- `/health/ping`: will respond `{"status":"UP"}` to all requests.  This should be used by dependent systems to check connectivity to whereabouts,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by whereabouts health monitoring (e.g. pager duty) and not other systems who wish to find out the state of whereabouts.
- `/info`: provides information about the version of deployed application.

### Pre Release Testing

Whereabouts api is best tested by the front end (https://whereabouts-preprod.prison.service.justice.gov.uk).  To manually smoke test / regression test whereabouts api prior to release:

1. Choose a residential wing and click continue
   1. Check sub locations are shown - selecting one should limit listed prisoners to that location
   1. Click attended - select yes reason and enter case note
   1. Click attended - select no reason and choose unacceptable so that IEP functionality triggered
1. Choose activity / appointment and click continue
   1. Click not attended - select yes reason and enter case note
1. Click [View all appointments](https://whereabouts-preprod.prison.service.justice.gov.uk/appointments) and check page loads with data
1. Click [View prisoners unaccounted for](https://whereabouts-preprod.prison.service.justice.gov.uk/manage-prisoner-whereabouts/prisoners-unaccounted-for) and check page loads with data
1. Click [View attendance reason statistics](https://whereabouts-preprod.prison.service.justice.gov.uk/manage-prisoner-whereabouts/attendance-reason-statistics) and check page loads with data (generated from yes / no above)
