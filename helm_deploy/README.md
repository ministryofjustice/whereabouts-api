
### Example deploy command
```
helm --namespace whereabouts-api-dev  --tiller-namespace whereabouts-api-dev upgrade whereabouts-api ./whereabouts-api/ --install --values=values-dev.yaml --values=example-secrets.yaml
```

### Helm init

```
helm init --tiller-namespace whereabouts-api-dev --service-account tiller --history-max 200
```

### Setup Lets Encrypt cert

```
kubectl -n whereabouts-api-dev apply -f certificate-dev.yaml
kubectl -n whereabouts-api-dev apply -f certificate-preprod.yaml
kubectl -n whereabouts-api-dev apply -f certificate-prod.yaml
```
