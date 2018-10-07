--TEST--
Produce, consume queue
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

$topicNames = [
    sprintf("test_rdkafka_0_%s", uniqid()),
    sprintf("test_rdkafka_1_%s", uniqid()),
];

$topics = array_map(function ($topicName) use ($producer) {
    return $producer->newTopic($topicName);
}, $topicNames);

if (!$producer->getMetadata(false, reset($topics), 2*1000)) {
    echo "Failed to get metadata, is broker down?\n";
}

for ($i = 0; $i < 10; $i++) {
    $topics[$i%2]->produce(0, 0, "message $i");
    $producer->poll(0);
}

while ($producer->getOutQLen()) {
    $producer->poll(50);
}

printf("%d messages delivered\n", $delivered);

$consumer = new RdKafka\Consumer($conf);
$consumer->addBrokers(TEST_KAFKA_BROKERS);

$queue = $consumer->newQueue();

array_walk($topicNames, function ($topicName) use ($consumer, $queue) {
    $topic = $consumer->newTopic($topicName);
    $topic->consumeQueueStart(0, RD_KAFKA_OFFSET_BEGINNING, $queue);
});

$messages = [];
$eof = 0;

while ($eof < 2) {
    $msg = $queue->consume(60*1000);
    if (!$msg) {
        continue;
    }
    switch ($msg->err) {
        case RD_KAFKA_RESP_ERR_NO_ERROR:
            $messages[] = sprintf("Got message: %s from %s", $msg->payload, $msg->topic_name);
            break;
        case RD_KAFKA_RESP_ERR__PARTITION_EOF:
            $eof++;
            break;
        default:
            throw new Exception($message->errstr());
    }
}

sort($messages);
echo implode("\n", $messages), "\n";

--EXPECTF--
10 messages delivered
Got message: message 0 from test_rdkafka_0_%s
Got message: message 1 from test_rdkafka_1_%s
Got message: message 2 from test_rdkafka_0_%s
Got message: message 3 from test_rdkafka_1_%s
Got message: message 4 from test_rdkafka_0_%s
Got message: message 5 from test_rdkafka_1_%s
Got message: message 6 from test_rdkafka_0_%s
Got message: message 7 from test_rdkafka_1_%s
Got message: message 8 from test_rdkafka_0_%s
Got message: message 9 from test_rdkafka_1_%s
