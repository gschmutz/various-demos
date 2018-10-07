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

/* $Id$ */

#ifdef HAVE_CONFIG_H
#include "config.h"
#endif

#include "php.h"
#include "php_ini.h"
#include "ext/standard/info.h"
#include "php_rdkafka.h"
#include "php_rdkafka_priv.h"
#include "librdkafka/rdkafka.h"
#include "Zend/zend_exceptions.h"
#include "ext/spl/spl_exceptions.h"
#include "metadata.h"
#include "conf.h"
#include "topic.h"
#include "queue.h"
#include "message.h"
#include "kafka_consumer.h"
#include "topic_partition.h"
#include "fun.h"

enum {
   RD_KAFKA_LOG_PRINT = 100
   , RD_KAFKA_LOG_SYSLOG = 101
   , RD_KAFKA_LOG_SYSLOG_PRINT = 102
};

typedef struct _toppar {
    rd_kafka_topic_t    *rkt;
    int32_t             partition;
} toppar;

static const zend_function_entry empty_function_entries[] = {
    PHP_FE_END
};

static zend_object_handlers kafka_object_handlers;
zend_object_handlers kafka_default_object_handlers;

static zend_class_entry * ce_kafka;
static zend_class_entry * ce_kafka_consumer;
zend_class_entry * ce_kafka_exception;
static zend_class_entry * ce_kafka_producer;

static void stop_consuming_toppar_pp(toppar ** tp) {
    rd_kafka_consume_stop((*tp)->rkt, (*tp)->partition);
}

static void stop_consuming(kafka_object * intern TSRMLS_DC) {
    zend_hash_apply(&intern->consuming, (apply_func_t)stop_consuming_toppar_pp TSRMLS_CC);
}

static void kafka_free(zend_object *object TSRMLS_DC) /* {{{ */
{
    kafka_object *intern = get_custom_object(kafka_object, object);

    if (intern->rk) {
        if (intern->type == RD_KAFKA_CONSUMER) {
            stop_consuming(intern TSRMLS_CC);
            zend_hash_destroy(&intern->consuming);
            zend_hash_destroy(&intern->queues);
        }
        zend_hash_destroy(&intern->topics);

        while (rd_kafka_outq_len(intern->rk) > 0) {
            rd_kafka_poll(intern->rk, 1);
        }
        rd_kafka_destroy(intern->rk);
        intern->rk = NULL;
    }

    kafka_conf_callbacks_dtor(&intern->cbs TSRMLS_CC);

    zend_object_std_dtor(&intern->std TSRMLS_CC);

    free_custom_object(intern);
}
/* }}} */

static void toppar_pp_dtor(toppar ** tp) {
    efree(*tp);
}

static void kafka_queue_object_pre_free(kafka_queue_object ** pp) {
    kafka_queue_object *intern = *pp;
    rd_kafka_queue_destroy(intern->rkqu);
    intern->rkqu = NULL;
    zval_ptr_dtor(&intern->zrk);
}

static void kafka_topic_object_pre_free(kafka_topic_object ** pp) {
    kafka_topic_object *intern = *pp;
    rd_kafka_topic_destroy(intern->rkt);
    intern->rkt = NULL;
    zval_ptr_dtor(&intern->zrk);
}

