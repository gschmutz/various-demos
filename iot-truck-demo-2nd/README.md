# IoT Truck Demo

## Prepare Environment

### Docker Compose

In order for Kafka to work in the Docker Compose setup below, two envrionment variables are necessary.

You can add them to /etc/environment (without export) to make them persistent:

```
export DOCKER_HOST_IP=192.168.25.136
export PUBLIC_IP=192.168.25.136
```

Add streamingplatform alias to /etc/hosts

```
192.168.25.136	streamingplatform
```

Start the environment using 

```
export SAMPLE_HOME=/mnt/hgfs/git/gschmutz/various-demos/iot-truck-demo
cd $SAMPLE_HOME/docker
```

Start Docker Compose environemnt

```
docker-compose up -d
```

Show logs

```
docker-compose logs -f
```

the following user interfaces are available:

 * Confluent Control Center: <http://streamingplatform:9021>
 * Kafka Manager: <http://streamingplatform:9000> 
 * Streamsets: <http://streamingplatform:18630>


### Creating Kafka Topics

Connect to docker container (broker-1)

```
docker exec -ti docker_broker-1_1 bash
```

list topics and create an new topic

```
kafka-topics --zookeeper zookeeper:2181 --list
kafka-topics --zookeeper zookeeper:2181 --create --topic truck_position --partitions 8 --replication-factor 2
kafka-topics --zookeeper zookeeper:2181 --create --topic truck_driving_info --partitions 8 --replication-factor 2
kafka-topics --zookeeper zookeeper:2181 --create --topic dangerous_driving_and_driver --partitions 8 --replication-factor 2


kafka-topics --zookeeper zookeeper:2181 --create --topic truck_driver --partitions 8 --replication-factor 2 --config cleanup.policy=compact --config segment.ms=100 --config delete.retention.ms=100 --config min.cleanable.dirty.ratio=0.001
```
### Prepare Database Table

```
docker exec -ti docker_db_1 bash
psql -d sample -U sample
```

```
DROP TABLE driver;
CREATE TABLE driver (id BIGINT, first_name CHARACTER VARYING(45), last_name CHARACTER VARYING(45), available CHARACTER VARYING(1), birthdate DATE, last_update TIMESTAMP);
ALTER TABLE driver ADD CONSTRAINT driver_pk PRIMARY KEY (id);
```

```
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (10,'Diann', 'Butler', 'Y', '10-JUN-68', CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (11,'Micky', 'Isaacson', 'Y', '31-AUG-72' ,CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (12,'Laurence', 'Lindsey', 'Y', '19-MAY-78' ,CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (13,'Pam', 'Harrington', 'Y','10-JUN-68' ,CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (14,'Brooke', 'Ferguson', 'Y','10-DEC-66' ,CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (15,'Clint','Hudson', 'Y','5-JUN-75' ,CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (16,'Ben','Simpson', 'Y','11-SEP-74' ,CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (17,'Frank','Bishop', 'Y','3-OCT-60' ,CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (18,'Trevor','Hines', 'Y','23-FEB-78' ,CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (19,'Christy','Stephens', 'Y','11-JAN-73' ,CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (20,'Clarence','Lamb', 'Y','15-NOV-77' ,CURRENT_TIMESTAMP);

INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (21,'Lila', 'Page', 'Y', '5-APR-77', CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (22,'Patricia', 'Coleman', 'Y', '11-AUG-80' ,CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (23,'Jeremy', 'Olson', 'Y', '13-JUN-82', CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (24,'Walter', 'Ward', 'Y', '24-JUL-85', CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (25,'Kristen', ' Patterson', 'Y', '14-JUN-73', CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (26,'Jacquelyn', 'Fletcher', 'Y', '24-AUG-85', CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (27,'Walter', '  Leonard', 'Y', '12-SEP-88', CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (28,'Della', ' Mcdonald', 'Y', '24-JUL-79', CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (29,'Leah', 'Sutton', 'Y', '12-JUL-75', CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (30,'Larry', 'Jensen', 'Y', '14-AUG-83', CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (31,'Rosemarie', 'Ruiz', 'Y', '22-SEP-80', CURRENT_TIMESTAMP);
INSERT INTO "driver" ("id", "first_name", "last_name", "available", "birthdate", "last_update") VALUES (32,'Shaun', ' Marshall', 'Y', '22-JAN-85', CURRENT_TIMESTAMP);
```
## Truck Simulator

