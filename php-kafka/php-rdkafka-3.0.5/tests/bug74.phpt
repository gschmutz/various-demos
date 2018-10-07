--TEST--
Bug 74
--FILE--
<?php

$consumer = new RdKafka\Consumer(null);
$topic = $consumer->newTopic("batman", null);

$producer = new RdKafka\Producer(null);

if (class_exists('RdKafka\TopicPartition')) {
    $tp = new RdKafka\TopicPartition("batman", 0, null);
}
--EXPECT--
