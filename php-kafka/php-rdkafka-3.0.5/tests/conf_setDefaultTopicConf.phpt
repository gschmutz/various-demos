--TEST--
RdKafka\Conf::setDefaultTopicConf()
--SKIPIF--
<?php
if (!method_exists('RdKafka\Conf', 'setDefaultTopicConf')) {
    echo "skip";
}
?>
--FILE--
<?php

$conf = new RdKafka\Conf();

echo "Setting valid topic conf\n";
$conf->setDefaultTopicConf(new RdKafka\TopicConf());

echo "Setting invalid topic conf\n";
$conf->setDefaultTopicConf($conf);

--EXPECTF--
Setting valid topic conf
Setting invalid topic conf

Warning: RdKafka\Conf::setDefaultTopicConf() expects parameter 1 to be RdKafka\TopicConf, object given in %s/conf_setDefaultTopicConf.php on line 9