### Producing to Kafka

Start the kafka console consumer on the Kafka topic `truck_position` and another on `truck_driving_info`:
 
```
docker exec -ti docker_broker-1_1 bash
```

```
kafka-console-consumer --bootstrap-server broker-1:9092 --topic truck_position
kafka-console-consumer --bootstrap-server broker-1:9092 --topic truck_driving_info
```

or by using kafkacat:

```
kafkacat -b streamingplatform:9092 -t truck_position
kafkacat -b streamingplatform:9092 -t truck_driving_info
```

Produce the IoT Truck events to topic `truck_position` and `truck_driving_info`.

```
cd $SAMPLE_HOME/../iot-truck-simulator
```

```
mvn exec:java -Dexec.args="-s KAFKA -f JSON -m SPLIT -t sec -b localhost -p 9092"
```

### Producing to MQTT

```
cd $SAMPLE_HOME/../iot-truck-simulator
```

To produce to 2 separate topics in MQTT

```
mvn exec:java -Dexec.args="-s MQTT -f JSON -p 1883 -m SPLIT -t millisec"
```

in MQTT.fx suscribe to `truck/+/position` and `truck/+/drving-info`

## MQTT to Kafa using Kafka Connect

First let's listen on the two topcis: 

```
kafkacat -b streamingplatform:9092 -t truck_position
kafkacat -b streamingplatform:9092 -t truck_driving_info
```

Add and start the MQTT connector (make sure that consumer is still running):

```
cd $SAMPLE_HOME/docker
./configure-connect-mqtt.sh
```

