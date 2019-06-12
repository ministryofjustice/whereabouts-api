# whereabouts-api


### To build:

```bash
./gradlew build
```

#### Running against local postgres docker:
Run the postgres docker image:
```bash
docker run --name=whereabouts-api-postgres -e POSTGRES_PASSWORD=password -p5432:5432 -d postgres
```
Run spring boot with the the postgres spring profile

#### Health

- `/ping`: will respond `pong` to all requests.  This should be used by dependent systems to check connectivity to whereabouts,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by whereabouts health monitoring (e.g. pager duty) and not other systems who wish to find out the state of whereabouts.
- `/info`: provides information about the version of deployed application.

