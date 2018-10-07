--TEST--
Bug 88
--SKIPIF--
<?php
if (!class_exists("RdKafka\\KafkaConsumer")) {
    echo "skip";
}
--FILE--
<?php
$conf = new RdKafka\Conf();
$conf->set('metadata.broker.list', '127.0.0.1:9092');
$consumer = new RdKafka\KafkaConsumer($conf);
echo "ok\n";
--EXPECTF--
Fatal error: Uncaught %SRdKafka\Exception%S"group.id" must be configured%s
Stack trace:
%a
