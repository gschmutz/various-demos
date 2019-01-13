# Event-Driven Microservices with Kafka

## Prepare Environment

### Docker Compose

In order for Kafka to work in the Docker Compose setup below, two environment variables are necessary.

You can add them to `/etc/environment` (without export) to make them persistent:

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
export SAMPLE_HOME=/mnt/hgfs/git/gschmutz/various-demos/event-driven-microservices
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

kafka-topics --zookeeper zookeeper:2181 --create --topic customer --partitions 8 --replication-factor 2 --config cleanup.policy=compact --config segment.ms=100 --config segment.bytes=1000 --config delete.retention.ms=100 --config min.cleanable.dirty.ratio=0.01

kafka-topics --zookeeper zookeeper:2181 --create --topic order --partitions 8 --replication-factor 2 --config cleanup.policy=compact --config segment.ms=100 -config segment.bytes=1000 --config delete.retention.ms=100 --config min.cleanable.dirty.ratio=0.01
```

## Register Schemas in Registry

In the meta project

```
cd $SAMPLE_HOME/java/meta
```

execute a schema-registry:register:

```
mvn schema-registry:register
```

Schema should show up in Schema Registry UI: <http://streamingplatform:8002/#/>


## Add a Customer

```
{
  "customerId" : 1,
  "firstName" : "Guido",
  "lastName" : "Schmutz",
  "title" : "Mr",
  "emailAddress" : "guido.schmutz@someemail.com",
  "addresses" : [
  		{
  		  "street" : "Altikofenstrasse",
         "number" : "158",
         "postcode" : "",
		  "city" : "Worblaufen",
         "country" : "Switzerland"
  		}
  ]
}
```

```
{
  "customerId" : 2,
  "firstName" : "Renata",
  "lastName" : "Schmutz",
  "title" : "Ms",
  "emailAddress" : "guido.schmutz@someemail.com",
  "addresses" : [
  		{
  		  "street" : "Altikofenstrasse",
         "number" : "158",
         "postcode" : "",
		  "city" : "Worblaufen",
         "country" : "Switzerland"
  		}
  ]
}
```

```
{
  "customerId" : 1,
  "firstName" : "Guido Werner",
  "lastName" : "Schmutz",
  "title" : "Mr",
  "emailAddress" : "guido.schmutz@someemail.com",
  "addresses" : [
  		{
  		  "street" : "Altikofenstrasse",
  		  "city" : "Worblaufen"
  		}
  ]
}
```


## Consuming messages with "Normal" consumer

In one of the Kafka broker

```
docker exec -ti docker_broker-1_1 bash
```

Use the Kafka Console Consumer to get the current messages

```
kafka-console-consumer --bootstrap-server broker-1:9092 --topic customer
```

Use the Kafka Console Consumer to get the all historical messages

```
kafka-console-consumer --bootstrap-server broker-1:9092 --topic customer --from-beginning
```

## Consuming messages with Avro-Console Consumer

```
docker exec -ti schema-registry bash
```

```
kafka-avro-console-consumer --bootstrap-server broker-1:9092 --topic customer --property schema.registry.url=http://schema-registry:8081
```

```
kafka-avro-console-consumer --bootstrap-server broker-1:9092 --topic customer-v1 
```

## Reset consumer group

List the topics to which the group is subscribed

```
kafka-consumer-groups --bootstrap-server broker-1:9092  --group orderManagement --describe
```

Reset the consumer offset for a topic (preview)

```
kafka-consumer-groups --bootstrap-server broker-1:9092 --group orderManagement --topic customer --reset-offsets --to-earliest
```

Reset the consumer offset for a topic (execute)

```
kafka-consumer-groups --bootstrap-server broker-1:9092 --group orderManagement --topic customer --reset-offsets --to-earliest --execute
```
