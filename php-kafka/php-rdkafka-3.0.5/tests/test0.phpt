--TEST--
test0
--FILE--
<?php
var_dump(class_exists("Rdkafka\Consumer"));
--EXPECT--
bool(true)