static void kafka_init(zval *this_ptr, rd_kafka_type_t type, zval *zconf TSRMLS_DC) /* {{{ */
{
    char errstr[512];
    rd_kafka_t *rk;
    kafka_object *intern;
    kafka_conf_object *conf_intern;
    rd_kafka_conf_t *conf = NULL;

    intern = get_custom_object_zval(kafka_object, this_ptr);
    intern->type = type;

    if (zconf) {
        conf_intern = get_kafka_conf_object(zconf TSRMLS_CC);
        if (conf_intern) {
            conf = rd_kafka_conf_dup(conf_intern->u.conf);
            kafka_conf_callbacks_copy(&intern->cbs, &conf_intern->cbs TSRMLS_CC);
            intern->cbs.rk = *this_ptr;
            rd_kafka_conf_set_opaque(conf, &intern->cbs);
        }
    }

    rk = rd_kafka_new(type, conf, errstr, sizeof(errstr));

    if (rk == NULL) {
        zend_throw_exception(ce_kafka_exception, errstr, 0 TSRMLS_CC);
        return;
    }

    intern->rk = rk;

    if (type == RD_KAFKA_CONSUMER) {
        zend_hash_init(&intern->consuming, 0, NULL, (dtor_func_t)toppar_pp_dtor, 0);
        zend_hash_init(&intern->queues, 0, NULL, (dtor_func_t)kafka_queue_object_pre_free, 0);
    }

    zend_hash_init(&intern->topics, 0, NULL, (dtor_func_t)kafka_topic_object_pre_free, 0);
}
/* }}} */

static zend_object_value kafka_new(zend_class_entry *class_type TSRMLS_DC) /* {{{ */
{
    zend_object_value retval;
    kafka_object *intern;

    intern = alloc_object(intern, class_type);
    zend_object_std_init(&intern->std, class_type TSRMLS_CC);
    object_properties_init(&intern->std, class_type);

    STORE_OBJECT(retval, intern, (zend_objects_store_dtor_t) zend_objects_destroy_object, kafka_free, NULL);
    SET_OBJECT_HANDLERS(retval, &kafka_object_handlers);

    return retval;
}
/* }}} */

kafka_object * get_kafka_object(zval *zrk TSRMLS_DC)
{
    kafka_object *ork = get_custom_object_zval(kafka_object, zrk);

    if (!ork->rk) {
        zend_throw_exception_ex(NULL, 0 TSRMLS_CC, "RdKafka\\Kafka::__construct() has not been called" TSRMLS_CC);
        return NULL;
    }

    return ork;
}

static void kafka_log_syslog_print(const rd_kafka_t *rk, int level, const char *fac, const char *buf) {
    rd_kafka_log_print(rk, level, fac, buf);
#ifndef _MSC_VER
    rd_kafka_log_syslog(rk, level, fac, buf);
#endif
}

void add_consuming_toppar(kafka_object * intern, rd_kafka_topic_t * rkt, int32_t partition) {
    char *key = NULL;
    int key_len;
    const char *topic_name = rd_kafka_topic_name(rkt);
    toppar *tp;

    tp = emalloc(sizeof(*tp));
    tp->rkt = rkt;
    tp->partition = partition;

    key_len = spprintf(&key, 0, "%s:%d", topic_name, partition);

    zend_hash_str_add_ptr(&intern->consuming, key, key_len+1, tp);

    efree(key);
}

void del_consuming_toppar(kafka_object * intern, rd_kafka_topic_t * rkt, int32_t partition) {
    char *key = NULL;
    int key_len;
    const char *topic_name = rd_kafka_topic_name(rkt);

    key_len = spprintf(&key, 0, "%s:%d", topic_name, partition);

    zend_hash_str_del(&intern->consuming, key, key_len+1);

    efree(key);
}

int is_consuming_toppar(kafka_object * intern, rd_kafka_topic_t * rkt, int32_t partition) {
    char *key = NULL;
    int key_len;
    const char *topic_name = rd_kafka_topic_name(rkt);
    int ret;

    key_len = spprintf(&key, 0, "%s:%d", topic_name, partition);

    ret = zend_hash_str_exists(&intern->consuming, key, key_len+1);

    efree(key);

    return ret;
}

/* {{{ private constructor */
ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka___private_construct, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka, __construct)
{
    zend_throw_exception(NULL, "Private constructor", 0 TSRMLS_CC);
    return;
}
/* }}} */

