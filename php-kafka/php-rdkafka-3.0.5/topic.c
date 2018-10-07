/*
  +----------------------------------------------------------------------+
  | php-rdkafka                                                          |
  +----------------------------------------------------------------------+
  | Copyright (c) 2016 Arnaud Le Blanc                                   |
  +----------------------------------------------------------------------+
  | This source file is subject to version 3.01 of the PHP license,      |
  | that is bundled with this package in the file LICENSE, and is        |
  | available through the world-wide-web at the following url:           |
  | http://www.php.net/license/3_01.txt                                  |
  | If you did not receive a copy of the PHP license and are unable to   |
  | obtain it through the world-wide-web, please send a note to          |
  | license@php.net so we can mail you a copy immediately.               |
  +----------------------------------------------------------------------+
  | Author: Arnaud Le Blanc <arnaud.lb@gmail.com>                        |
  +----------------------------------------------------------------------+
*/

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include "php.h"
#include "php_rdkafka.h"
#include "php_rdkafka_priv.h"
#include "librdkafka/rdkafka.h"
#include "ext/spl/spl_iterators.h"
#include "Zend/zend_interfaces.h"
#include "Zend/zend_exceptions.h"
#include "ext/spl/spl_exceptions.h"
#include "topic.h"
#include "queue.h"
#include "message.h"

static zend_object_handlers object_handlers;
zend_class_entry * ce_kafka_consumer_topic;
zend_class_entry * ce_kafka_kafka_consumer_topic;
zend_class_entry * ce_kafka_producer_topic;
zend_class_entry * ce_kafka_topic;

static void kafka_topic_free(zend_object *object TSRMLS_DC) /* {{{ */
{
    kafka_topic_object *intern = get_custom_object(kafka_topic_object, object);

    if (intern->rkt) {
        kafka_object *kafka_intern = get_kafka_object(P_ZEVAL(intern->zrk) TSRMLS_CC);
        if (kafka_intern) {
            zend_hash_index_del(&kafka_intern->topics, (zend_ulong)intern);
        }
    }

    zend_object_std_dtor(&intern->std TSRMLS_CC);

    free_custom_object(intern);
}
/* }}} */

static zend_object_value kafka_topic_new(zend_class_entry *class_type TSRMLS_DC) /* {{{ */
{
    zend_object_value retval;
    kafka_topic_object *intern;

    intern = alloc_object(intern, class_type);
    zend_object_std_init(&intern->std, class_type TSRMLS_CC);
    object_properties_init(&intern->std, class_type);

    STORE_OBJECT(retval, intern, (zend_objects_store_dtor_t) zend_objects_destroy_object, kafka_topic_free, NULL);
    SET_OBJECT_HANDLERS(retval, &object_handlers);

    return retval;
}
/* }}} */

kafka_topic_object * get_kafka_topic_object(zval *zrkt TSRMLS_DC)
{
    kafka_topic_object *orkt = get_custom_object_zval(kafka_topic_object, zrkt);

    if (!orkt->rkt) {
        zend_throw_exception_ex(NULL, 0 TSRMLS_CC, "RdKafka\\Topic::__construct() has not been called" TSRMLS_CC);
        return NULL;
    }

    return orkt;
}

