--TEST--
RdKafka\TopicPartition
--SKIPIF--
<?php
if (!class_exists('RdKafka\TopicPartition')) {
    echo 'skip';
}
?>
--FILE--
<?php

$topar = new RdKafka\TopicPartition("test", RD_KAFKA_PARTITION_UA, 42);

var_dump($topar);

var_dump(array(
    "topic" => $topar->getTopic(),
    "partition" => $topar->getPartition(),
    "offset" => $topar->getOffset(),
));

$topar
    ->setTopic("foo")
    ->setPartition(123)
    ->setOffset(43);

var_dump($topar);
--EXPECT--
object(RdKafka\TopicPartition)#1 (3) {
  ["topic"]=>
  string(4) "test"
  ["partition"]=>
  int(-1)
  ["offset"]=>
  int(42)
}
array(3) {
  ["topic"]=>
  string(4) "test"
  ["partition"]=>
  int(-1)
  ["offset"]=>
  int(42)
}
object(RdKafka\TopicPartition)#1 (3) {
  ["topic"]=>
  string(3) "foo"
  ["partition"]=>
  int(123)
  ["offset"]=>
  int(43)
}
