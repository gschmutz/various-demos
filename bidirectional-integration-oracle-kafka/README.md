# Bi-Directional Communication between Oracle RDBMS and Kafka

## Setup Oracle XE 18 database
Setup a docker 

### Setup Sample schema

```
cd /mnt/hgfs/git/gschmutz/various-demos/bidirectional-integration-oracle-kafka/scripts/oracle
``` 

```
/mnt/hgfs/Downloads/sqlcl/bin/sql sys/manager as sysdba
```

```
alter session set container= XEPDB1;
```

```
@order-processing/user/order-processing.sql
```

```
connect order_processing/order_processing@//localhost:1521/XEPDB1
```

```
@order-processing/install.sql
```



```
DECLARE
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN

    ORDS.ENABLE_SCHEMA(p_enabled => TRUE,
                       p_schema => 'ORDER_PROCESSING',
                       p_url_mapping_type => 'BASE_PATH',
                       p_url_mapping_pattern => 'order_processing',
                       p_auto_rest_auth => FALSE);

    commit;

END;
/
```

DECLARE
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN

    ORDS.ENABLE_OBJECT(p_enabled => TRUE,
                       p_schema => 'ORDER_PROCESSING',
                       p_object => 'ORDER_T',
                       p_object_type => 'TABLE',
                       p_object_alias => 'orders',
                       p_auto_rest_auth => FALSE);

    commit;

END;


### Create Kafka Topics

Create the necessary Kafka topics

```
docker exec -ti broker-1 kafka-topics --create --zookeeper zookeeper-1:2181 --topic order --replication-factor 3 --partitions 8
```

```
docker exec -ti broker-1 kafka-topics --create --zookeeper zookeeper-1:2181 --topic order-item --replication-factor 3 --partitions 8
```

```
docker exec -ti broker-1 kafka-topics --create --zookeeper zookeeper-1:2181 --topic customer --replication-factor 3 --partitions 8
```


## RDBMS => Kafka



### Kafka Connect JDBC

```
./scripts/kafka-connect/start-connect-jdbc-source.sh
```

### StreamSets REST Polling

### Kafka Connect JMS

The following connectors are available for JMS:

 * Confluent: <https://docs.confluent.io/current/connect/kafka-connect-jms/index.html> (Source & Sink)
 * Bikeholik: <https://github.com/bikeholik/jms-kafka-connector>
 * Landoop: <https://github.com/Landoop/stream-reactor/releases> (Source & Sink)

Install the Kafka JMS Connect from Landoop into the `kafka-connect folder

```
cd kafka-connect
mkdir kafka-connect-jms-1.2.1-2.1.0-all
cd kafka-connect-jms-1.2.1-2.1.0-all

wget https://github.com/Landoop/stream-reactor/releases/download/1.2.1/kafka-connect-jms-1.2.1-2.1.0-all.tar.gz

tar -xvzf kafka-connect-jms-1.2.1-2.1.0-all.tar.gz
```

Download the necessary AQ jars into the `kafka-connect-jms-1.2.1-2.1.0-all` folder

```
wget https://github.com/PhilippSalvisberg/emptracker/blob/master/lib/aqapi.jar
wget https://github.com/PhilippSalvisberg/emptracker/blob/master/lib/jmscommon.jar
wget https://github.com/PhilippSalvisberg/emptracker/blob/master/lib/ojdbc8.jar
wget https://github.com/PhilippSalvisberg/emptracker/blob/master/lib/orai18n-collation.jar
wget https://github.com/PhilippSalvisberg/emptracker/blob/master/lib/orai18n-mapping.jar
wget https://github.com/PhilippSalvisberg/emptracker/blob/master/lib/ucp.jar
```


Consume from the Kafka topic

```
docker exec -ti schema-registry kafka-avro-console-consumer --bootstrap-server broker-1:9092 --topic order 
```

	
```
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
```

Generate a message

```
DECLARE
      l_enqueue_options sys.dbms_aq.enqueue_options_t;
      l_message_props   sys.dbms_aq.message_properties_t;
      l_jms_message     sys.aq$_jms_text_message := sys.aq$_jms_text_message.construct;
      l_msgid           RAW(16);
      
      order_json CLOB;
      
		CURSOR order_sel
		IS
		SELECT json_object('orderId' VALUE po.id,
		          'orderDate' VALUE po.order_date,
		          'orderMode' VALUE po.order_mode,
		          'customer' VALUE
		              json_object('firstName' VALUE cu.first_name,
		                          'lastName' VALUE cu.last_name),
		          'lineItems' VALUE (SELECT json_arrayagg(
		              json_object('ItemNumber' VALUE li.id,
		                     'Product' VALUE
		                       json_object('productId' VALUE li.product_id,
		                                   'unitPrice' VALUE li.unit_price),
		                      'quantity' VALUE li.quantity))
		                      FROM order_item_t li WHERE po.id = li.order_id),
		         'offset' VALUE TO_CHAR(po.modified_at, 'YYYYMMDDHH24MISS'))
		FROM order_t po LEFT JOIN customer_t cu ON (po.customer_id = cu.id)
		WHERE po.modified_at > TO_DATE('20190313000000', 'YYYYMMDDHH24MISS');
      
BEGIN
	   OPEN order_sel;
		FETCH order_sel INTO order_json;
		dbms_output.put_line(order_json);


      l_jms_message.clear_properties();
      l_message_props.correlation := sys_guid;
      l_message_props.priority := 3;
      l_message_props.expiration := 5;
      l_jms_message.set_string_property('msg_type', 'test');
      l_jms_message.set_text(order_json);
      dbms_aq.enqueue(queue_name         => 'order_aq',
                      enqueue_options    => l_enqueue_options,
                      message_properties => l_message_props,
                      payload            => l_jms_message,
                      msgid              => l_msgid);
      COMMIT;
END;
```

### Notification Service
 
First let's connect to the KSQL CLI

```
docker run --rm -it --network analyticsplatform_default confluentinc/cp-ksql-cli:5.1.2 http://ksql-server-1:8088
```

First let's connect to the KSQL CLI

```
CREATE STREAM order_s WITH (KAFKA_TOPIC='order', VALUE_FORMAT='AVRO');
```

```
docker exec -ti broker-1 kafka-topics --create --zookeeper zookeeper-1:2181 --topic notify-twitter --replication-factor 3 --partitions 8
```


```
CREATE STREAM notify_twitter_s WITH (KAFKA_TOPIC='notify', VALUE_FORMAT='AVRO', PARTITIONS=8)
AS SELECT * 
FROM order_s
WHERE order_status = 1;
```


## Kafka => RDBMS

### JDBC Connector



```
"transforms": "flatten",
"transforms.flatten.type": "org.apache.kafka.connect.transforms.Flatten$Value",
"transforms.flatten.delimiter": "_"
```

### StreamSets to REST API



### Kafka Connect to AQ





