
### Example deploy command
```
helm --namespace whereabouts-api-dev  --tiller-namespace whereabouts-api-dev upgrade whereabouts-api ./whereabouts-api/ --install --values=values-dev.yaml --values=example-secrets.yaml
```

### Rolling back a release
Find the revision number for the deployment you want to roll back:
```
helm --tiller-namespace whereabouts-api-dev history whereabouts-api -o yaml
```
(note, each revision has a description which has the app version and circleci build URL)

Rollback
```
helm --tiller-namespace whereabouts-api-dev rollback whereabouts-api [INSERT REVISION NUMBER HERE] --wait
```

### Helm init

```
helm init --tiller-namespace whereabouts-api-dev --service-account tiller --history-max 200
```

### Setup Lets Encrypt cert

Ensure the certificate definition exists in the cloud-platform-environments repo under the relevant namespaces folder

e.g.
```
cloud-platform-environments/namespaces/live-1.cloud-platform.service.justice.gov.uk/[INSERT NAMESPACE NAME]/05-certificate.yaml
```
