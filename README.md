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

