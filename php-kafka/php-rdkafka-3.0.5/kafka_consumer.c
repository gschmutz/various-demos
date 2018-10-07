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
#include "Zend/zend_exceptions.h"
#include "ext/spl/spl_exceptions.h"
#include "conf.h"
#include "topic_partition.h"
#include "topic.h"
#include "message.h"
#include "metadata.h"

typedef struct _object_intern {
#if PHP_MAJOR_VERSION < 7
    zend_object             std;
#endif
    rd_kafka_t              *rk;
    kafka_conf_callbacks    cbs;
#if PHP_MAJOR_VERSION >= 7
    zend_object             std;
#endif
} object_intern;

static zend_class_entry * ce;
static zend_object_handlers handlers;

static void kafka_consumer_free(zend_object *object TSRMLS_DC) /* {{{ */
{
    object_intern *intern = get_custom_object(object_intern, object);
    rd_kafka_resp_err_t err;

    if (intern->rk) {
        err = rd_kafka_consumer_close(intern->rk);
        if (err) {
            php_error(E_WARNING, "rd_kafka_consumer_close failed: %s", rd_kafka_err2str(err));
        } else {
            while (rd_kafka_outq_len(intern->rk) > 0) {
                rd_kafka_poll(intern->rk, 10);
            }
        }
        rd_kafka_destroy(intern->rk);
        intern->rk = NULL;
    }

    kafka_conf_callbacks_dtor(&intern->cbs TSRMLS_CC);

    zend_object_std_dtor(&intern->std TSRMLS_CC);

    free_custom_object(intern);
}
/* }}} */

static zend_object_value kafka_consumer_new(zend_class_entry *class_type TSRMLS_DC) /* {{{ */
{
    zend_object_value retval;
    object_intern *intern;

    intern = alloc_object(intern, class_type);
    zend_object_std_init(&intern->std, class_type TSRMLS_CC);
    object_properties_init(&intern->std, class_type);

    STORE_OBJECT(retval, intern, (zend_objects_store_dtor_t) zend_objects_destroy_object, kafka_consumer_free, NULL);
    SET_OBJECT_HANDLERS(retval, &handlers);

    return retval;
}
/* }}} */

static object_intern * get_object(zval *zconsumer TSRMLS_DC) /* {{{ */
{
    object_intern *oconsumer = get_custom_object_zval(object_intern, zconsumer);

    if (!oconsumer->rk) {
        zend_throw_exception_ex(NULL, 0 TSRMLS_CC, "RdKafka\\KafkaConsumer::__construct() has not been called");
        return NULL;
    }

    return oconsumer;
} /* }}} */

static int has_group_id(rd_kafka_conf_t *conf) { /* {{{ */

    size_t len;

    if (conf == NULL) {
        return 0;
    }

    if (rd_kafka_conf_get(conf, "group.id", NULL, &len) != RD_KAFKA_CONF_OK) {
        return 0;
    }

    if (len <= 1) {
        return 0;
    }

    return 1;
} /* }}} */

/* {{{ proto RdKafka\KafkaConsumer::__construct(RdKafka\Conf $conf) */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_kafka_consumer___construct, 0, 0, 1)
    ZEND_ARG_INFO(0, conf)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__KafkaConsumer, __construct)
{
    zval *zconf;
    zend_error_handling error_handling;
    char errstr[512];
    rd_kafka_t *rk;
    object_intern *intern;
    kafka_conf_object *conf_intern;
    rd_kafka_conf_t *conf = NULL;

    zend_replace_error_handling(EH_THROW, spl_ce_InvalidArgumentException, &error_handling TSRMLS_CC);

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "O", &zconf, ce_kafka_conf) == FAILURE) {
        zend_restore_error_handling(&error_handling TSRMLS_CC);
        return;
    }

    intern = get_custom_object_zval(object_intern, getThis());

    conf_intern = get_kafka_conf_object(zconf TSRMLS_CC);
    if (conf_intern) {
        conf = rd_kafka_conf_dup(conf_intern->u.conf);
        kafka_conf_callbacks_copy(&intern->cbs, &conf_intern->cbs TSRMLS_CC);
        intern->cbs.rk = *getThis();
        rd_kafka_conf_set_opaque(conf, &intern->cbs);
    }

    if (!has_group_id(conf)) {
        if (conf) {
            rd_kafka_conf_destroy(conf);
        }
        zend_throw_exception(ce_kafka_exception, "\"group.id\" must be configured", 0 TSRMLS_CC);
        return;
    }

    rk = rd_kafka_new(RD_KAFKA_CONSUMER, conf, errstr, sizeof(errstr));

    if (rk == NULL) {
        zend_restore_error_handling(&error_handling TSRMLS_CC);
        zend_throw_exception(ce_kafka_exception, errstr, 0 TSRMLS_CC);
        return;
    }

    intern->rk = rk;

    rd_kafka_poll_set_consumer(rk);

    zend_restore_error_handling(&error_handling TSRMLS_CC);
}
/* }}} */

