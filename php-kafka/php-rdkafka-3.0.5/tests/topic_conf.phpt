--TEST--
RdKafka\TopicConf
--FILE--
<?php

$conf = new RdKafka\TopicConf();

echo "Setting partitioner\n";
$conf->setPartitioner(RD_KAFKA_MSG_PARTITIONER_RANDOM);

--EXPECT--
Setting partitioner
