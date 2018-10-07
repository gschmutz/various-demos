
<?php

$conf = new RdKafka\Conf();
$conf->setErrorCb(function ($kafka, $err, $reason) {
    printf("Kafka error: %s (reason: %s)\n", rd_kafka_err2str($err), $reason);
});

$rk = new RdKafka\Producer($conf);
$rk->setLogLevel(LOG_DEBUG);
$rk->addBrokers("192.168.1.141");

$topic = $rk->newTopic("php_test");

for ($i = 0; $i < 10; $i++) {
    $topic->produce(RD_KAFKA_PARTITION_UA, 0, "Message $i");
    $rk->poll(0);
}

while ($rk->getOutQLen() > 0) {
    $rk->poll(50);
}

?>

