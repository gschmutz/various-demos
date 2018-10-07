# Azure Event Hub as Kafka



kafka-avro-console-consumer --bootstrap-server dorstevh001.servicebus.windows.net:9093 --topic test-property security.protocol:SASL_SSL --property sasl.mechanism:PLAIN --property sasl.jaas.config:Endpoint=sb://dorstevh001.servicebus.windows.net/;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=BZfVUNfTBNmLvmi9X5M7PWXIrkUCcVmCV2hl4dt8OfI=
