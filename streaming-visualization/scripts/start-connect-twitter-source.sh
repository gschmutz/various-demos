#!/bin/bash

echo "removing Twitter Source Connectors"

curl -X "DELETE" "$DOCKER_HOST_IP:8083/connectors/twitter-source"

echo "creating Twitter Source Connector"

curl -X "POST" "$DOCKER_HOST_IP:8083/connectors" \
     -H "Content-Type: application/json" \
     --data '{
  "name": "twitter-source",
  "config": {
  	"connector.class": "com.github.jcustenborder.kafka.connect.twitter.TwitterSourceConnector",
  	"twitter.oauth.consumerKey": "xxxxx",
  	"twitter.oauth.consumerSecret": "xxxxx",
  	"twitter.oauth.accessToken": "xxxx",
  	"twitter.oauth.accessTokenSecret": "xxxxx",
  	"process.deletes": "false",
  	"filter.keywords": "#javazone,#javazone2019",
  	"filter.userIds": "15148494",
  	"kafka.status.topic": "tweet-raw-v1",
  	"tasks.max": "1"
	}
  }'