/* {{{ proto RdKafka\Consumer::__construct([RdKafka\Conf $conf]) */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_consumer___construct, 0, 0, 0)
    ZEND_ARG_INFO(0, conf)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Consumer, __construct)
{
    zval *zconf = NULL;
    zend_error_handling error_handling;

    zend_replace_error_handling(EH_THROW, spl_ce_InvalidArgumentException, &error_handling TSRMLS_CC);

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "|O!", &zconf, ce_kafka_conf) == FAILURE) {
        zend_restore_error_handling(&error_handling TSRMLS_CC);
        return;
    }

    kafka_init(getThis(), RD_KAFKA_CONSUMER, zconf TSRMLS_CC);

    zend_restore_error_handling(&error_handling TSRMLS_CC);
}
/* }}} */

static const zend_function_entry kafka_consumer_fe[] = {
    PHP_ME(RdKafka__Consumer, __construct, arginfo_kafka_consumer___construct, ZEND_ACC_PUBLIC)
    PHP_FE_END
};

/* {{{ proto int RdKafka\Kafka::addBrokers(string $brokerList)
   Returns the number of brokers successfully added */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_add_brokers, 0, 0, 1)
    ZEND_ARG_INFO(0, broker_list)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Kafka, addBrokers)
{
    char *broker_list;
    arglen_t broker_list_len;
    kafka_object *intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "s", &broker_list, &broker_list_len) == FAILURE) {
        return;
    }

    intern = get_kafka_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    RETURN_LONG(rd_kafka_brokers_add(intern->rk, broker_list));
}
/* }}} */

/* {{{ proto RdKafka\Metadata::getMetadata(bool $all_topics, RdKafka\Topic $only_topic, int $timeout_ms)
   Request Metadata from broker */
ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_get_metadata, 0, 0, 1)
    ZEND_ARG_INFO(0, all_topics)
    ZEND_ARG_INFO(0, only_topic)
    ZEND_ARG_INFO(0, timeout_ms)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Kafka, getMetadata)
{
    zend_bool all_topics;
    zval *only_zrkt;
    long timeout_ms;
    rd_kafka_resp_err_t err;
    kafka_object *intern;
    const rd_kafka_metadata_t *metadata;
    kafka_topic_object *only_orkt = NULL;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "bO!l", &all_topics, &only_zrkt, ce_kafka_topic, &timeout_ms) == FAILURE) {
        return;
    }

    intern = get_kafka_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    if (only_zrkt) {
        only_orkt = get_kafka_topic_object(only_zrkt TSRMLS_CC);
        if (!only_orkt) {
            return;
        }
    }

    err = rd_kafka_metadata(intern->rk, all_topics, only_orkt ? only_orkt->rkt : NULL, &metadata, timeout_ms);

    if (err != RD_KAFKA_RESP_ERR_NO_ERROR) {
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }

    kafka_metadata_init(return_value, metadata TSRMLS_CC);
}
/* }}} */

/* {{{ proto void RdKafka\Kafka::setLogLevel(int $level)
   Specifies the maximum logging level produced by internal kafka logging and debugging */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_set_log_level, 0, 0, 1)
    ZEND_ARG_INFO(0, level)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Kafka, setLogLevel)
{
    kafka_object *intern;
    long level;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "l", &level) == FAILURE) {
        return;
    }

    intern = get_kafka_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    rd_kafka_set_log_level(intern->rk, level);
}
/* }}} */

/* {{{ proto RdKafka\Queue RdKafka\Kafka::newQueue()
   Returns a RdKafka\Queue object */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_new_queue, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Kafka, newQueue)
{
    rd_kafka_queue_t *rkqu;
    kafka_object *intern;
    kafka_queue_object *queue_intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "") == FAILURE) {
        return;
    }

    intern = get_kafka_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    rkqu = rd_kafka_queue_new(intern->rk);

    if (!rkqu) {
        return;
    }

    if (object_init_ex(return_value, ce_kafka_queue) != SUCCESS) {
        return;
    }

    queue_intern = get_custom_object_zval(kafka_queue_object, return_value);
    if (!queue_intern) {
        return;
    }

    queue_intern->rkqu = rkqu;

    // Keep a reference to the parent Kafka object, attempts to ensure that
    // the Queue object is destroyed before the Kafka object.
    // This avoids rd_kafka_destroy() hanging.
