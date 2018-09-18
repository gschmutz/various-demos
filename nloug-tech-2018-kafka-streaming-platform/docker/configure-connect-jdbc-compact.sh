#!/bin/bash
curl -X "DELETE" "http://192.168.69.134:8083/connectors/jdbc-driver-source2"

## Request
curl -X "POST" "http://192.168.69.134:8083/connectors" \
     -H "Content-Type: application/json" \
     -d $'{
  "name": "jdbc-driver-source2",
  "config": {
    "connector.class": "JdbcSourceConnector",
    "tasks.max": "1",
    "connection.url":"jdbc:postgresql://db/sample?user=sample&password=sample",
    "mode": "timestamp",
    "timestamp.column.name":"last_update",
    "table.whitelist":"driver",
    "validate.non.null":"false",
    "topic.prefix":"trucking_compact_",
    "key.converter":"org.apache.kafka.connect.json.JsonConverter",
    "key.converter.schemas.enable": "false",
    "value.converter":"org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "name": "jdbc-driver-source2",
     "transforms":"createKey,extractInt",
     "transforms.createKey.type":"org.apache.kafka.connect.transforms.ValueToKey",
     "transforms.createKey.fields":"id",
     "transforms.extractInt.type":"org.apache.kafka.connect.transforms.ExtractField$Key",
     "transforms.extractInt.field":"id"
  }
}'
