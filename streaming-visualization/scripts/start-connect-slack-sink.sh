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
    "topics":"tweet-term-v1",
    "slack.token":"xxxxxxxx",
    "slack.channel":"general",
    "message.template":"tweet by ${screenname} with ${tweet}",
    "key.converter": "org.apache.kafka.connect.storage.StringConverter"
    }
  }'

