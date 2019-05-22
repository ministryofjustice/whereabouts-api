{{/* vim: set filetype=mustache: */}}
{{/*
Environment variables for web and worker containers
*/}}
{{- define "deployment.envs" }}
env:
  - name: SERVER_PORT
    value: {{ .Values.image.port }}

  - name: SPRING_PROFILES_ACTIVE
    value: "postgres"

  - name: JWT_PUBLIC_KEY 
    value: "{{ .Values.env.NOMIS_OAUTH_PUBLIC_KEY }}" 

  - name: ELITE2API_ENDPOINT_URL
    value: "{{ .Values.env.ELITE2API_ENDPOINT_URL }}"

  - name: DATABASE_URL
    valueFrom:
      secretKeyRef:
        name: dps-rds-instance-output 
        key: url
{{- end -}}
