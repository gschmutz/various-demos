# IoT Streaming Data Intgestion Demo

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

kafka-topics --zookeeper zookeeper:2181 --create --topic truck_driver --partitions 8 --replication-factor 2 --config cleanup.policy=compact --config segment.ms=100 --config delete.retention.ms=100 --config min.cleanable.dirty.ratio=0.001
```

## Truck Client

### Producing to Kafka

Produce the IoT Truck events to topic `truck_position` and `truck_driving_info`.

```
cd $SAMPLE_HOME/src/truck-client
mvn exec:java -Dexec.args="-s KAFKA -f JSON -m COMBINE"
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
kafkacat -b streamingplatform:9092 -t truck_driving_info
```

### Producing to MQTT

```
cd $SAMPLE_HOME/src/truck-client
mvn exec:java -Dexec.args="-s MQTT -f JSON -p 1883 -m COMBINE -t millisec"
```

in MQTT.fx suscribe to `truck/+/position` 

## Apache NiFi

```
http://streamingplatform:28080/nifi
```

Broker URI
tcp://192.168.69.136:1883

ClientID
consumer1

Topic Filter
truck/+/position

Message Size
1000

Kafka

Kafka Brokers
192.168.69.135:9092

Topic Name
truck_position

Masking wit ReplaceText Processor

Search Value
driverId":(.*?),

Replacement Value
driverId":NN,

## StreamSets Data Collector

Navigate to <http://streamingplatform:18630/>

Broker URL: tcp://mosquitto:1883
Topic Filter: truck/+/position


Schema Registry URI: http://schema-registry:8081
Schema ID: truck-position-value

Schema Registry UI:

http://192.168.69.136:8002


Register new schema

```
curl -vs --stderr - -XPOST -i -H "Content-Type: application/vnd.schemaregistry.v1+json" --data '{"schema":"{\"type\":\"record\",\"name\":\"truckMovement\",\"namespace\":\"com.landoop\",\"doc\":\"This is an Avro schema for Truck Movements\",\"fields\":[{\"name\":\"truckid\",\"type\":\"string\"},{\"name\":\"driverid\",\"type\":\"string\"},{\"name\":\"eventtype\",\"type\":\"string\"},{\"name\":\"latitude\",\"type\":\"string\"},{\"name\":\"longitude\",\"type\":\"string\"}]}"}' http://streamingplatform:8081/subjects/truck-position-value/versions

{
  "type": "record",
  "name": "truckMovement",
  "namespace": "com.trivadis.truck",
  "doc": "This is an Avro schema for Truck Movements",
  "fields": [
    {
      "name": "truckid",
      "type": "string"
    },
    {
      "name": "driverid",
      "type": "string"
    },
    {
      "name": "eventtype",
      "type": "string"
    },
    {
      "name": "latitude",
      "type": "string"
    },
    {
      "name": "longitude",
      "type": "string"
    }
  ]
}
```

## Kafka Connect

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

