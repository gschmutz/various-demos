# Apache Atlas Governance Demo

This demo shows how Apache Atlas can be used for Big Data Governance. 

## Setup

The environment can be started using docker compose.

The Atlas docker images has been forked from 

### Install Example Data

```
docker exec -ti atlas tail -f /opt/atlas/logs/application.log
```

### Hive Integration

According to the <https://atlas.apache.org/Hook-Hive.html>

Add to hadoop-hive.env

```
HIVE_SITE_CONF_hive_exec_post_hooks=org.apache.atlas.hive.hook.HiveHook
HIVE_AUX_JARS_PATH=/atlas/hook/hive
```

```
untar apache-atlas-${project.version}-hive-hook.tar.gz available in the Atlas container
```

### Kafka Integration

Create a topic in Kafka

```
docker exec -ti broker-1 kafka-topics --create --bootstrap-server broker-1:9092 --topic truck_position --replication-factor 1 --partitions 8
```


```
tar -xzf apache-atlas-kafka-hook.tar.gz 
```

mv kafka/ /opt/atlas/hook



```
Properties properties = new Properties();
properties.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
KafkaAdminClient kafkaAdminClient = (KafkaAdminClient) AdminClient.create(properties);
ListTopicsResult listTopicResult = kafkaAdminClient.listTopics();
System.out.println(listTopicResult.names().get().toString());
```

### Spark Integration

```
from pyspark.sql import SparkSession

spark = SparkSession.builder.master("spark://spark-master:7077").appName("Hello World").getOrCreate()

l = [('Alice', 1),('Bob', 3)]

df = spark.createDataFrame(l, ['name', 'age'])

df.filter(df.age > 1).collect()
```


## ATLAS REST API



```
curl -u admin:admin -H 'Content-Type: application/json' -XGET http://analyticsplatform:21000/api/atlas/types
```

curl -u admin:admin -H 'Content-Type: application/json' -XGET http://analyticsplatform:21000/api/atlas/v2/types/typedefs

```
curl -u admin:admin -H 'Content-Type: application/json' -XGET http://analyticsplatform:21000/api/atlas/types/avro_type 
```

```
curl -X POST -H 'Content-Type: application/json' -H 'X-XSRF-HEADER: valid' -H 'Authorization: Basic YWRtaW46YWRtaW4=' -i http://analyticsplatform:21000/api/atlas/v2/entity --data '{     "entity":{        "typeName":"kafka_topic",      "attributes":{           "description":null,         "name":"truck_position",         "owner":"Guido",         "qualifiedName":"PRIVATE@${cluster_name}",         "topic":"truck_position",         "uri":"none"      },      "guid":-1   },   "referredEntities":{     }}'```
```


Search

```
http://analyticsplatform:5001/search?query_term=product&page_index=0
```

Metadata

Get Details for a table

```
http://analyticsplatform:5002/table/Table://null.Sales@cl1/time_dim@cl1
```

### Tips und Tricks

#### What does xxxxNational and xxxxLocal mean in Kafka Typedef

We have National and Local kafka installations and may have different replication Factors, retention Bytes, partitionCounts, and segmentBytes for National vs Local

#### How to add custom types to the "create new entity" type selection dropdown in the Atlas UI?

1. Add the following property in ATLAS_HOME/conf/atlas-application.properties :

```
atlas.ui.editable.entity.types=your_custom_type
```

or

```
atlas.ui.editable.entity.types=* ( to list all types)
```

2. Restart Atlas.

3. Refresh browser cache

Now you should be able to see the custom type you created.