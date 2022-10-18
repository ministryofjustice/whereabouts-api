#!/usr/bin/env bash
set -e
aws --endpoint-url=http://localhost:4566 sns create-topic --name offender_events
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name whereabouts_api_dlq
aws --endpoint-url=http://localhost:4566 sqs create-queue --queue-name whereabouts_api_queue
aws --endpoint-url=http://localhost:4566 sns subscribe \
    --topic-arn arn:aws:sns:eu-west-2:000000000000:offender_events \
    --protocol sqs \
    --notification-endpoint http://localhost:4566/queue/whereabouts_api_queue \
    --attributes '{"FilterPolicy":"{\"eventType\":[\"DATA_COMPLIANCE_DELETE-OFFENDER\", \"APPOINTMENT_CHANGED\"]}"}'
