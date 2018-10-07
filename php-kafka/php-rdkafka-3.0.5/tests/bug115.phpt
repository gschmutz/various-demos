--TEST--
Bug 115
--SKIPIF--
<?php
file_exists(__DIR__."/test_env.php") || die("skip");
--FILE--
<?php

require __DIR__."/test_env.php";

$delivered = 0;

$conf = new RdKafka\Conf();
if (RD_KAFKA_VERSION >= 0x090000) {
    $conf->set('broker.version.fallback', TEST_KAFKA_BROKER_VERSION);
}
$conf->setErrorCb(function ($producer, $err, $errstr) {
    printf("%s: %s\n", rd_kafka_err2str($err), $errstr);
    exit;
});
$conf->setDrMsgCb(function ($producer, $msg) use (&$delivered) {
    if ($msg->err) {
        throw new Exception("Message delivery failed: " . $msg->errstr());
    }
    $delivered++;
});

$topicName = sprintf("test_rdkafka_%s", uniqid());

$consumer = new RdKafka\Consumer($conf);
$consumer->addBrokers(TEST_KAFKA_BROKERS);

$topic = $consumer->newTopic($topicName);
$topic->consumeStart(0, RD_KAFKA_OFFSET_BEGINNING);

while (true) {
    $msg = $topic->consume(0, 1000);
    if ($msg && $msg->err) {
        break;
    }
}

$topic->consumeStop(0);
--EXPECT--