/* {{{ proto void RdKafka\ConsumerTopic::consumeQueueStart(int $partition, int $offset, RdKafka\Queue $queue)
 * Same as consumeStart(), but re-routes incoming messages to the provided queue */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_consume_queue_start, 0, 0, 3)
    ZEND_ARG_INFO(0, partition)
    ZEND_ARG_INFO(0, offset)
    ZEND_ARG_INFO(0, queue)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__ConsumerTopic, consumeQueueStart)
{
    zval *zrkqu;
    kafka_topic_object *intern;
    kafka_queue_object *queue_intern;
    long partition;
    long offset;
    int ret;
    rd_kafka_resp_err_t err;
    kafka_object *kafka_intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "llO", &partition, &offset, &zrkqu, ce_kafka_queue) == FAILURE) {
        return;
    }

    if (partition != RD_KAFKA_PARTITION_UA && (partition < 0 || partition > 0x7FFFFFFF)) {
        zend_throw_exception_ex(spl_ce_InvalidArgumentException, 0 TSRMLS_CC, "Out of range value '%ld' for $partition", partition TSRMLS_CC);
        return;
    }

    intern = get_kafka_topic_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    queue_intern = get_kafka_queue_object(zrkqu TSRMLS_CC);
    if (!queue_intern) {
        return;
    }

    kafka_intern = get_kafka_object(P_ZEVAL(intern->zrk) TSRMLS_CC);
    if (!kafka_intern) {
        return;
    }

    if (is_consuming_toppar(kafka_intern, intern->rkt, partition)) {
        zend_throw_exception_ex(
            ce_kafka_exception,
            0 TSRMLS_CC,
            "%s:%d is already being consumed by the same Consumer instance",
            rd_kafka_topic_name(intern->rkt),
            partition
        );
        return;
    }

    ret = rd_kafka_consume_start_queue(intern->rkt, partition, offset, queue_intern->rkqu);

    if (ret == -1) {
        err = rd_kafka_errno2err(errno);
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }

    add_consuming_toppar(kafka_intern, intern->rkt, partition);
}
/* }}} */

/* {{{ proto void RdKafka\ConsumerTopic::consumeStart(int partition, int offset)
   Start consuming messages */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_consume_start, 0, 0, 2)
    ZEND_ARG_INFO(0, partition)
    ZEND_ARG_INFO(0, offset)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__ConsumerTopic, consumeStart)
{
    kafka_topic_object *intern;
    long partition;
    long offset;
    int ret;
    rd_kafka_resp_err_t err;
    kafka_object *kafka_intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "ll", &partition, &offset) == FAILURE) {
        return;
    }

    if (partition != RD_KAFKA_PARTITION_UA && (partition < 0 || partition > 0x7FFFFFFF)) {
        zend_throw_exception_ex(spl_ce_InvalidArgumentException, 0 TSRMLS_CC, "Out of range value '%ld' for $partition", partition TSRMLS_CC);
        return;
    }

    intern = get_kafka_topic_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    kafka_intern = get_kafka_object(P_ZEVAL(intern->zrk) TSRMLS_CC);
    if (!kafka_intern) {
        return;
    }

    if (is_consuming_toppar(kafka_intern, intern->rkt, partition)) {
        zend_throw_exception_ex(
            ce_kafka_exception,
            0 TSRMLS_CC,
            "%s:%d is already being consumed by the same Consumer instance",
            rd_kafka_topic_name(intern->rkt),
            partition
        );
        return;
    }

    ret = rd_kafka_consume_start(intern->rkt, partition, offset);

    if (ret == -1) {
        err = rd_kafka_errno2err(errno);
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }

    add_consuming_toppar(kafka_intern, intern->rkt, partition);
}
/* }}} */

/* {{{ proto void RdKafka\ConsumerTopic::consumeStop(int partition)
   Stop consuming messages */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_consume_stop, 0, 0, 1)
    ZEND_ARG_INFO(0, partition)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__ConsumerTopic, consumeStop)
{
    kafka_topic_object *intern;
    long partition;
    int ret;
    rd_kafka_resp_err_t err;
    kafka_object *kafka_intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "l", &partition) == FAILURE) {
        return;
    }

    if (partition != RD_KAFKA_PARTITION_UA && (partition < 0 || partition > 0x7FFFFFFF)) {
        zend_throw_exception_ex(spl_ce_InvalidArgumentException, 0 TSRMLS_CC, "Out of range value '%ld' for $partition", partition TSRMLS_CC);
        return;
    }

    intern = get_kafka_topic_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    kafka_intern = get_kafka_object(P_ZEVAL(intern->zrk) TSRMLS_CC);
    if (!kafka_intern) {
        return;
    }

    ret = rd_kafka_consume_stop(intern->rkt, partition);

    if (ret == -1) {
        err = rd_kafka_errno2err(errno);
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }

    del_consuming_toppar(kafka_intern, intern->rkt, partition);
}
/* }}} */