#if PHP_MAJOR_VERSION >= 7
    queue_intern->zrk = *getThis();
#else
    queue_intern->zrk = getThis();
#endif
    Z_ADDREF_P(P_ZEVAL(queue_intern->zrk));

    zend_hash_index_add_ptr(&intern->queues, (zend_ulong)queue_intern, queue_intern);
}
/* }}} */

/* {{{ proto RdKafka\Topic RdKafka\Kafka::newTopic(string $topic)
   Returns an RdKafka\Topic object */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_new_topic, 0, 0, 1)
    ZEND_ARG_INFO(0, topic_name)
    ZEND_ARG_INFO(0, topic_conf)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Kafka, newTopic)
{
    char *topic;
    arglen_t topic_len;
    rd_kafka_topic_t *rkt;
    kafka_object *intern;
    kafka_topic_object *topic_intern;
    zend_class_entry *topic_type;
    zval *zconf = NULL;
    rd_kafka_topic_conf_t *conf = NULL;
    kafka_conf_object *conf_intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "s|O!", &topic, &topic_len, &zconf, ce_kafka_topic_conf) == FAILURE) {
        return;
    }

    intern = get_kafka_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    if (zconf) {
        conf_intern = get_kafka_conf_object(zconf TSRMLS_CC);
        if (conf_intern) {
            conf = rd_kafka_topic_conf_dup(conf_intern->u.topic_conf);
        }
    }

    rkt = rd_kafka_topic_new(intern->rk, topic, conf);

    if (!rkt) {
        return;
    }

    switch (intern->type) {
        case RD_KAFKA_CONSUMER:
            topic_type = ce_kafka_consumer_topic;
            break;
        case RD_KAFKA_PRODUCER:
            topic_type = ce_kafka_producer_topic;
            break;
        default:
            return;
    }

    if (object_init_ex(return_value, topic_type) != SUCCESS) {
        return;
    }

    topic_intern = get_custom_object_zval(kafka_topic_object, return_value);
    if (!topic_intern) {
        return;
    }

    topic_intern->rkt = rkt;
#if PHP_MAJOR_VERSION >= 7
    topic_intern->zrk = *getThis();
#else
    topic_intern->zrk = getThis();
#endif
    Z_ADDREF_P(P_ZEVAL(topic_intern->zrk));

    zend_hash_index_add_ptr(&intern->topics, (zend_ulong)topic_intern, topic_intern);
}
/* }}} */

/* {{{ proto int RdKafka\Kafka::getOutQLen()
   Returns the current out queue length */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_get_outq_len, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Kafka, getOutQLen)
{
    kafka_object *intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "") == FAILURE) {
        return;
    }

    intern = get_kafka_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    RETURN_LONG(rd_kafka_outq_len(intern->rk));
}
/* }}} */

/* {{{ proto int RdKafka\Kafka::poll(int $timeout_ms)
   Polls the provided kafka handle for events */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_poll, 0, 0, 1)
    ZEND_ARG_INFO(0, timeout_ms)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Kafka, poll)
{
    kafka_object *intern;
    long timeout;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "l", &timeout) == FAILURE) {
        return;
    }

    intern = get_kafka_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    RETURN_LONG(rd_kafka_poll(intern->rk, timeout));
}
/* }}} */

/* {{{ proto void RdKafka::setLogger(mixed $logger)
   Sets the log callback */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_set_logger, 0, 0, 1)
    ZEND_ARG_INFO(0, logger)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Kafka, setLogger)
{
    kafka_object *intern;
    long id;
    void (*logger) (const rd_kafka_t * rk, int level, const char *fac, const char *buf);

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "l", &id) == FAILURE) {
        return;
    }

    intern = get_kafka_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    switch (id) {
        case RD_KAFKA_LOG_PRINT:
            logger = rd_kafka_log_print;
            break;
#ifndef _MSC_VER
        case RD_KAFKA_LOG_SYSLOG:
            logger = rd_kafka_log_syslog;
            break;
#endif
        case RD_KAFKA_LOG_SYSLOG_PRINT:
            logger = kafka_log_syslog_print;
            break;
        default:
            zend_throw_exception_ex(NULL, 0 TSRMLS_CC, "Invalid logger" TSRMLS_CC);
            return;
    }

    rd_kafka_set_logger(intern->rk, logger);
}
/* }}} */

