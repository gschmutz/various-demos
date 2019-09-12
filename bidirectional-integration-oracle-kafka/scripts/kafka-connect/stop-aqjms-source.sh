#!/bin/bash

echo "removing JMS Source Connector"

curl -X "DELETE" "$DOCKER_HOST_IP:8083/connectors/jms-source"