Navigate to the [Kafka Connect UI](http://streamingplatform:8003) to see the connector configured and running.

You can remove the connector using the following command

```
curl -X "DELETE" "$DOCKER_HOST_IP:8083/connectors/mqtt-source"
```

## MQTT to Kafa using Confluent MQTT Proxy

Make sure that the MQTT proxy has been started as a service in the `docker-compose.yml`.

```
  mqtt-proxy:
    image: confluentinc/cp-kafka-mqtt:5.0.0
    hostname: mqtt-proxy
    ports:
      - "1884:1884"
    environment:
      KAFKA_MQTT_TOPIC_REGEX_LIST: 'truck_position:.*position, truck_driving_info:.*driving-info'
      KAFKA_MQTT_LISTENERS: 0.0.0.0:1884
      KAFKA_MQTT_BOOTSTRAP_SERVERS: PLAINTEXT://broker-1:9092,broker-2:9093
      KAFKA_MQTT_CONFLUENT_TOPIC_REPLICATIN_FACTOR: 1
```

Change the truck simulator to produce on port 1884, which is the one the MQTT proxy listens on.

```
mvn exec:java -Dexec.args="-s MQTT -f JSON -p 1884 -m SPLIT -t millisec"
```

## MQTT to Kafa using StreamSets Data Collector (todo)


## Using KSQL for Stream Analytics

connect to KSQL CLI

```
cd $SAMPLE_HOME/docker

docker-compose exec ksql-cli ksql http://ksql-server:8088
```


```
show topics;
```

```
print 'truck_position';
print 'truck_driving_info';
```

```
print 'truck_position' from beginning;
print 'truck_driving_info' from beginning;
```

```
show streams;
show tables;
show queries;
```

## Streaming Query
```
DROP STREAM IF EXISTS truck_driving_info_s;

CREATE STREAM truck_driving_info_s \
  (timestamp VARCHAR, \
   truckId VARCHAR, \
   driverId BIGINT, \
   routeId BIGINT, \
   eventType VARCHAR, \
   correlationId VARCHAR) \
  WITH (kafka_topic='truck_driving_info', \
        value_format='JSON');
```

Get info on the stream

```
DESCRIBE truck_driving_info_s;
DESCRIBE EXTENDED truck_driving_info_s;
```

```
SELECT * FROM truck_driving_info_s;
```

```
ksql> SELECT * from truck_driving_info_s;
1537349668679 | 84 | 1537349668598 | 84 | 11 | 1565885487 | Normal | -6815250318731517092
1537349668800 | 48 | 1537349668685 | 48 | 14 | 1390372503 | Normal | -6815250318731517092
1537349668827 | 108 | 1537349668807 | 108 | 28 | 137128276 | Normal | -6815250318731517092
1537349668846 | 78 | 1537349668834 | 78 | 30 | 1594289134 | Normal | -6815250318731517092
1537349668895 | 97 | 1537349668854 | 97 | 19 | 927636994 | Normal | -6815250318731517092
1537349669104 | 19 | 1537349668905 | 19 | 26 | 1090292248 | Normal | -6815250318731517092
```

## Streaming Filter with KSQL

Now let's filter on all the info messages, where the `eventType` is not normal:

```
SELECT * FROM truck_driving_info_s WHERE eventType != 'Normal';
```

Let's provide the data as a topic:

create a topic where all "dangerous driving" events should be sent to
	
```
cd $SAMPLE_HOME/docker
docker exec -ti docker_broker-1_1 bash

kafka-topics --zookeeper zookeeper:2181 --create --topic dangerous_driving --partitions 8 --replication-factor 2
```

listen on the topic

```
kafka-console-consumer --bootstrap-server broker-1:9092 --topic dangerous_driving
```

```
DROP STREAM dangerous_driving_s;
CREATE STREAM dangerous_driving_s \
  WITH (kafka_topic='dangerous_driving', \
        value_format='DELIMITED', \
        partitions=8) \
AS SELECT * FROM truck_driving_info_s \
WHERE eventType != 'Normal';
```

```
SELECT * FROM dangerous_driving_s;
```

## Aggregations using KSQL

DROP TABLE dangerous_driving_count;

```
CREATE TABLE dangerous_driving_count \
AS SELECT eventType, count(*) nof \
FROM dangerous_driving_s \
WINDOW TUMBLING (SIZE 30 SECONDS) \
GROUP BY eventType;
```

```
SELECT  TIMESTAMPTOSTRING(ROWTIME, 'yyyy-MM-dd HH:mm:ss.SSS'), eventType, nof \
FROM dangerous_driving_count;
```

```
CREATE TABLE dangerous_driving_count
AS
SELECT eventType, count(*) \
FROM dangerous_driving_s \
WINDOW HOPPING (SIZE 30 SECONDS, ADVANCE BY 10 SECONDS) \
GROUP BY eventType;
```

## Join with Static Driver Data

first start the console consumer on the `trucking_driver` topic:

```
docker exec -ti docker_broker-1_1 bash
kafka-console-consumer --bootstrap-server broker-1:9092 --topic truck_driver --from-beginning
```

Print the key and value of the truck_driver topic

```
kafkacat -b streamingplatform -t truck_driver -f "%k::%s\n" -u -q
```

then start the JDBC connector:

```
cd $SAMPLE_HOME/docker
./configure-connect-jdbc.sh
```

To stop the connector execute the following command

```
curl -X "DELETE" "http://$DOCKER_HOST_IP:8083/connectors/jdbc-driver-source"
```

Perform an update to see that these will be delivered

```
docker exec -ti docker_db_1 bash

psql -d sample -U sample
```

```
UPDATE "driver" SET "available" = 'N', "last_update" = CURRENT_TIMESTAMP  WHERE "id" = 21;
```

```
UPDATE "driver" SET "available" = 'N', "last_update" = CURRENT_TIMESTAMP  WHERE "id" = 14;
```

Stop the consumer and restart with `--from-beginning` option

```
docker exec -ti docker_broker-1_1 bash
kafka-console-consumer --bootstrap-server broker-1:9092 --topic trucking_driver --from-beginning
```


### Create a KSQL table

```
docker-compose exec ksql-cli ksql http://ksql-server:8088
```

```
set 'commit.interval.ms'='5000';
set 'cache.max.bytes.buffering'='10000000';
set 'auto.offset.reset'='earliest';

DROP TABLE driver_t;
CREATE TABLE driver_t  \
   (id BIGINT,  \
   first_name VARCHAR, \
   last_name VARCHAR, \
   available VARCHAR, \
   birthdate VARCHAR) \
  WITH (kafka_topic='truck_driver', \
        value_format='JSON', \
        KEY = 'id');
```

```
SELECT * FROM driver_t;
```

join `dangerous_driving_s` stream to `driver_t` table

```
SELECT driverid, first_name, last_name, truckId, routeId, eventType \
FROM dangerous_driving_s \
LEFT JOIN driver_t \
ON dangerous_driving_s.driverId = driver_t.id;
```

with outer join

```
SELECT driverid, first_name, last_name, truckId, routeId, eventType \
FROM dangerous_driving_s \
LEFT OUTER JOIN driver_t \
ON dangerous_driving_s.driverId = driver_t.id;
```

Create a Stream with the joined information

```
docker exec -ti docker_broker-1_1 bash
kafka-console-consumer --bootstrap-server broker-1:9092 --topic dangerous_driving_and_driver --from-beginning
```

dangerous_driving_and_driver

```
DROP STREAM dangerous_driving_and_driver_s;
CREATE STREAM dangerous_driving_and_driver_s  \
  WITH (kafka_topic='dangerous_driving_and_driver', \
        value_format='JSON', partitions=8) \
AS SELECT driverid, first_name, last_name, truckId, routeId ,eventType \
FROM dangerous_driving_s \
LEFT JOIN driver_t \
ON dangerous_driving_s.driverId = driver_t.id;
```


```
SELECT * FROM dangerous_driving_and_driver_s;
```

```
SELECT * FROM dangerous_driving_and_driver_s WHERE driverid = 11;
```

```
DROP STREAM truck_position_s;

CREATE STREAM truck_position_s \
  (timestamp VARCHAR, \
   truckId VARCHAR, \
   latitude DOUBLE, \
   longitude DOUBLE) \
  WITH (kafka_topic='truck_position', \
        value_format='JSON');
```

## Stream to Stream Join

```
SELECT ddad.driverid, ddad.first_name, ddad.last_name, ddad.truckid, ddad.routeid, ddad.eventtype, tp.latitude, tp.longitude \
FROM dangerous_driving_and_driver_s ddad \
INNER JOIN truck_position_s tp \
WITHIN 2 second \
ON tp.truckid = ddad.truckid;
```

```
SELECT ddad.driverid, ddad.first_name, ddad.last_name, ddad.truckid, ddad.routeid, ddad.eventtype, geohash(tp.latitude, tp.longitude, 5) \
FROM dangerous_driving_and_driver_s ddad \
INNER JOIN truck_position_s tp \
WITHIN 2 second \
ON tp.truckid = ddad.truckid;
```
## GeoHash and Aggregation

```
DROP STREAM dangerous_and_position_s;
CREATE STREAM dangerous_and_position_s \
  WITH (kafka_topic='dangerous_and_position', \
        value_format='JSON', partitions=8) \
AS SELECT ddad.driverid, ddad.first_name, ddad.last_name, ddad.truckid, ddad.routeid, ddad.eventtype, geohash(tp.latitude, tp.longitude, 4) as geohash \
FROM dangerous_driving_and_driver_s ddad \
INNER JOIN truck_position_s tp \
WITHIN 2 second \
ON tp.truckid = ddad.truckid;
```

```
SELECT eventType, geohash, count(*) nof \
FROM dangerous_and_position_s \
WINDOW TUMBLING (SIZE 30 SECONDS) \
GROUP BY eventType, geohash;
```


## Current Positions

CREATE TABLE truck_position_t \
  WITH (kafka_topic='truck_position_t', \
        value_format='JSON', \
        KEY = 'truckid') \
AS SELECT truck_id,  FROM truck_position_s GROUP BY truckid; 



## More complex analytics in KSQL

```
CREATE TABLE dangerous_driving_count \
AS SELECT eventType, count(*) \
FROM dangerous_driving_and_driver_s \
WINDOW TUMBLING (SIZE 30 SECONDS) \
GROUP BY eventType;
```

```
CREATE TABLE dangerous_driving_count
AS
SELECT eventType, count(*) \
FROM dangerous_driving_and_driver_s \
WINDOW HOPPING (SIZE 30 SECONDS, ADVANCE BY 10 SECONDS) \
GROUP BY eventType;
```

```
SELECT first_name, last_name, eventType, count(*) \
FROM dangerous_driving_and_driver_s \
WINDOW TUMBLING (SIZE 20 SECONDS) \
GROUP BY first_name, last_name, eventType;
```



## Using Kafka Streams to detect danagerous driving

```
docker exec -ti docker_broker-1_1 bash
```

```
kafka-topics --zookeeper zookeeper:2181 --create --topic dangerous_driving --partitions 8 --replication-factor 2
kafka-console-consumer --bootstrap-server broker-1:9092 --topic dangerous_driving
```

```
cd $SAMPLE_HOME/src/kafka-streams-truck
mvn exec:java
```
