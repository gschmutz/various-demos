# product-soaring-clouds-sequel

docker exec -ti schema_registry bash

kafka-avro-console-consumer --bootstrap-server 129.150.77.116:6667 --topic a516817-soaring-products --property schema.registry.url=http://129.150.114.134:8081
kafka-avro-console-consumer --bootstrap-server 129.150.77.116:6667 --topic a516817-soaring-customers --property schema.registry.url=http://129.150.114.134:8081


Get the messages from the shopping cart

kafka-avro-console-consumer --bootstrap-server 129.150.114.134:9092 --topic a516817-soaring-add-to-shopping-cart

kafka-avro-console-consumer --bootstrap-server broker-1:9092 --topic a516817-soaring-add-to-shopping-cart


kafka-console-consumer --bootstrap-server 129.150.114.134:9092 --topic a516817-soaring-products 


kafka-console-consumer --bootstrap-server 129.150.77.116:6667 --topic a516817-soaring-products 