# Stream Processing Demo

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
export SAMPLE_HOME=/mnt/hgfs/git/gschmutz/various-demos/nloug-tech-2018-kafka-streaming-platform
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


### Creating Kafka Topics

Connect to docker container (broker-1)

```
docker exec -ti docker_broker-1_1 bash
```

list topics and create an new topic

```
kafka-topics --zookeeper zookeeper:2181 --list
kafka-topics --zookeeper zookeeper:2181 --create --topic truck_position --partitions 8 --replication-factor 2

kafka-topics --zookeeper zookeeper:2181 --create --topic trucking_driver --partitions 8 --replication-factor 2 --config cleanup.policy=compact --config segment.ms=100 --config delete.retention.ms=100 --config min.cleanable.dirty.ratio=0.001
```

## Truck Client

### Producing to Kafka


```
cd $SAMPLE_HOME/src/truck-client
mvn exec:java -Dexec.args="-s KAFKA -f CSV"
```

First start the kafka-console-consumer on the Kafka topic `truck_position`:

```
docker exec -ti docker_broker-1_1 bash
```
 
```
kafka-console-consumer --bootstrap-server broker-1:9092 --topic truck_position
```

```
kafkacat -b streamingplatform:9092 -t truck_position
```

```
kafkacat -b streamingplatform -t truck_position -f 'Part-%p => %k:%s\n'
```

### Producing to MQTT

```
cd $SAMPLE_HOME/src/truck-client
mvn exec:java -Dexec.args="-s MQTT -f CSV -p 1883"
```

in MQTT.fx suscribe to `truck/+/position`

## Kafka Connect

Add and start the MQTT connector (make sure that consumer is still running):

```
cd $SAMPLE_HOME/docker
./configure-connect-mqtt.sh
```

## Streaming Filter with KSQL


```
cd $SAMPLE_HOME/docker
docker exec -ti docker_broker-1_1 bash
```

create a topic where all "dangerous driving" events should be sent to

```
kafka-topics --zookeeper zookeeper:2181 --create --topic dangerous_driving_ksql --partitions 8 --replication-factor 2
```

listen on the topic

```
kafka-console-consumer --bootstrap-server broker-1:9092 --topic dangerous_driving_ksql
```

connect to KSQL CLI

```
docker-compose exec ksql-cli ksql http://ksql-server:8088
```


```
show topics;
show streams;
show tables;
```

```
DROP STREAM truck_position_s;

CREATE STREAM truck_position_s \
  (ts VARCHAR, \
   truckId VARCHAR, \
   driverId BIGINT, \
   routeId BIGINT, \
   eventType VARCHAR, \
   latitude DOUBLE, \
   longitude DOUBLE, \
   correlationId VARCHAR) \
  WITH (kafka_topic='truck_position', \
        value_format='DELIMITED');

SELECT * FROM truck_position_s;
SELECT * FROM truck_position_s WHERE eventType != 'Normal';
```

```
DROP STREAM dangerous_driving_s;
CREATE STREAM dangerous_driving_s \
  WITH (kafka_topic='dangerous_driving_ksql', \
        value_format='DELIMITED', \
        partitions=8) \
AS SELECT * FROM truck_position_s \
WHERE eventType != 'Normal';
```

```
DESCRIBE dangerous_driving_s;        
```

```
SELECT * FROM dangerous_driving_s;
```

## Static Driver Data

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

### Kafka JDBC Connector

first start the console consumer on the `trucking_driver` topic:

```
docker exec -ti docker_broker-1_1 bash
kafka-console-consumer --bootstrap-server broker-1:9092 --topic trucking_driver --from-beginning
```

then start the JDBC connector:

```
cd $SAMPLE_HOME/docker
./configure-connect-jdbc.sh
```

Perform an update to see that these will be delivered

```
docker exec -ti docker_db_1 bash
psql -d sample -U sample
```

```
UPDATE "driver" SET "available" = 'N', "last_update" = CURRENT_TIMESTAMP  WHERE "id" = 21;```
```

```
UPDATE "driver" SET "available" = 'N', "last_update" = CURRENT_TIMESTAMP  WHERE "id" = 14;
```

Stop the consumer and restart with `--from-beginning` option

```
docker exec -ti docker_broker-1_1 bash
kafka-console-consumer --bootstrap-server broker-1:9092 --topic trucking_driver --from-beginning
```


#

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
  WITH (kafka_topic='trucking_driver', \
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

```
--Join using a STREAM
DROP STREAM dangerous_driving_and_driver_s;
CREATE STREAM dangerous_driving_and_driver_s  \
  WITH (kafka_topic='dangerous_driving_and_driver_s', \
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

## More complex analytics in KSQL

```
select eventType, count(*) from dangerous_driving_and_driver_s window tumbling (size 10 seconds) group by eventType;
```

```
select eventType, count(*) from dangerous_driving_and_driver_s window hopping (size 30 seconds, advance by 10 seconds) group by eventType;
```

```
select first_name, last_name, eventType, count(*) from dangerous_driving_and_driver_s window tumbling (size 20 seconds) group by first_name, last_name, eventType;
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
