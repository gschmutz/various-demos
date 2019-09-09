#!/bin/bash

echo "removing Slack Sink Connectors"

curl -X "DELETE" "$DOCKER_HOST_IP:8083/connectors/slack-sink"

echo "creating Slack Sink Connector"

curl -X "POST" "$DOCKER_HOST_IP:8083/connectors" \
     -H "Content-Type: application/json" \
     --data '{
  "name": "slack-sink",
  "config": {
    "connector.class": "net.mongey.kafka.connect.SlackSinkConnector",
    "tasks.max": "1",
    "topics":"slack-notify",
    "slack.token":"xoxp-560576135894-558395845680-560731595825-c4ad01f1301f1f789f5b916e4854d610",
    "slack.channel":"general",
    "message.template":"tweet by ${USER_SCREENNAME} with ${TEXT}",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
    }
  }'

