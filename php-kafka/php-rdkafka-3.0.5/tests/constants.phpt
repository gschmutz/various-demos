--TEST--
constants
--FILE--
<?php
var_dump(RD_KAFKA_RESP_ERR__UNKNOWN_PARTITION);
--EXPECT--
int(-190)
