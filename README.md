# whereabouts-api


### To build:

```bash
./gradlew build
```

#### To run:
This service requires oauth and elite2 to work. Bootstrap these services by running. 
```bash
docker-compose up
```
Then run:
```bash
./gradlew bootRun
```
 

#### Health

- `/ping`: will respond `pong` to all requests.  This should be used by dependent systems to check connectivity to whereabouts,
rather than calling the `/health` endpoint.
- `/health`: provides information about the application health and its dependencies.  This should only be used
by whereabouts health monitoring (e.g. pager duty) and not other systems who wish to find out the state of whereabouts.
- `/info`: provides information about the version of deployed application.

