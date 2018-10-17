#!/bin/bash

echo "removing MQTT Source Connectors"

curl -X "DELETE" "$DOCKER_HOST_IP:8083/connectors/mqtt-position-source"

