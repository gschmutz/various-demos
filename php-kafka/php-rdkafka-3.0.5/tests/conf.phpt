--TEST--
RdKafka\Conf
--FILE--
<?php

$conf = new RdKafka\Conf();

echo "Setting a string property\n";
$conf->set("client.id", "acme");

echo "Setting an integer property\n";
$conf->set("message.max.bytes", 1 << 20);

echo "Setting a boolean property\n";
$conf->set("topic.metadata.refresh.sparse", "true");

echo "Setting a boolean property to an invalid value\n";
try {
    $conf->set("topic.metadata.refresh.sparse", "xx");
} catch(Exception $e) {
    printf("Caught a %s: %s\n", get_class($e), $e->getMessage());
}

echo "Setting an invalid property\n";
try {
    $conf->set("invalid", "xx");
} catch(Exception $e) {
    printf("Caught a %s: %s\n", get_class($e), $e->getMessage());
}

echo "Setting error callback\n";
$conf->setErrorCb(function () { });
$dump = $conf->dump();
var_dump(isset($dump["error_cb"]));

echo "Setting dr_msg callback\n";
$conf->setDrMsgCb(function () { });
$dump = $conf->dump();
var_dump(isset($dump["dr_msg_cb"]));

echo "Setting stats callback\n";
$conf->setStatsCb(function () { });
$dump = $conf->dump();
var_dump(isset($dump["stats_cb"]));

echo "Dumping conf\n";
var_dump(array_intersect_key($conf->dump(), array(
    "client.id" => true,
    "message.max.bytes" => true,
    "topic.metadata.refresh.sparse" => true,
)));

--EXPECT--
Setting a string property
Setting an integer property
Setting a boolean property
Setting a boolean property to an invalid value
Caught a RdKafka\Exception: Expected bool value for "topic.metadata.refresh.sparse": true or false
Setting an invalid property
Caught a RdKafka\Exception: No such configuration property: "invalid"
Setting error callback
bool(true)
Setting dr_msg callback
bool(true)
Setting stats callback
bool(true)
Dumping conf
array(3) {
  ["client.id"]=>
  string(4) "acme"
  ["message.max.bytes"]=>
  string(7) "1048576"
  ["topic.metadata.refresh.sparse"]=>
  string(4) "true"
}