/* {{{ proto RdKafka\Message RdKafka\ConsumerTopic::consume(int $partition, int timeout_ms)
   Consume a single message from partition */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_consume, 0, 0, 2)
    ZEND_ARG_INFO(0, partition)
    ZEND_ARG_INFO(0, timeout_ms)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__ConsumerTopic, consume)
{
    kafka_topic_object *intern;
    long partition;
    long timeout_ms;
    rd_kafka_message_t *message;
    rd_kafka_resp_err_t err;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "ll", &partition, &timeout_ms) == FAILURE) {
        return;
    }

    if (partition != RD_KAFKA_PARTITION_UA && (partition < 0 || partition > 0x7FFFFFFF)) {
        zend_throw_exception_ex(spl_ce_InvalidArgumentException, 0 TSRMLS_CC, "Out of range value '%ld' for $partition", partition TSRMLS_CC);
        return;
    }

    intern = get_kafka_topic_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    message = rd_kafka_consume(intern->rkt, partition, timeout_ms);

    if (!message) {
        err = rd_kafka_errno2err(errno);
        if (err == RD_KAFKA_RESP_ERR__TIMED_OUT) {
            return;
        }
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }

    kafka_message_new(return_value, message TSRMLS_CC);

    rd_kafka_message_destroy(message);
}
/* }}} */

/* {{{ proto void RdKafka\ConsumerTopic::offsetStore(int partition, int offset) */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_offset_store, 0, 0, 2)
    ZEND_ARG_INFO(0, partition)
    ZEND_ARG_INFO(0, offset)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__ConsumerTopic, offsetStore)
{
    kafka_topic_object *intern;
    long partition;
    long offset;
    rd_kafka_resp_err_t err;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "ll", &partition, &offset) == FAILURE) {
        return;
    }

    intern = get_kafka_topic_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    err = rd_kafka_offset_store(intern->rkt, partition, offset);

    if (err != RD_KAFKA_RESP_ERR_NO_ERROR) {
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }
}
/* }}} */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka___private_construct, 0, 0, 0)
ZEND_END_ARG_INFO()

static const zend_function_entry kafka_consumer_topic_fe[] = {
    PHP_ME(RdKafka, __construct, arginfo_kafka___private_construct, ZEND_ACC_PRIVATE)
    PHP_ME(RdKafka__ConsumerTopic, consumeQueueStart, arginfo_kafka_consume_queue_start, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__ConsumerTopic, consumeStart, arginfo_kafka_consume_start, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__ConsumerTopic, consumeStop, arginfo_kafka_consume_stop, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__ConsumerTopic, consume, arginfo_kafka_consume, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__ConsumerTopic, offsetStore, arginfo_kafka_offset_store, ZEND_ACC_PUBLIC)
    PHP_FE_END
};

static const zend_function_entry kafka_kafka_consumer_topic_fe[] = {
    PHP_ME(RdKafka, __construct, arginfo_kafka___private_construct, ZEND_ACC_PRIVATE)
    PHP_ME(RdKafka__ConsumerTopic, offsetStore, arginfo_kafka_offset_store, ZEND_ACC_PUBLIC)
    PHP_FE_END
};

