--TEST--
rd_kafka_get_err_descs()
--SKIPIF--
<?php
if (!function_exists('rd_kafka_get_err_descs')) {
    echo "skip";
}
--FILE--
<?php
$descs = rd_kafka_get_err_descs();
var_dump(gettype($descs));
foreach ($descs as $desc) {
    if ($desc['name'] == '_MSG_TIMED_OUT') {
        var_dump($desc);
    }
}
--EXPECT--
string(5) "array"
array(3) {
  ["code"]=>
  int(-192)
  ["name"]=>
  string(14) "_MSG_TIMED_OUT"
  ["desc"]=>
  string(24) "Local: Message timed out"
}
