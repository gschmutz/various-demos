#!/bin/bash

echo "removing JDBC Source Connector"

curl -X "DELETE" "http://$DOCKER_HOST_IP:8083/connectors/jdbc-driver-source"

echo "creating JDBC Source Connector"

## Request
curl -X "POST" "$DOCKER_HOST_IP:8083/connectors" \
     -H "Content-Type: application/json" \
     -d $'{
  "name": "jdbc-driver-source",
  "config": {
    "connector.class": "JdbcSourceConnector",
    "tasks.max": "1",
    "connection.url":"jdbc:postgresql://db/sample?user=sample&password=sample",
    "mode": "timestamp",
    "timestamp.column.name":"last_update",
    "table.whitelist":"driver",
    "validate.non.null":"false",
    "topic.prefix":"truck_",
    "key.converter":"org.apache.kafka.connect.storage.StringConverter",
    "key.converter.schemas.enable": "false",
    "value.converter":"org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "name": "jdbc-driver-source",
     "transforms":"createKey,extractInt",
     "transforms.createKey.type":"org.apache.kafka.connect.transforms.ValueToKey",
     "transforms.createKey.fields":"id",
     "transforms.extractInt.type":"org.apache.kafka.connect.transforms.ExtractField$Key",
     "transforms.extractInt.field":"id"
  }
}'