static const zend_function_entry kafka_fe[] = {
    PHP_ME(RdKafka__Kafka, addBrokers, arginfo_kafka_add_brokers, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Kafka, getMetadata, arginfo_kafka_get_metadata, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Kafka, getOutQLen, arginfo_kafka_get_outq_len, ZEND_ACC_PUBLIC)
    PHP_MALIAS(RdKafka__Kafka, metadata, getMetadata, arginfo_kafka_get_metadata, ZEND_ACC_PUBLIC | ZEND_ACC_DEPRECATED)
    PHP_ME(RdKafka__Kafka, setLogLevel, arginfo_kafka_set_log_level, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Kafka, newQueue, arginfo_kafka_new_queue, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Kafka, newTopic, arginfo_kafka_new_topic, ZEND_ACC_PUBLIC)
    PHP_MALIAS(RdKafka__Kafka, outqLen, getOutQLen, arginfo_kafka_get_outq_len, ZEND_ACC_PUBLIC | ZEND_ACC_DEPRECATED)
    PHP_ME(RdKafka__Kafka, poll, arginfo_kafka_poll, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Kafka, setLogger, arginfo_kafka_set_logger, ZEND_ACC_PUBLIC)
    PHP_FE_END
};

/* {{{ proto RdKafka\Producer::__construct([RdKafka\Conf $conf]) */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_producer___construct, 0, 0, 0)
    ZEND_ARG_INFO(0, conf)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Producer, __construct)
{
    zval *zconf = NULL;
    zend_error_handling error_handling;

    zend_replace_error_handling(EH_THROW, spl_ce_InvalidArgumentException, &error_handling TSRMLS_CC);

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "|O!", &zconf, ce_kafka_conf) == FAILURE) {
        zend_restore_error_handling(&error_handling TSRMLS_CC);
        return;
    }

    kafka_init(getThis(), RD_KAFKA_PRODUCER, zconf TSRMLS_CC);

    zend_restore_error_handling(&error_handling TSRMLS_CC);
}
/* }}} */

static const zend_function_entry kafka_producer_fe[] = {
    PHP_ME(RdKafka__Producer, __construct, arginfo_kafka_producer___construct, ZEND_ACC_PUBLIC)
    PHP_FE_END
};

#define COPY_CONSTANT(name) \
    REGISTER_LONG_CONSTANT(#name, name, CONST_CS | CONST_PERSISTENT)

void register_err_constants(INIT_FUNC_ARGS) /* {{{ */
{
#ifdef HAVE_RD_KAFKA_GET_ERR_DESCS
    const struct rd_kafka_err_desc *errdescs;
    size_t cnt;
    size_t i;
    char buf[128];

    rd_kafka_get_err_descs(&errdescs, &cnt);

    for (i = 0; i < cnt; i++) {
        const struct rd_kafka_err_desc *desc = &errdescs[i];
        int len;

        if (!desc->name) {
            continue;
        }

        len = snprintf(buf, sizeof(buf), "RD_KAFKA_RESP_ERR_%s", desc->name);
        if ((size_t)len >= sizeof(buf)) {
            len = sizeof(buf)-1;
        }

#if PHP_MAJOR_VERSION < 7
		len += 1;
#endif

        zend_register_long_constant(buf, len, desc->code, CONST_CS | CONST_PERSISTENT, module_number TSRMLS_CC);
    }

#else /* HAVE_RD_KAFKA_GET_ERR_DESCS */

    COPY_CONSTANT(RD_KAFKA_RESP_ERR__BEGIN);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__BAD_MSG);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__BAD_COMPRESSION);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__DESTROY);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__FAIL);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__TRANSPORT);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__CRIT_SYS_RESOURCE);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__RESOLVE);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__MSG_TIMED_OUT);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__PARTITION_EOF);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__UNKNOWN_PARTITION);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__FS);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__UNKNOWN_TOPIC);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__ALL_BROKERS_DOWN);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__INVALID_ARG);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__TIMED_OUT);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__QUEUE_FULL);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__ISR_INSUFF);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR__END);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_UNKNOWN);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_NO_ERROR);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_OFFSET_OUT_OF_RANGE);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_INVALID_MSG);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_UNKNOWN_TOPIC_OR_PART);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_INVALID_MSG_SIZE);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_LEADER_NOT_AVAILABLE);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_NOT_LEADER_FOR_PARTITION);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_REQUEST_TIMED_OUT);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_BROKER_NOT_AVAILABLE);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_REPLICA_NOT_AVAILABLE);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_MSG_SIZE_TOO_LARGE);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_STALE_CTRL_EPOCH);
    COPY_CONSTANT(RD_KAFKA_RESP_ERR_OFFSET_METADATA_TOO_LARGE);
