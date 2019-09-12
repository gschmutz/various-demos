#!/bin/bash

echo "creating JMS Source Connector"

curl -X "POST" "$DOCKER_HOST_IP:8083/connectors" \
     -H "Content-Type: application/json" \
     --data '{
    "name": "jms-source",
    "config": {
        "name": "jms-source",
        "connector.class": "com.datamountaineer.streamreactor.connect.jms.source.JMSSourceConnector",
        "connect.jms.initial.context.factory": "oracle.jms.AQjmsInitialContextFactory",
        "connect.jms.initial.context.extra.params": "db_url=jdbc:oracle:thin:@//192.168.73.86:1521/XEPDB1,java.naming.security.principal=order_processing,java.naming.security.credentials=order_processing",
        "tasks.max": "1",
        "connect.jms.connection.factory": "ConnectionFactory",
        "connect.jms.url": "jdbc:oracle:thin:@//192.168.73.86:1521/XEPDB1",
        "connect.jms.kcql": "INSERT INTO order SELECT * FROM order_aq WITHTYPE QUEUE WITHCONVERTER=`com.datamountaineer.streamreactor.connect.converters.source.JsonSimpleConverter`"
    }
}'

