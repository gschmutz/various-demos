#!/bin/bash

echo "removing Twitter Source Connectors"

curl -X "DELETE" "$DOCKER_HOST_IP:8083/connectors/twitter-source"