#endif /* HAVE_RD_KAFKA_GET_ERR_DESCS */
} /* }}} */

/* {{{ PHP_MINIT_FUNCTION
 */
PHP_MINIT_FUNCTION(rdkafka)
{
    COPY_CONSTANT(RD_KAFKA_CONSUMER);
    COPY_CONSTANT(RD_KAFKA_OFFSET_BEGINNING);
    COPY_CONSTANT(RD_KAFKA_OFFSET_END);
    COPY_CONSTANT(RD_KAFKA_OFFSET_STORED);
    COPY_CONSTANT(RD_KAFKA_PARTITION_UA);
    COPY_CONSTANT(RD_KAFKA_PRODUCER);
    REGISTER_LONG_CONSTANT("RD_KAFKA_VERSION", rd_kafka_version(), CONST_CS | CONST_PERSISTENT);
    REGISTER_LONG_CONSTANT("RD_KAFKA_BUILD_VERSION", RD_KAFKA_VERSION, CONST_CS | CONST_PERSISTENT);

    register_err_constants(INIT_FUNC_ARGS_PASSTHRU);

    COPY_CONSTANT(RD_KAFKA_CONF_UNKNOWN);
    COPY_CONSTANT(RD_KAFKA_CONF_INVALID);
    COPY_CONSTANT(RD_KAFKA_CONF_OK);

    REGISTER_LONG_CONSTANT("RD_KAFKA_MSG_PARTITIONER_RANDOM", MSG_PARTITIONER_RANDOM, CONST_CS | CONST_PERSISTENT);
#ifdef HAVE_RD_KAFKA_MSG_PARTIIONER_CONSISTENT
    REGISTER_LONG_CONSTANT("RD_KAFKA_MSG_PARTITIONER_CONSISTENT", MSG_PARTITIONER_CONSISTENT, CONST_CS | CONST_PERSISTENT);
#endif

    REGISTER_LONG_CONSTANT("RD_KAFKA_LOG_PRINT", RD_KAFKA_LOG_PRINT, CONST_CS | CONST_PERSISTENT);
    REGISTER_LONG_CONSTANT("RD_KAFKA_LOG_SYSLOG", RD_KAFKA_LOG_SYSLOG, CONST_CS | CONST_PERSISTENT);
    REGISTER_LONG_CONSTANT("RD_KAFKA_LOG_SYSLOG_PRINT", RD_KAFKA_LOG_SYSLOG_PRINT, CONST_CS | CONST_PERSISTENT);
    zend_class_entry ce;

    memcpy(&kafka_default_object_handlers, zend_get_std_object_handlers(), sizeof(zend_object_handlers));
    kafka_default_object_handlers.clone_obj = NULL;

	kafka_object_handlers = kafka_default_object_handlers;
    set_object_handler_free_obj(&kafka_object_handlers, kafka_free);
    set_object_handler_offset(&kafka_object_handlers, XtOffsetOf(kafka_object, std));

    INIT_CLASS_ENTRY(ce, "RdKafka", kafka_fe);
    ce_kafka = zend_register_internal_class(&ce TSRMLS_CC);
    ce_kafka->ce_flags |= ZEND_ACC_EXPLICIT_ABSTRACT_CLASS;
    ce_kafka->create_object = kafka_new;

    zend_declare_property_null(ce_kafka, ZEND_STRL("error_cb"), ZEND_ACC_PRIVATE TSRMLS_CC);
    zend_declare_property_null(ce_kafka, ZEND_STRL("dr_cb"), ZEND_ACC_PRIVATE TSRMLS_CC);

    INIT_NS_CLASS_ENTRY(ce, "RdKafka", "Consumer", kafka_consumer_fe);
    ce_kafka_consumer = rdkafka_register_internal_class_ex(&ce, ce_kafka TSRMLS_CC);

    INIT_NS_CLASS_ENTRY(ce, "RdKafka", "Producer", kafka_producer_fe);
    ce_kafka_producer = rdkafka_register_internal_class_ex(&ce, ce_kafka TSRMLS_CC);

    INIT_NS_CLASS_ENTRY(ce, "RdKafka", "Exception", NULL);
    ce_kafka_exception = rdkafka_register_internal_class_ex(&ce, zend_exception_get_default(TSRMLS_C) TSRMLS_CC);

    kafka_conf_minit(TSRMLS_C);
    kafka_kafka_consumer_minit(TSRMLS_C);
    kafka_message_minit(TSRMLS_C);
    kafka_metadata_minit(TSRMLS_C);
    kafka_metadata_topic_partition_minit(TSRMLS_C);
    kafka_queue_minit(TSRMLS_C);
    kafka_topic_minit(TSRMLS_C);

    return SUCCESS;
}
/* }}} */

