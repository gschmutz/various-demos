from confluent_kafka import avro

record_schema = avro.loads("""
{
  "type": "record",
  "name": "KsqlDataSourceSchema",
  "namespace": "io.confluent.ksql.avro_schemas",
  "fields": [
    {
      "name": "TYPE",
      "type": [
        "null",
        "string"
      ],
      "default": null
    },
    {
      "name": "TOP_10",
      "type": [
        "null",
        {
          "type": "array",
          "items": [
            "null",
            "string"
          ]
        }
      ],
      "default": null
    },
    {
      "name": "TOP_20",
      "type": [
        "null",
        {
          "type": "array",
          "items": [
            "null",
            "string"
          ]
        }
      ],
      "default": null
    }
  ]
}
""")



def consume(topic, conf):
    """
        Consume User records
    """
    from confluent_kafka.avro import AvroConsumer
    from confluent_kafka.avro.serializer import SerializerError

    print("Consuming user records from topic {} with group {}. to exit.".format(topic, conf["group.id"]))

    c = AvroConsumer(conf)
    c.subscribe([topic])

    while True:
        try:
            msg = c.poll(1)

            # There were no messages on the queue, continue polling
            if msg is None:
                continue

            if msg.error():
                print("Consumer error: {}".format(msg.error()))
                continue

            record = User(msg.value())
            print("type: {}\n".format(
                record.TYPE))
        except SerializerError as e:
            # Report malformed record, discard results, continue polling
            print("Message deserialization failed {}".format(msg,e))
            continue
        except KeyboardInterrupt:
            break

    print("Shutting down consumer..")
    c.close()


if __name__ == '__main__':

    # handle common configs
    conf = {'bootstrap.servers': '192.168.73.86:9092',
			'schema.registry.url': 'http://192.168.73.86:8089',
			'group.id': 'consumer'}
    consume ('TWEET_HASHTAG_TOP10_1MIN_T', conf)
    