/* {{{ proto void RdKafka\ProducerTopic::produce(int $partition, int $msgflags, string $payload[, string $key])
   Produce and send a single message to broker. */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_produce, 0, 0, 3)
    ZEND_ARG_INFO(0, partition)
    ZEND_ARG_INFO(0, msgflags)
    ZEND_ARG_INFO(0, payload)
    ZEND_ARG_INFO(0, key)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__ProducerTopic, produce)
{
    long partition;
    long msgflags;
    char *payload;
    arglen_t payload_len;
    char *key = NULL;
    arglen_t key_len = 0;
    int ret;
    rd_kafka_resp_err_t err;
    kafka_topic_object *intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "lls|s!", &partition, &msgflags, &payload, &payload_len, &key, &key_len) == FAILURE) {
        return;
    }

    if (partition != RD_KAFKA_PARTITION_UA && (partition < 0 || partition > 0x7FFFFFFF)) {
        zend_throw_exception_ex(spl_ce_InvalidArgumentException, 0 TSRMLS_CC, "Out of range value '%ld' for $partition", partition TSRMLS_CC);
        return;
    }

    if (msgflags != 0) {
        zend_throw_exception_ex(spl_ce_InvalidArgumentException, 0 TSRMLS_CC, "Invalid value '%ld' for $msgflags", msgflags TSRMLS_CC);
        return;
    }

    intern = get_kafka_topic_object(getThis() TSRMLS_CC);

    ret = rd_kafka_produce(intern->rkt, partition, msgflags | RD_KAFKA_MSG_F_COPY, payload, payload_len, key, key_len, NULL);

    if (ret == -1) {
        err = rd_kafka_errno2err(errno);
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }
}
/* }}} */

static const zend_function_entry kafka_producer_topic_fe[] = {
    PHP_ME(RdKafka, __construct, arginfo_kafka___private_construct, ZEND_ACC_PRIVATE)
    PHP_ME(RdKafka__ProducerTopic, produce, arginfo_kafka_produce, ZEND_ACC_PUBLIC)
    PHP_FE_END
};

/* {{{ proto string RdKafka\Topic::getName() */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_topic_get_name, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Topic, getName)
{
    kafka_topic_object *intern;

    if (zend_parse_parameters_none() == FAILURE) {
        return;
    }

    intern = get_kafka_topic_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    RDKAFKA_RETURN_STRING(rd_kafka_topic_name(intern->rkt));
}
/* }}} */

static const zend_function_entry kafka_topic_fe[] = {
    PHP_ME(RdKafka__Topic, getName, arginfo_kafka_topic_get_name, ZEND_ACC_PUBLIC)
    PHP_FE_END
};

void kafka_topic_minit(TSRMLS_D) { /* {{{ */

    zend_class_entry ce;

    memcpy(&object_handlers, zend_get_std_object_handlers(), sizeof(zend_object_handlers));
    object_handlers.clone_obj = NULL;
    set_object_handler_free_obj(&object_handlers, kafka_topic_free);
    set_object_handler_offset(&object_handlers, XtOffsetOf(kafka_topic_object, std));

    INIT_NS_CLASS_ENTRY(ce, "RdKafka", "Topic", kafka_topic_fe);
    ce_kafka_topic = zend_register_internal_class(&ce TSRMLS_CC);
    ce_kafka_topic->ce_flags = ZEND_ACC_EXPLICIT_ABSTRACT_CLASS;
    ce_kafka_topic->create_object = kafka_topic_new;

    INIT_NS_CLASS_ENTRY(ce, "RdKafka", "ConsumerTopic", kafka_consumer_topic_fe);
    ce_kafka_consumer_topic = rdkafka_register_internal_class_ex(&ce, ce_kafka_topic TSRMLS_CC);

    INIT_NS_CLASS_ENTRY(ce, "RdKafka", "KafkaConsumerTopic", kafka_kafka_consumer_topic_fe);
    ce_kafka_kafka_consumer_topic = rdkafka_register_internal_class_ex(&ce, ce_kafka_topic TSRMLS_CC);

    INIT_NS_CLASS_ENTRY(ce, "RdKafka", "ProducerTopic", kafka_producer_topic_fe);
    ce_kafka_producer_topic = rdkafka_register_internal_class_ex(&ce, ce_kafka_topic TSRMLS_CC);
} /* }}} */