/* {{{ PHP_MSHUTDOWN_FUNCTION
 */
PHP_MSHUTDOWN_FUNCTION(rdkafka)
{
    rd_kafka_wait_destroyed(1000);
    return SUCCESS;
}
/* }}} */

/* {{{ PHP_MINFO_FUNCTION
 */
PHP_MINFO_FUNCTION(rdkafka)
{
    char *rd_kafka_version;

    php_info_print_table_start();
    php_info_print_table_header(2, "rdkafka support", "enabled");

    php_info_print_table_header(2, "version", PHP_RDKAFKA_VERSION);
    php_info_print_table_header(2, "build date", __DATE__ " " __TIME__);

    spprintf(
        &rd_kafka_version,
        0,
        "%u.%u.%u.%u",
        (RD_KAFKA_VERSION & 0xFF000000) >> 24,
        (RD_KAFKA_VERSION & 0x00FF0000) >> 16,
        (RD_KAFKA_VERSION & 0x0000FF00) >> 8,
        (RD_KAFKA_VERSION & 0x000000FF) >> 8
    );

    php_info_print_table_header(2, "librdkafka version (runtime)", rd_kafka_version_str());
    php_info_print_table_header(2, "librdkafka version (build)", rd_kafka_version);


    efree(rd_kafka_version);

    php_info_print_table_end();
}
/* }}} */

/* {{{ rdkafka_module_entry
 */
zend_module_entry rdkafka_module_entry = {
    STANDARD_MODULE_HEADER,
    "rdkafka",
    rdkafka_functions,
    PHP_MINIT(rdkafka),
    PHP_MSHUTDOWN(rdkafka),
    NULL,
    NULL,
    PHP_MINFO(rdkafka),
    PHP_RDKAFKA_VERSION,
    STANDARD_MODULE_PROPERTIES
};
/* }}} */

#ifdef COMPILE_DL_RDKAFKA
ZEND_GET_MODULE(rdkafka)
#endif

/*
 * Local variables:
 * tab-width: 4
 * c-basic-offset: 4
 * End:
 * vim600: noet sw=4 ts=4 fdm=marker
 * vim<600: noet sw=4 ts=4
 */
