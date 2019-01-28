# Kafka Disaster Recovery Test

In this project a Kafka HA Setup based on a so-called Streteched Cluster is setup and tested. 

## Setup

Two virtual machines simulating 2 datacenters. Each VM runs 3 Zookeeper Nodes and 3 Kafka brokers.


### Setup Kafka-1

Configure Zookeeper in Supervisor: /etc/supervisor/conf.d/kafka.conf

Configure Kafka in Supervisor: /etc/supervisor/conf.d/kafka.conf

Checkout the rb-dr-case project

```
git clone https://github.com/gschmutz/various-demos.git/ --no-checkout
cd various-demos

git config core.sparseCheckout true
echo "/rb-dr-case/*" > .git/info/sparse-checkout
git checkout master
```


### Configuring /etc/hosts

```
192.168.73.80  kafka-manager	

192.168.73.81  broker-1		broker-2	broker-3
192.168.73.81  zookeeper-1	zookeeper-2	zookeeper-3

192.168.73.82  broker-4        	broker-5        broker-6
192.168.73.82  zookeeper-4     	zookeeper-5     zookeeper-6
```

## Starting Environment


### Starting Zookeeper

In DC1 perform

```
sudo supervisorctl start zookeeper-1 zookeeper-2 zookeeper-3
tail -f /var/log/zookeeper/zookeeper-*.log
```

In DC2 platform

```
sudo supervisorctl start zookeeper-4 zookeeper-5 zookeeper-6
tail -f /var/log/zookeeper/zookeeper-*.log
```

### Starting Kafka 

In DC1 perform

```
sudo supervisorctl start kafka-1 kafka-2 kafka-3
tail -f /var/log/kafka/kafka-*.log
```

In DC2 perform

```
sudo supervisorctl start kafka-4 kafka-5 kafka-6
tail -f /var/log/kafka/kafka-*.log
```

### Starting Kafka Manger

In MGR perform

```
sudo supervisorctl start all
```

<http://kafka-manager:9000>


## Use Cluster

### Create a Topic

```
cd /home/gus/confluent-5.0.1/bin
```

```
./kafka-topics --zookeeper zookeeper-1:2181, zookeeper-4:2185 --create --topic sequence --partitions 8 --replication-factor 4
```

<http://kafka-manager:9000/clusters/rb/topics/sequence>

## Running the test

### Cleanup

Run the following command to cleanup the cluster. 

```
./cleanup.sh
```

```
gus@kafka-manager /m/h/g/w/g/g/v/r/v/scripts> tree ../data/

../data/
|-- kafka-1
|-- kafka-2
|-- kafka-3
|-- kafka-4
|-- kafka-5
|-- kafka-6
|-- zookeeper-1
|   |-- data
|   |   `-- myid
|   `-- log
|-- zookeeper-2
|   |-- data
|   |   `-- myid
|   `-- log
|-- zookeeper-3
|   |-- data
|   |   `-- myid
|   `-- log
|-- zookeeper-4
|   |-- data
|   |   `-- myid
|   `-- log
|-- zookeeper-5
|   |-- data
|   |   `-- myid
|   `-- log
`-- zookeeper-6
    |-- data
    |   `-- myid
    `-- log
```

### Produce to Topic

```
cd $VARIOUS_DEMOS/rb-dr-case/vmware/scripts/
./produce.sh
```

```
echo "true" > control.info
```

```
echo "false" > control.info
```


### Consume from Topic
```
kafkacat -b broker-1:9092 -t sequence -p 1
```


