# Azure IoT Hub to Hasura GraphQL

## IoT Simulator

```
mkdir conf
```

```
nano conf/config-azureiothub.json
```

```
{
	"host": "gusiothub2.azure-devices.net",
	"deviceId": "test",
	"accessKey": "T5aTVz79LDH7kSKpyMLqSh4Fm9ChQdL2VDtQIWsnkik=",
	"clients": 1,
	"seed": 123456
}
```


nano conf/devices-def.json
```

add the follwoing definition

```
[
    {
        "type":"simple",
        "uuid":"",
        "topic":"sensor-reading",
        "partition":"{$uuid}",
        "sampling":{"type":"fixed", "interval":4000},
        "copy":1,
        "sensors":[
            {"type":"string", "name":"sensorType", "cycle":["temperature"]},
            {"type":"dev.timestamp",    "name":"ts", "format":"yyyy-MM-dd'T'HH:mm:ss.SSSZ"},
            {"type":"dev.uuid",         "name":"uuid"},
            {"type":"double_walk",   "name":"temp",  "min":10, "max":20},
            {"type":"double_walk",  "name":"level", "values": [1.1,3.2,8.3,9.4]},
            {"type":"string",        "name":"category", "random": ["a","b","c","d","e","f","g","h","i","j","k","l","m","n","o"]}
        ]
    }
]
```

```
docker run -rm -v $PWD/conf/config-azureiothub.json:/conf/config-azureiothub.json -v $PWD/conf/devices-def.json:/conf/devices-def.json trivadis/iot-simulator -t azureiothub -cf /conf/config-azureiothub.json -df /conf/devices-def.json
```

## Avro Message

Raw message

```
{
  "sensorType": "temperature",
  "ts": "2021-02-08T16:49:03.218+0000",
  "uuid": "ce2730fc-f2f0-4268-bbdc-706404ee4ef2",
  "temp": 11.7258,
  "level": 0.4966,
  "category": "a"
}
```

Register Avro Schema in Schema Registry under subject `sensor-reading-v1-value`:

```
{
  "type": "record",
  "name": "SensorReadingV1",
  "namespace": "com.trivadis.demo",
  "doc": "This is a sample Avro schema for a sensor reading",
  "fields": [
    {
      "name": "sensorType",
      "type": "string"
    },
    {
      "name": "ts",
      "logicalType": "timestamp",
      "type": "long"
    },
    {
      "name": "uuid",
      "type": "string"
    },
    {
      "name": "temp",
      "type": "double"
    },
    {
      "name": "level",
      "type": "double"
    },
    {
      "name": "category",
      "type": "string"
    }
  ]
}
```

## Kafka

Create a topic

```
docker exec -ti kafka-1 kafka-topics --zookeeper zookeeper-1:2181 --create --topic sensor-reading-v1 --replication-factor 3 --partitions 8 
```

```
docker exec -ti kafkacat kafkacat -b kafka-1 -t sensor-reading-v1 -s avro -r http://schema-registry-1:8081 -q
```


