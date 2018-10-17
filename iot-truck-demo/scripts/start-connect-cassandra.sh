#!/bin/bash

echo "removing Cassandra Sink Connectors"

curl -X "DELETE" "$DOCKER_HOST_IP:8083/connectors/cassandra-sink"

echo "creating Cassandra Sink Connector"

curl -X "POST" "$DOCKER_HOST_IP:8083/connectors" \
     -H "Content-Type: application/json" \
     --data '{
  "name": "cassandra-sink",
  "config": {
    "connector.class" : "io.confluent.connect.cassandra.CassandraSinkConnector",
    "tasks.max": "1",
    "topics" : "DANGEROUS_DRIVING_COUNT",
    "cassandra.contact.points" : "cassandra",
    "cassandra.keyspace" : "iot-truck"
    }
  }'
