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

kafka-topics --zookeeper zookeeper:2181 --create --topic customer --partitions 8 --replication-factor 2 --config cleanup.policy=compact --config segment.ms=100 --config delete.retention.ms=100 --config min.cleanable.dirty.ratio=0.01
```


curl -s "https://api.mockaroo.com/api/d5a195e0?count=2000&key=ff7856d0"|kafkacat -b streamingplatform:9092 -t customer -P

