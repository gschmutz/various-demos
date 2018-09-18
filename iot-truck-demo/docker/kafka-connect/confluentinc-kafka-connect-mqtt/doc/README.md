# Introduction

This project provides connectors for Kafka Connect to read and write data to a Mqtt broker.

# Source Connectors


## Mqtt Source Connector

This connector connects to a Mqtt broker and subscribes to the specified topics.

### Tip

The output of this connector is an envelope with all of the properties of the incoming message. If you need the body of the Mqtt message as the value written to the topic in Kakfa, use a Transformation to achieve this.

### Configuration

##### `mqtt.server.uri`
*Importance:* High

*Type:* List


List of Mqtt brokers to connect to.
##### `mqtt.topics`
*Importance:* High

*Type:* List


The Mqtt topics to subscribe to.
##### `mqtt.password`
*Importance:* High

*Type:* Password

*Default Value:* [hidden]


Password to connect with.
##### `mqtt.username`
*Importance:* High

*Type:* String


Username to connect with.
##### `kafka.topic.prefix`
*Importance:* Medium

*Type:* String

*Default Value:* mqtt.


The prefix to append to the Mqtt topic name when writing to Kafka.
##### `mqtt.clean.session.enabled`
*Importance:* Low

*Type:* Boolean

*Default Value:* true


Sets whether the client and server should remember state across restarts and reconnects.
##### `mqtt.connect.timeout.seconds`
*Importance:* Low

*Type:* Int

*Default Value:* 30

*Validator:* [1,...]


Sets the connection timeout value.
##### `mqtt.keepalive.interval.seconds`
*Importance:* Low

*Type:* Int

*Default Value:* 60

*Validator:* [1,...]


Sets the "keep alive" interval.
##### `mqtt.qos`
*Importance:* Low

*Type:* Int

*Default Value:* 0

*Validator:* [0,...,3]


mqtt.qos

#### Examples

##### Standalone Example

This configuration is used typically along with [standalone mode](http://docs.confluent.io/current/connect/concepts.html#standalone-workers).

```properties
name=MqttSourceConnector1
connector.class=com.github.jcustenborder.kafka.connect.mqtt.MqttSourceConnector
tasks.max=1
mqtt.server.uri=< Required Configuration >
mqtt.topics=< Required Configuration >
```

##### Distributed Example

This configuration is used typically along with [distributed mode](http://docs.confluent.io/current/connect/concepts.html#distributed-workers).
Write the following json to `connector.json`, configure all of the required values, and use the command below to
post the configuration to one the distributed connect worker(s).

```json
{
  "config" : {
    "name" : "MqttSourceConnector1",
    "connector.class" : "com.github.jcustenborder.kafka.connect.mqtt.MqttSourceConnector",
    "tasks.max" : "1",
    "mqtt.server.uri" : "< Required Configuration >",
    "mqtt.topics" : "< Required Configuration >"
  }
}
```

Use curl to post the configuration to one of the Kafka Connect Workers. Change `http://localhost:8083/` the the endpoint of
one of your Kafka Connect worker(s).

Create a new instance.
```bash
curl -s -X POST -H 'Content-Type: application/json' --data @connector.json http://localhost:8083/connectors
```

Update an existing instance.
```bash
curl -s -X PUT -H 'Content-Type: application/json' --data @connector.json http://localhost:8083/connectors/TestSinkConnector1/config
```




