--TEST--
Produce, consume
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

$producer = new RdKafka\Producer($conf);

if ($producer->addBrokers(TEST_KAFKA_BROKERS) < 1) {
    echo "Failed adding brokers\n";
    exit;
}

$topicName = sprintf("test_rdkafka_%s", uniqid());

$topic = $producer->newTopic($topicName);

if (!$producer->getMetadata(false, $topic, 2*1000)) {
    echo "Failed to get metadata, is broker down?\n";
}

for ($i = 0; $i < 10; $i++) {
    $topic->produce(0, 0, "message $i");
    $producer->poll(0);
}

while ($producer->getOutQLen()) {
    $producer->poll(50);
}

printf("%d messages delivered\n", $delivered);

$consumer = new RdKafka\Consumer($conf);
$consumer->addBrokers(TEST_KAFKA_BROKERS);

$topic = $consumer->newTopic($topicName);
$topic->consumeStart(0, RD_KAFKA_OFFSET_BEGINNING);

$messages = [];

while (true) {
    $msg = $topic->consume(0, 60*1000);
    if (!$msg) {
        continue;
    }
    switch ($msg->err) {
        case RD_KAFKA_RESP_ERR_NO_ERROR:
            printf("Got message: %s\n", $msg->payload);
            break;
        case RD_KAFKA_RESP_ERR__PARTITION_EOF:
            echo "EOF\n";
            break 2;
        default:
            throw new Exception($message->errstr());
    }
}
--EXPECT--
10 messages delivered
Got message: message 0
Got message: message 1
Got message: message 2
Got message: message 3
Got message: message 4
Got message: message 5
Got message: message 6
Got message: message 7
Got message: message 8
Got message: message 9
EOF