/* {{{ proto void RdKafka\KafkaConsumer::assign([array $topics])
    Atomic assignment of partitions to consume */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_kafka_consumer_assign, 0, 0, 0)
    ZEND_ARG_INFO(0, topic_partitions)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__KafkaConsumer, assign)
{
    HashTable *htopars = NULL;
    rd_kafka_topic_partition_list_t *topics;
    rd_kafka_resp_err_t err;
    object_intern *intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "|h!", &htopars) == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    if (htopars) {
        topics = array_arg_to_kafka_topic_partition_list(1, htopars TSRMLS_CC);
        if (!topics) {
            return;
        }
    } else {
        topics = NULL;
    }

    err = rd_kafka_assign(intern->rk, topics);

    if (topics) {
        rd_kafka_topic_partition_list_destroy(topics);
    }

    if (err != RD_KAFKA_RESP_ERR_NO_ERROR) {
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }
}
/* }}} */

/* {{{ proto array RdKafka\KafkaConsumer::getAssignment()
    Returns the current partition getAssignment */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_kafka_consumer_getAssignment, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__KafkaConsumer, getAssignment)
{
    rd_kafka_resp_err_t err;
    rd_kafka_topic_partition_list_t *topics;
    object_intern *intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "") == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    err = rd_kafka_assignment(intern->rk, &topics);

    if (err != RD_KAFKA_RESP_ERR_NO_ERROR) {
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }

    kafka_topic_partition_list_to_array(return_value, topics TSRMLS_CC);
    rd_kafka_topic_partition_list_destroy(topics);
}
/* }}} */

/* {{{ proto void RdKafka\KafkaConsumer::subscribe(array $topics)
    Update the subscription set to $topics */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_kafka_consumer_subscribe, 0, 0, 1)
    ZEND_ARG_INFO(0, topics)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__KafkaConsumer, subscribe)
{
    HashTable *htopics;
    HashPosition pos;
    object_intern *intern;
    rd_kafka_topic_partition_list_t *topics;
    rd_kafka_resp_err_t err;
    zeval *zv;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "h", &htopics) == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    topics = rd_kafka_topic_partition_list_new(zend_hash_num_elements(htopics));

    for (zend_hash_internal_pointer_reset_ex(htopics, &pos);
            (zv = rdkafka_hash_get_current_data_ex(htopics, &pos)) != NULL;
            zend_hash_move_forward_ex(htopics, &pos)) {
        convert_to_string_ex(zv);
        rd_kafka_topic_partition_list_add(topics, Z_STRVAL_P(ZEVAL(zv)), RD_KAFKA_PARTITION_UA);
    }

    err = rd_kafka_subscribe(intern->rk, topics);

    rd_kafka_topic_partition_list_destroy(topics);

    if (err != RD_KAFKA_RESP_ERR_NO_ERROR) {
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }
}
/* }}} */

/* {{{ proto array RdKafka\KafkaConsumer::getSubscription()
   Returns the current subscription as set by subscribe() */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_kafka_consumer_getSubscription, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__KafkaConsumer, getSubscription)
{
    rd_kafka_resp_err_t err;
    rd_kafka_topic_partition_list_t *topics;
    object_intern *intern;
    int i;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "") == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    err = rd_kafka_subscription(intern->rk, &topics);

    if (err != RD_KAFKA_RESP_ERR_NO_ERROR) {
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }

    array_init_size(return_value, topics->cnt);

    for (i = 0; i < topics->cnt; i++) {
        add_next_index_string(return_value, topics->elems[i].topic ZEVAL_DUP_CC);
    }

    rd_kafka_topic_partition_list_destroy(topics);
}
/* }}} */

/* {{{ proto void RdKafka\KafkaConsumer::unsubsribe()
    Unsubscribe from the current subscription set */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_kafka_consumer_unsubscribe, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__KafkaConsumer, unsubscribe)
{
    object_intern *intern;
    rd_kafka_resp_err_t err;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "") == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    err = rd_kafka_unsubscribe(intern->rk);

    if (err != RD_KAFKA_RESP_ERR_NO_ERROR) {
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }
}
/* }}} */

