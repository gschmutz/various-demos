# PHP Kafka client - php-rdkafka

[![Join the chat at https://gitter.im/arnaud-lb/php-rdkafka](https://badges.gitter.im/arnaud-lb/php-rdkafka.svg)](https://gitter.im/arnaud-lb/php-rdkafka?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

![Supported Kafka versions: 0.8, 0.9, 0.10](https://img.shields.io/badge/kafka-0.8%2C%200.9%2C%200.10-blue.svg) ![Supported PHP versions: 5.3 .. 7.x](https://img.shields.io/badge/php-5.3,%207.x-blue.svg) [![Build Status](https://travis-ci.org/arnaud-lb/php-rdkafka.svg)](https://travis-ci.org/arnaud-lb/php-rdkafka)

PHP-rdkafka is a thin [librdkafka](https://github.com/edenhill/librdkafka) binding providing a working PHP 5 / PHP 7 [Kafka](https://kafka.apache.org/) 0.8 / 0.9 / 0.10 client.

It supports the high level and low level *consumers*, *producer*, and *metadata* APIs.

The API ressembles as much as possible to librdkafka's, and is fully documented [here](https://arnaud-lb.github.io/php-rdkafka/phpdoc/book.rdkafka.html).

## Table of Contents

1. [Installation](#installation)
2. [Examples](#examples)
3. [Usage](#usage)
   * [Producing](#producing)
   * [High-level consuming](#high-level-consuming)
   * [Low-level consuming](#low-level-consuming)
   * [Low-level consuming form multiple topics / partitions](#low-level-consuming-from-multiple-topics--partitions)
   * [Using stored offsets](#using-stored-offsets)
   * [Interesting configuration parameters](#interesting-configuration-parameters)
     * [queued.max.messages.kbytes](#queuedmaxmessageskbytes)
     * [topic.metadata.refresh.sparse and topic.metadata.refresh.interval.ms](#topicmetadatarefreshsparse-and-topicmetadatarefreshintervalms)
     * [internal.termination.signal](#internalterminationsignal)
4. [Documentation](#documentation)
5. [Credits](#credits)
6. [License](#license)

## Installation

https://arnaud-lb.github.io/php-rdkafka/phpdoc/rdkafka.setup.html

## Examples

https://arnaud-lb.github.io/php-rdkafka/phpdoc/rdkafka.examples.html

## Usage

### Producing

For producing, we first need to create a producer, and to add brokers (Kafka
servers) to it:

``` php
<?php

$rk = new RdKafka\Producer();
$rk->setLogLevel(LOG_DEBUG);
$rk->addBrokers("10.0.0.1,10.0.0.2");
```

Next, we create a topic instance from the producer:

``` php
<?php

$topic = $rk->newTopic("test");
```

From there, we can produce as much messages as we want, using the produce
method:

``` php
<?php

$topic->produce(RD_KAFKA_PARTITION_UA, 0, "Message payload");
```

The first argument is the partition. RD_KAFKA_PARTITION_UA stands for
*unassigned*, and lets librdkafka choose the partition.

The second argument are message flags and should always be 0, currently.

The message payload can be anything.

### High-level consuming

The RdKafka\KafkaConsumer class supports automatic partition assignment/revocation. See the example [here](https://arnaud-lb.github.io/php-rdkafka/phpdoc/rdkafka.examples.html#example-1).

### Low-level consuming

We first need to create a low level consumer, and to add brokers (Kafka
servers) to it:

``` php
<?php

$rk = new RdKafka\Consumer();
$rk->setLogLevel(LOG_DEBUG);
$rk->addBrokers("10.0.0.1,10.0.0.2");
```

Next, create a topic instance by calling the `newTopic()` method, and start
consuming on partition 0:

``` php
<?php

$topic = $rk->newTopic("test");

// The first argument is the partition to consume from.
// The second argument is the offset at which to start consumption. Valid values
// are: RD_KAFKA_OFFSET_BEGINNING, RD_KAFKA_OFFSET_END, RD_KAFKA_OFFSET_STORED.
$topic->consumeStart(0, RD_KAFKA_OFFSET_BEGINNING);
```

Next, retrieve the consumed messages:

``` php
<?php

while (true) {
    // The first argument is the partition (again).
    // The second argument is the timeout.
    $msg = $topic->consume(0, 1000);
    if ($msg->err) {
        echo $msg->errstr(), "\n";
        break;
    } else {
        echo $msg->payload, "\n";
    }
}
```

### Low-level consuming from multiple topics / partitions

Consuming from multiple topics and/or partitions can be done by telling
librdkafka to forward all messages from these topics/partitions to an internal
queue, and then consuming from this queue:

Creating the queue:

``` php
<?php
$queue = $rk->newQueue();
```

Adding topars to the queue:

``` php
<?php

$topic1 = $rk->newTopic("topic1");
$topic1->consumeQueueStart(0, RD_KAFKA_OFFSET_BEGINNING, $queue);
$topic1->consumeQueueStart(1, RD_KAFKA_OFFSET_BEGINNING, $queue);

$topic2 = $rk->newTopic("topic2");
$topic2->consumeQueueStart(0, RD_KAFKA_OFFSET_BEGINNING, $queue);
```

Next, retrieve the consumed messages from the queue:

``` php
<?php

while (true) {
    // The only argument is the timeout.
    $msg = $queue->consume(1000);
    if ($msg->err) {
        echo $msg->errstr(), "\n";
        break;
    } else {
        echo $msg->payload, "\n";
    }
}
```

### Using stored offsets

librdkafka can store offsets in a local file, or on the broker. The default is local file, and as soon as you start using ``RD_KAFKA_OFFSET_STORED`` as consuming offset, rdkafka starts to store the offset.

By default, the file is created in the current directory, with a name based on the topic and the partition. The directory can be changed by setting the ``offset.store.path`` [configuration property](https://github.com/edenhill/librdkafka/blob/master/CONFIGURATION.md).

Other interesting properties are: ``offset.store.sync.interval.ms``, ``offset.store.method``, ``auto.commit.interval.ms``, ``auto.commit.enable``, ``offset.store.method``, ``group.id``.

``` php
<?php

$topicConf = new RdKafka\TopicConf();
$topicConf->set("auto.commit.interval.ms", 1e3);
$topicConf->set("offset.store.sync.interval.ms", 60e3);

$topic = $rk->newTopic("test", $topicConf);

$topic->consumeStart(0, RD_KAFKA_OFFSET_STORED);
```

### Interesting configuration parameters

#### queued.max.messages.kbytes

librdkafka will buffer up to 1GB of messages for each consumed partition by default. You can lower memory usage by reducing the value of the ``queued.max.messages.kbytes`` parameter on your consumers.

### topic.metadata.refresh.sparse and topic.metadata.refresh.interval.ms

Each consumer and procuder instance will fetch topics metadata at an interval defined by the ``topic.metadata.refresh.interval.ms`` parameter. Depending on your librdkafka version, the parameter defaults to 10 seconds, or 600 seconds.

librdkafka fetches the metadata for all topics of the cluster by default. Setting ``topic.metadata.refresh.sparse`` to the string ``"true"`` makes sure that librdkafka fetches only the topics he uses.

Setting ``topic.metadata.refresh.sparse`` to ``"true"``, and ``topic.metadata.refresh.interval.ms`` to 600 seconds (plus some jitter) can reduce the bandwidth a lot, depending on the number of consumers and topics.

### internal.termination.signal

This setting allows librdkafka threads to terminate as soon as librdkafka is done with them. This effectively allows your PHP processes / requests to terminate quickly.

When enabling this, you have to mask the signal like this:

``` php
<?php
// once
pcntl_sigprocmask(SIG_BLOCK, array(SIGIO));
// any time
$conf->set('internal.termination.signal', SIGIO);
```

### socket.blocking.max.ms

> Maximum time a broker socket operation may block. A lower value improves responsiveness at the expense of slightly higher CPU usage.

Reducing the value of this setting improves shutdown speed. The value defines the maximum time librdkafka will block in one iteration of a read loop. This also defines how often the main librdkafka thread will check for termination.

### queue.buffering.max.ms

This defines the maximum and default time librdkafka will wait before sending a batch of messages. Reducing this setting to e.g. 1ms ensures that messages are sent ASAP, instead of being batched.

This has been seen to reduce the shutdown time of the rdkafka instance, and of the PHP process / request.

## Performance / Low-latency settings

Here is a configuration optimized for low latency. This allows a PHP process / request to send messages ASAP and to terminate quickly.

``` php
<?php

$conf = new \RdKafka\Conf();
$conf->set('socket.timeout.ms', 50); // or socket.blocking.max.ms, depending on librdkafka version
if (function_exists('pcntl_sigprocmask')) {
    pcntl_sigprocmask(SIG_BLOCK, array(SIGIO));
    $conf->set('internal.termination.signal', SIGIO);
} else {
    $conf->set('queue.buffering.max.ms', 1);
}

$producer = new \RdKafka\Producer($conf);
$consumer = new \RdKafka\Consumer($conf);
```

Polling after producing can also be important to reduce termination times:

```
$producer->produce(...);
while ($producer->getOutQLen() > 0) {
    $producer->poll(1);
}
```

## Documentation

https://arnaud-lb.github.io/php-rdkafka/phpdoc/book.rdkafka.html

## Asking for Help

If the documentation is not enough, feel free to ask a questions on the php-rdkafka channels on [Gitter](https://gitter.im/arnaud-lb/php-rdkafka) or [Google Groups](https://groups.google.com/forum/#!forum/php-rdkafka).

## Stubs

Because your IDE is not able to auto discover php-rdkadka api you can consider usage of external package providing a set of stubs for php-rdkafka classes, functions and constants: [kwn/php-rdkafka-stubs](https://github.com/kwn/php-rdkafka-stubs)

## Contributing

If you would like to contribute, thank you :)

Before you start, please take a look at the [CONTRIBUTING document](https://github.com/arnaud-lb/php-rdkafka/blob/master/CONTRIBUTING.md) to see how to get your changes merged in.

## Credits

Documentation copied from [librdkafka](https://github.com/edenhill/librdkafka).

Authors: see [contributors](https://github.com/arnaud-lb/php-rdkafka/graphs/contributors).

## License

php-rdkafka is released under the [MIT](https://github.com/arnaud-lb/php-rdkafka/blob/master/LICENSE) license.
