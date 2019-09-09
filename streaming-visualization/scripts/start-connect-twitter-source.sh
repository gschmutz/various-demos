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
  	"twitter.oauth.consumerKey": "3fDCg0uZhqf4LfawQ5pb4uurq",
  	"twitter.oauth.consumerSecret": "18898576-kKEEFsiwiDDt3n4uSqSMu8yd2Tbaarxyemc83alC2",
  	"twitter.oauth.accessToken": "18898576-2Qzx1PlhCL2ZkCBVZvX0epzKOSoOaZ9ABaeL7ndd5",
  	"twitter.oauth.accessTokenSecret": "eBYm8gRCZgUvpW4jcuYZcji9BRMYMVleKrUSLrpKwpakX",
  	"process.deletes": "false",
  	"filter.keywords": "#vdz19",
  	"kafka.status.topic": "tweet-raw-v1",
  	"tasks.max": "1"
	}
  }'