/* {{{ proto Message RdKafka\KafkaConsumer::consume()
   Consume message or get error event, triggers callbacks */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_kafka_consumer_consume, 0, 0, 1)
    ZEND_ARG_INFO(0, timeout_ms)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__KafkaConsumer, consume)
{
    object_intern *intern;
    long timeout_ms;
    rd_kafka_message_t *rkmessage, rkmessage_tmp = {0};

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "l", &timeout_ms) == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    rkmessage = rd_kafka_consumer_poll(intern->rk, timeout_ms);

    if (!rkmessage) {
        rkmessage_tmp.err = RD_KAFKA_RESP_ERR__TIMED_OUT;
        rkmessage = &rkmessage_tmp;
    }

    kafka_message_new(return_value, rkmessage TSRMLS_CC);

    if (rkmessage != &rkmessage_tmp) {
        rd_kafka_message_destroy(rkmessage);
    }
}
/* }}} */

static void consumer_commit(int async, INTERNAL_FUNCTION_PARAMETERS) /* {{{ */
{
    zval *zarg = NULL;
    object_intern *intern;
    rd_kafka_topic_partition_list_t *offsets = NULL;
    rd_kafka_resp_err_t err;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "|z!", &zarg) == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    if (zarg) {
        if (Z_TYPE_P(zarg) == IS_OBJECT && instanceof_function(Z_OBJCE_P(zarg), ce_kafka_message TSRMLS_CC)) {
            zval *zerr;
            zval *ztopic;
            zval *zpartition;
            zval *zoffset;
            rd_kafka_topic_partition_t *rktpar;

            zerr = rdkafka_read_property(NULL, zarg, ZEND_STRL("err"), 0 TSRMLS_CC);
            if (zerr && Z_TYPE_P(zerr) != IS_NULL && (Z_TYPE_P(zerr) != IS_LONG || Z_LVAL_P(zerr) != RD_KAFKA_RESP_ERR_NO_ERROR)) {
                zend_throw_exception(ce_kafka_exception, "Invalid argument: Specified Message has an error", RD_KAFKA_RESP_ERR__INVALID_ARG TSRMLS_CC);
                return;
            }

            ztopic = rdkafka_read_property(NULL, zarg, ZEND_STRL("topic_name"), 0 TSRMLS_CC);
            if (!ztopic || Z_TYPE_P(ztopic) != IS_STRING) {
                zend_throw_exception(ce_kafka_exception, "Invalid argument: Specified Message's topic_name is not a string", RD_KAFKA_RESP_ERR__INVALID_ARG TSRMLS_CC);
                return;
            }

            zpartition = rdkafka_read_property(NULL, zarg, ZEND_STRL("partition"), 0 TSRMLS_CC);
            if (!zpartition || Z_TYPE_P(zpartition) != IS_LONG) {
                zend_throw_exception(ce_kafka_exception, "Invalid argument: Specified Message's partition is not an int", RD_KAFKA_RESP_ERR__INVALID_ARG TSRMLS_CC);
                return;
            }

            zoffset = rdkafka_read_property(NULL, zarg, ZEND_STRL("offset"), 0 TSRMLS_CC);
            if (!zoffset || Z_TYPE_P(zoffset) != IS_LONG) {
                zend_throw_exception(ce_kafka_exception, "Invalid argument: Specified Message's offset is not an int", RD_KAFKA_RESP_ERR__INVALID_ARG TSRMLS_CC);
                return;
            }

            offsets = rd_kafka_topic_partition_list_new(1);
            rktpar = rd_kafka_topic_partition_list_add(
                    offsets, Z_STRVAL_P(ztopic),
                    Z_LVAL_P(zpartition));
            rktpar->offset = Z_LVAL_P(zoffset)+1;

        } else if (Z_TYPE_P(zarg) == IS_ARRAY) {
            HashTable *ary = Z_ARRVAL_P(zarg);
            offsets = array_arg_to_kafka_topic_partition_list(1, ary TSRMLS_CC);
            if (!offsets) {
                return;
            }
        } else if (Z_TYPE_P(zarg) != IS_NULL) {
            php_error(E_ERROR,
                    "RdKafka\\KafkaConsumer::%s() expects parameter %d to be %s, %s given",
                    get_active_function_name(TSRMLS_C),
                    1,
                    "an instance of RdKafka\\Message or an array of RdKafka\\TopicPartition",
                    zend_zval_type_name(zarg));
            return;
        }
    }

    err = rd_kafka_commit(intern->rk, offsets, async);

    if (offsets) {
        rd_kafka_topic_partition_list_destroy(offsets);
    }

    if (err != RD_KAFKA_RESP_ERR_NO_ERROR) {
        zend_throw_exception(ce_kafka_exception, rd_kafka_err2str(err), err TSRMLS_CC);
        return;
    }
}
/* }}} */

