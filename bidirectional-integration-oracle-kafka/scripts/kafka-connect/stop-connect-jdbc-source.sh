#!/bin/bash

echo "removing JDBC Source Connector"

curl -X "DELETE" "http://$DOCKER_HOST_IP:8083/connectors/jdbc-orderprocessing-source"

