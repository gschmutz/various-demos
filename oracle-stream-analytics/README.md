# Using Oracle Stream Analytics (18.1)

```
cd /mnt/hgfs/git/gschmutz/various-demos/oracle-stream-analytics
```


```
docker-compose up -d
```

Copy the spark-osa.jar into the two spark workers (workaround)

```
docker cp spark-osa.jar oracle-stream-analytics_worker-1_1:/usr/spark-2.2.1/jars
docker cp spark-osa.jar oracle-stream-analytics_worker-2_1:/usr/spark-2.2.1/jars
```
## Create the Kafka Topics

list topics and create the new topics

```
docker exec -ti oracle-stream-analytics_broker-1_1 bash
```


```
kafka-topics --zookeeper zookeeper:2181 --list

kafka-topics --zookeeper zookeeper:2181 --create --topic truck_position --partitions 8 --replication-factor 2
kafka-topics --zookeeper zookeeper:2181 --create --topic truck_driving_info --partitions 8 --replication-factor 2
kafka-topics --zookeeper zookeeper:2181 --create --topic truck_geofencing --partitions 8 --replication-factor 2
```

## Starting Kafka Truck Simulation

```
cd $SAMPLE_HOME/src/truck-client
```

```
mvn exec:java -Dexec.args="-s KAFKA -f JSON -m COMBINE -t sec"
```

## Starting OSA

make sure that the IP address is current

```
sudo nano ../etc/jetty-osa-datasource.xml
```

Start the shell script

```
cd /home/gus/OSA-18.1.0.0.1/osa-base/bin
```

```
./start-osa.sh dbroot=root dbroot_password=root
```

```
tail -f ../nohup.out 
```

Enter the password to be used for the osaadmin user. 

## Using OSA

Navigate to <http://streamingplatform:9080/osa>

Login using the osaadmin user and the password specified above. 

Navigate to System Settings and enter  