/* {{{ proto void RdKafka\KafkaConsumer::commit([mixed $message_or_offsets])
   Commit offsets */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_kafka_consumer_commit, 0, 0, 0)
    ZEND_ARG_INFO(0, message_or_offsets)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__KafkaConsumer, commit)
{
    consumer_commit(0, INTERNAL_FUNCTION_PARAM_PASSTHRU);
}
/* }}} */

/* {{{ proto void RdKafka\KafkaConsumer::commitAsync([mixed $message_or_offsets])
   Commit offsets */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_kafka_consumer_commit_async, 0, 0, 0)
    ZEND_ARG_INFO(0, message_or_offsets)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__KafkaConsumer, commitAsync)
{
    consumer_commit(1, INTERNAL_FUNCTION_PARAM_PASSTHRU);
}
/* }}} */

/* {{{ proto Metadata RdKafka\KafkaConsumer::getMetadata(bool all_topics, RdKafka\Topic only_topic, int timeout_ms)
   Request Metadata from broker */
ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_kafka_consumer_getMetadata, 0, 0, 3)
    ZEND_ARG_INFO(0, all_topics)
    ZEND_ARG_INFO(0, only_topic)
    ZEND_ARG_INFO(0, timeout_ms)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__KafkaConsumer, getMetadata)
{
    zend_bool all_topics;
    zval *only_zrkt;
    long timeout_ms;
    rd_kafka_resp_err_t err;
    object_intern *intern;
    const rd_kafka_metadata_t *metadata;
    kafka_topic_object *only_orkt = NULL;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "bO!l", &all_topics, &only_zrkt, ce_kafka_topic, &timeout_ms) == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
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

/* {{{ proto RdKafka\KafkaConsumerTopic RdKafka\KafkaConsumer::newTopic(string $topic)
   Returns a RdKafka\KafkaConsumerTopic object */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_kafka_consumer_new_topic, 0, 0, 1)
    ZEND_ARG_INFO(0, topic_name)
    ZEND_ARG_INFO(0, topic_conf)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__KafkaConsumer, newTopic)
{
    char *topic;
    arglen_t topic_len;
    rd_kafka_topic_t *rkt;
    object_intern *intern;
    kafka_topic_object *topic_intern;
    zval *zconf = NULL;
    rd_kafka_topic_conf_t *conf = NULL;
    kafka_conf_object *conf_intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "s|O!", &topic, &topic_len, &zconf, ce_kafka_topic_conf) == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
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

    if (object_init_ex(return_value, ce_kafka_kafka_consumer_topic) != SUCCESS) {
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
}
/* }}} */

static const zend_function_entry fe[] = { /* {{{ */
    PHP_ME(RdKafka__KafkaConsumer, __construct, arginfo_kafka_kafka_consumer___construct, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__KafkaConsumer, assign, arginfo_kafka_kafka_consumer_assign, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__KafkaConsumer, getAssignment, arginfo_kafka_kafka_consumer_getAssignment, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__KafkaConsumer, commit, arginfo_kafka_kafka_consumer_commit, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__KafkaConsumer, commitAsync, arginfo_kafka_kafka_consumer_commit_async, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__KafkaConsumer, consume, arginfo_kafka_kafka_consumer_consume, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__KafkaConsumer, subscribe, arginfo_kafka_kafka_consumer_subscribe, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__KafkaConsumer, getSubscription, arginfo_kafka_kafka_consumer_getSubscription, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__KafkaConsumer, unsubscribe, arginfo_kafka_kafka_consumer_unsubscribe, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__KafkaConsumer, getMetadata, arginfo_kafka_kafka_consumer_getMetadata, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__KafkaConsumer, newTopic, arginfo_kafka_kafka_consumer_new_topic, ZEND_ACC_PUBLIC)
    PHP_FE_END
}; /* }}} */

void kafka_kafka_consumer_minit(TSRMLS_D) /* {{{ */
{
    zend_class_entry tmpce;

    INIT_NS_CLASS_ENTRY(tmpce, "RdKafka", "KafkaConsumer", fe);
    ce = zend_register_internal_class(&tmpce TSRMLS_CC);
    ce->create_object = kafka_consumer_new;

    handlers = kafka_default_object_handlers;
    set_object_handler_free_obj(&handlers, kafka_consumer_free);
    set_object_handler_offset(&handlers, XtOffsetOf(object_intern, std));

    zend_declare_property_null(ce, ZEND_STRL("error_cb"), ZEND_ACC_PRIVATE TSRMLS_CC);
    zend_declare_property_null(ce, ZEND_STRL("rebalance_cb"), ZEND_ACC_PRIVATE TSRMLS_CC);
    zend_declare_property_null(ce, ZEND_STRL("dr_msg_cb"), ZEND_ACC_PRIVATE TSRMLS_CC);
} /* }}} */
