# Kafka Disaster Recovery Test

In this project a Kafka HA Setup based on a so-called Streteched Cluster is setup and tested. 

## Setup

Two virtual machines simulating 2 datacenters. Each VM runs 3 Zookeeper Nodes and 3 Kafka brokers.


### Setup Kafka-1

Configure Zookeeper in Supervisor: /etc/supervisor/conf.d/kafka.conf

Configure Kafka in Supervisor: /etc/supervisor/conf.d/kafka.conf

```
[program:kafka-1]
command=/home/gus/confluent-5.0.1/bin/kafka-server-start /mnt/hgfs/gus/workspace/git/gschmutz/various-demos/rb-dr-case/vmware/config/kafka/server-1.properties
priority=100
startsecs=10
startretries=3
stopsignal=TERM
stopwaitsecs=180
stdout_logfile=/var/log/kafka/kafka-1.log
environment=JMX_PORT=9999

[program:kafka-2]
command=/home/gus/confluent-5.0.1/bin/kafka-server-start /mnt/hgfs/gus/workspace/git/gschmutz/various-demos/rb-dr-case/vmware/config/kafka/server-2.properties
priority=100
startsecs=10
startretries=3
stopsignal=TERM
stopwaitsecs=180                                                              
stdout_logfile=/var/log/kafka/kafka-2.log
environment=JMX_PORT=9998

[program:kafka-3]
command=/home/gus/confluent-5.0.1/bin/kafka-server-start /mnt/hgfs/gus/workspace/git/gschmutz/various-demos/rb-dr-case/vmware/config/kafka/server-3.properties
priority=100
startsecs=10
startretries=3
stopsignal=TERM
stopwaitsecs=180
stdout_logfile=/var/log/kafka/kafka-3.log
environment=JMX_PORT=9997
```

### Configuring /etc/hosts

```
192.168.73.80  kafka-manager	

192.168.73.81  broker-1		broker-2	broker-3
192.168.73.81  zookeeper-1	zookeeper-2	zookeeper-3

192.168.73.82  broker-4        	broker-5        broker-6
192.168.73.82  zookeeper-4     	zookeeper-5     zookeeper-6
```



