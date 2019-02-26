# IoT Streaming Data Ingestion Demo

## Prepare Environment

The environment we are going to use is based on docker containers. In order to easily start the multiple containers, we are going to use Docker Compose. 

### Docker Compose

For Kafka to work in this Docker Compose setup, two envrionment variables are necessary, which are configured with the IP address of the docker machine as well as the Public IP of the docker machine. 

You can add them to `/etc/environment` (without export) to make them persistent:

```
export DOCKER_HOST_IP=192.168.25.136
export PUBLIC_IP=192.168.25.136
```
Also export the local folder of this GitHub project as the SAMPLE_HOME variable. 

```
export SAMPLE_HOME=/mnt/hgfs/git/gschmutz/various-demos/iot-truck-demo
```


Add `streamingplatform` as an alias to the `/etc/hosts` file on the machine you are using to run the demo on. 

```
192.168.25.136	streamingplatform
```

Now we can start the environment. Navigate to the `docker` sub-folder inside the SAMPLE_HOME folder. 

```
cd $SAMPLE_HOME/docker
```

and start the vaious docker containers 

```
docker-compose up -d
```

to show the logs of the containers

```
docker-compose logs -f
```


### Creating Kafka Topics

The Kafka cluster is configured with `auto.topic.create.enable` set to `false`. Therefore we first have to create all the necessary topics, using the `kafka-topics` command line utility of Apache Kafka. 

We can easily get access to the `kafka-topics` CLI by navigating into one of the containers for the 3 Kafka Borkers. Let's use `broker-1`

```
docker exec -ti docker_broker-1_1 bash
```

First lets see all existing topics

```
kafka-topics --zookeeper zookeeper:2181 --list
```

And now create the topics `truck_position` and `truck_driver`.

```
kafka-topics --zookeeper zookeeper:2181 --create --topic truck_position --partitions 8 --replication-factor 2

kafka-topics --zookeeper zookeeper:2181 --create --topic truck_driver --partitions 8 --replication-factor 2 --config cleanup.policy=compact --config segment.ms=100 --config delete.retention.ms=100 --config min.cleanable.dirty.ratio=0.001
```

## Truck Client

### Producing to Kafka

Produce the IoT Truck events to topic `truck_position` and `truck_driving_info`.

In a new terminal window, move to the `truck-client` folder and start the truck simulator:

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

In a new 

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

