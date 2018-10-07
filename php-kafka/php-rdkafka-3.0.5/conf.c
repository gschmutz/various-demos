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
#include "php_ini.h"
#include "ext/standard/info.h"
#include "php_rdkafka.h"
#include "php_rdkafka_priv.h"
#include "librdkafka/rdkafka.h"
#include "Zend/zend_exceptions.h"
#include "ext/spl/spl_exceptions.h"
#include "conf.h"
#include "topic_partition.h"
#include "message.h"
#include "zeval.h"

zend_class_entry * ce_kafka_conf;
zend_class_entry * ce_kafka_topic_conf;

static zend_object_handlers handlers;

static void kafka_conf_callback_dtor(kafka_conf_callback *cb TSRMLS_DC) /* {{{ */
{
    if (cb) {
        zval_ptr_dtor(&cb->fci.function_name);
        efree(cb);
    }
} /* }}} */

void kafka_conf_callbacks_dtor(kafka_conf_callbacks *cbs TSRMLS_DC) /* {{{ */
{
    kafka_conf_callback_dtor(cbs->error TSRMLS_CC);
    kafka_conf_callback_dtor(cbs->rebalance TSRMLS_CC);
    kafka_conf_callback_dtor(cbs->dr_msg TSRMLS_CC);
    kafka_conf_callback_dtor(cbs->stats TSRMLS_CC);
} /* }}} */

static void kafka_conf_callback_copy(kafka_conf_callback **to, kafka_conf_callback *from TSRMLS_DC) /* {{{ */
{
    if (from) {
        *to = emalloc(sizeof(**to));
        **to = *from;
#if PHP_MAJOR_VERSION >= 7
        zval_copy_ctor(&(*to)->fci.function_name);
#else
        Z_ADDREF_P((*to)->fci.function_name);
#endif
    }
} /* }}} */

void kafka_conf_callbacks_copy(kafka_conf_callbacks *to, kafka_conf_callbacks *from TSRMLS_DC) /* {{{ */
{
    kafka_conf_callback_copy(&to->error, from->error TSRMLS_CC);
    kafka_conf_callback_copy(&to->rebalance, from->rebalance TSRMLS_CC);
    kafka_conf_callback_copy(&to->dr_msg, from->dr_msg TSRMLS_CC);
    kafka_conf_callback_copy(&to->stats, from->stats TSRMLS_CC);
} /* }}} */

static void kafka_conf_free(zend_object *object TSRMLS_DC) /* {{{ */
{
    kafka_conf_object *intern = get_custom_object(kafka_conf_object, object);

    switch (intern->type) {
        case KAFKA_CONF:
            if (intern->u.conf) {
                rd_kafka_conf_destroy(intern->u.conf);
            }
            kafka_conf_callbacks_dtor(&intern->cbs TSRMLS_CC);
            break;
        case KAFKA_TOPIC_CONF:
            if (intern->u.topic_conf) {
                rd_kafka_topic_conf_destroy(intern->u.topic_conf);
            }
            break;
    }

    zend_object_std_dtor(&intern->std TSRMLS_CC);

    free_custom_object(intern);
}
/* }}} */

static zend_object_value kafka_conf_new(zend_class_entry *class_type TSRMLS_DC) /* {{{ */
{
    zend_object_value retval;
    kafka_conf_object *intern;

    intern = alloc_object(intern, class_type);
    zend_object_std_init(&intern->std, class_type TSRMLS_CC);
    object_properties_init(&intern->std, class_type);

    STORE_OBJECT(retval, intern, (zend_objects_store_dtor_t) zend_objects_destroy_object, kafka_conf_free, NULL);
    SET_OBJECT_HANDLERS(retval, &handlers);

    return retval;
}
/* }}} */

kafka_conf_object * get_kafka_conf_object(zval *zconf TSRMLS_DC)
{
    kafka_conf_object *oconf = get_custom_object_zval(kafka_conf_object, zconf);

    if (!oconf->type) {
        zend_throw_exception_ex(NULL, 0 TSRMLS_CC, "RdKafka\\Conf::__construct() has not been called" TSRMLS_CC);
        return NULL;
    }

    return oconf;
}

static void kafka_conf_error_cb(rd_kafka_t *rk, int err, const char *reason, void *opaque)
{
    kafka_conf_callbacks *cbs = (kafka_conf_callbacks*) opaque;
    zeval args[3];
    TSRMLS_FETCH();

    if (!opaque) {
        return;
    }

    if (!cbs->error) {
        return;
    }

    MAKE_STD_ZEVAL(args[0]);
    MAKE_STD_ZEVAL(args[1]);
    MAKE_STD_ZEVAL(args[2]);

    KAFKA_ZVAL_ZVAL(P_ZEVAL(args[0]), &cbs->rk, 1, 0);
    ZVAL_LONG(P_ZEVAL(args[1]), err);
    RDKAFKA_ZVAL_STRING(P_ZEVAL(args[2]), reason);

    rdkafka_call_function(&cbs->error->fci, &cbs->error->fcc, NULL, 3, args TSRMLS_CC);

    zval_ptr_dtor(&args[0]);
    zval_ptr_dtor(&args[1]);
    zval_ptr_dtor(&args[2]);
}

static void kafka_conf_dr_msg_cb(rd_kafka_t *rk, const rd_kafka_message_t *msg, void *opaque)
{
    kafka_conf_callbacks *cbs = (kafka_conf_callbacks*) opaque;
    zeval args[2];
    TSRMLS_FETCH();

    if (!opaque) {
        return;
    }

    if (!cbs->dr_msg) {
        return;
    }

    MAKE_STD_ZEVAL(args[0]);
    MAKE_STD_ZEVAL(args[1]);

    KAFKA_ZVAL_ZVAL(P_ZEVAL(args[0]), &cbs->rk, 1, 0);
    kafka_message_new(P_ZEVAL(args[1]), msg TSRMLS_CC);

    rdkafka_call_function(&cbs->dr_msg->fci, &cbs->dr_msg->fcc, NULL, 2, args TSRMLS_CC);

    zval_ptr_dtor(&args[0]);
    zval_ptr_dtor(&args[1]);
}

static int kafka_conf_stats_cb(rd_kafka_t *rk, char *json, size_t json_len, void *opaque)
{
    kafka_conf_callbacks *cbs = (kafka_conf_callbacks*) opaque;
    zeval args[3];
    TSRMLS_FETCH();

    if (!opaque) {
        return 0;
    }

    if (!cbs->stats) {
        return 0;
    }

    MAKE_STD_ZEVAL(args[0]);
    MAKE_STD_ZEVAL(args[1]);
    MAKE_STD_ZEVAL(args[2]);

    KAFKA_ZVAL_ZVAL(P_ZEVAL(args[0]), &cbs->rk, 1, 0);
    RDKAFKA_ZVAL_STRING(P_ZEVAL(args[1]), json);
    ZVAL_LONG(P_ZEVAL(args[2]), json_len);

    rdkafka_call_function(&cbs->stats->fci, &cbs->stats->fcc, NULL, 3, args TSRMLS_CC);

    zval_ptr_dtor(&args[0]);
    zval_ptr_dtor(&args[1]);
    zval_ptr_dtor(&args[2]);

    return 0;
}

#ifdef HAVE_NEW_KAFKA_CONSUMER
static void kafka_conf_rebalance_cb(rd_kafka_t *rk, rd_kafka_resp_err_t err, rd_kafka_topic_partition_list_t *partitions, void *opaque)
{
    kafka_conf_callbacks *cbs = (kafka_conf_callbacks*) opaque;
    zeval args[3];
    TSRMLS_FETCH();

    if (!opaque) {
        return;
    }

    if (!cbs->rebalance) {
        return;
    }

    MAKE_STD_ZEVAL(args[0]);
    MAKE_STD_ZEVAL(args[1]);
    MAKE_STD_ZEVAL(args[2]);

    KAFKA_ZVAL_ZVAL(P_ZEVAL(args[0]), &cbs->rk, 1, 0);
    ZVAL_LONG(P_ZEVAL(args[1]), err);
    kafka_topic_partition_list_to_array(P_ZEVAL(args[2]), partitions TSRMLS_CC);

    rdkafka_call_function(&cbs->rebalance->fci, &cbs->rebalance->fcc, NULL, 3, args TSRMLS_CC);

    zval_ptr_dtor(&args[0]);
    zval_ptr_dtor(&args[1]);
    zval_ptr_dtor(&args[2]);
}
#endif /* HAVE_NEW_KAFKA_CONSUMER */

/* {{{ proto RdKafka\Conf::__construct() */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_conf___construct, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Conf, __construct)
{
    kafka_conf_object *intern;
    zend_error_handling error_handling;

    zend_replace_error_handling(EH_THROW, spl_ce_InvalidArgumentException, &error_handling TSRMLS_CC);

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "") == FAILURE) {
        zend_restore_error_handling(&error_handling TSRMLS_CC);
        return;
    }

    intern = get_custom_object_zval(kafka_conf_object, getThis());
    intern->type = KAFKA_CONF;
    intern->u.conf = rd_kafka_conf_new();

    zend_restore_error_handling(&error_handling TSRMLS_CC);
}
/* }}} */

/* {{{ proto array RfKafka\Conf::dump()
   Dump the configuration properties and values of `conf` to an array */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_conf_dump, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Conf, dump)
{
    size_t cntp;
    const char **dump;
    kafka_conf_object *intern;
    size_t i;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "") == FAILURE) {
        return;
    }

    intern = get_kafka_conf_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    switch (intern->type) {
        case KAFKA_CONF:
            dump = rd_kafka_conf_dump(intern->u.conf, &cntp);
            break;
        case KAFKA_TOPIC_CONF:
            dump = rd_kafka_topic_conf_dump(intern->u.topic_conf, &cntp);
            break;
        default:
            return;
    }

    array_init(return_value);

    for (i = 0; i < cntp; i+=2) {
        const char *key = dump[i];
        const char *value = dump[i+1];
        rdkafka_add_assoc_string(return_value, (char*)key, (char*)value);
    }

    rd_kafka_conf_dump_free(dump, cntp);
}
/* }}} */

/* {{{ proto void RdKafka\Conf::set(string $name, string $value)
   Sets a configuration property. */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_conf_set, 0, 0, 2)
    ZEND_ARG_INFO(0, name)
    ZEND_ARG_INFO(0, value)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Conf, set)
{
    char *name;
    arglen_t name_len;
    char *value;
    arglen_t value_len;
    kafka_conf_object *intern;
    rd_kafka_conf_res_t ret = 0;
    char errstr[512];

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "ss", &name, &name_len, &value, &value_len) == FAILURE) {
        return;
    }

    intern = get_kafka_conf_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    errstr[0] = '\0';

    switch (intern->type) {
        case KAFKA_CONF:
            ret = rd_kafka_conf_set(intern->u.conf, name, value, errstr, sizeof(errstr));
            break;
        case KAFKA_TOPIC_CONF:
            ret = rd_kafka_topic_conf_set(intern->u.topic_conf, name, value, errstr, sizeof(errstr));
            break;
    }

    switch (ret) {
        case RD_KAFKA_CONF_UNKNOWN:
            zend_throw_exception(ce_kafka_exception, errstr, RD_KAFKA_CONF_UNKNOWN TSRMLS_CC);
            return;
        case RD_KAFKA_CONF_INVALID:
            zend_throw_exception(ce_kafka_exception, errstr, RD_KAFKA_CONF_INVALID TSRMLS_CC);
            return;
        case RD_KAFKA_CONF_OK:
            break;
    }
}
/* }}} */

#ifdef HAVE_NEW_KAFKA_CONSUMER
/* {{{ proto RdKafka\Conf::setDefaultTopicConf(RdKafka\TopicConf $topicConf) */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_conf_set_default_topic_conf, 0, 0, 1)
    ZEND_ARG_INFO(0, topic_conf)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Conf, setDefaultTopicConf)
{
    zval *ztopic_conf;
    kafka_conf_object *intern;
    kafka_conf_object *topic_conf_intern;
    rd_kafka_topic_conf_t *topic_conf;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "O", &ztopic_conf, ce_kafka_topic_conf) == FAILURE) {
        return;
    }

    intern = get_kafka_conf_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    topic_conf_intern = get_kafka_conf_object(ztopic_conf TSRMLS_CC);
    if (!topic_conf_intern) {
        return;
    }

    topic_conf = rd_kafka_topic_conf_dup(topic_conf_intern->u.topic_conf);

    rd_kafka_conf_set_default_topic_conf(intern->u.conf, topic_conf);
}
/* }}} */
#endif /* HAVE_NEW_KAFKA_CONSUMER */

/* {{{ proto void RdKafka\Conf::setErrorCb(callable $callback)
   Sets the error callback */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_conf_set_error_cb, 0, 0, 1)
    ZEND_ARG_INFO(0, callback)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Conf, setErrorCb)
{
    zend_fcall_info fci;
    zend_fcall_info_cache fcc;
    kafka_conf_object *intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "f", &fci, &fcc) == FAILURE) {
        return;
    }

    intern = get_kafka_conf_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    Z_ADDREF_P(P_ZEVAL(fci.function_name));

    if (intern->cbs.error) {
        zval_ptr_dtor(&intern->cbs.error->fci.function_name);
    } else {
        intern->cbs.error = ecalloc(1, sizeof(*intern->cbs.error));
    }

    intern->cbs.error->fci = fci;
    intern->cbs.error->fcc = fcc;

    rd_kafka_conf_set_error_cb(intern->u.conf, kafka_conf_error_cb);
}
/* }}} */

/* {{{ proto void RdKafka\Conf::setDrMsgCb(callable $callback)
   Sets the delivery report callback */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_conf_set_dr_msg_cb, 0, 0, 1)
    ZEND_ARG_INFO(0, callback)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Conf, setDrMsgCb)
{
    zend_fcall_info fci;
    zend_fcall_info_cache fcc;
    kafka_conf_object *intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "f", &fci, &fcc) == FAILURE) {
        return;
    }

    intern = get_kafka_conf_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    Z_ADDREF_P(P_ZEVAL(fci.function_name));

    if (intern->cbs.dr_msg) {
        zval_ptr_dtor(&intern->cbs.dr_msg->fci.function_name);
    } else {
        intern->cbs.dr_msg = ecalloc(1, sizeof(*intern->cbs.dr_msg));
    }

    intern->cbs.dr_msg->fci = fci;
    intern->cbs.dr_msg->fcc = fcc;

    rd_kafka_conf_set_dr_msg_cb(intern->u.conf, kafka_conf_dr_msg_cb);
}
/* }}} */

/* {{{ proto void RdKafka\Conf::setStatsCb(callable $callback)
   Sets the delivery report callback */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_conf_set_stats_cb, 0, 0, 1)
    ZEND_ARG_INFO(0, callback)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Conf, setStatsCb)
{
    zend_fcall_info fci;
    zend_fcall_info_cache fcc;
    kafka_conf_object *intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "f", &fci, &fcc) == FAILURE) {
        return;
    }

    intern = get_kafka_conf_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    Z_ADDREF_P(P_ZEVAL(fci.function_name));

    if (intern->cbs.stats) {
        zval_ptr_dtor(&intern->cbs.stats->fci.function_name);
    } else {
        intern->cbs.stats = ecalloc(1, sizeof(*intern->cbs.stats));
    }

    intern->cbs.stats->fci = fci;
    intern->cbs.stats->fcc = fcc;

    rd_kafka_conf_set_stats_cb(intern->u.conf, kafka_conf_stats_cb);
}
/* }}} */

#ifdef HAVE_NEW_KAFKA_CONSUMER
/* {{{ proto void RdKafka\Conf::setRebalanceCb(mixed $callback)
   Set rebalance callback for use with coordinated consumer group balancing */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_conf_set_rebalance_cb, 0, 0, 1)
    ZEND_ARG_INFO(0, callback)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Conf, setRebalanceCb)
{
    zend_fcall_info fci;
    zend_fcall_info_cache fcc;
    kafka_conf_object *intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "f", &fci, &fcc) == FAILURE) {
        return;
    }

    intern = get_kafka_conf_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    Z_ADDREF_P(P_ZEVAL(fci.function_name));

    if (intern->cbs.rebalance) {
        zval_ptr_dtor(&intern->cbs.rebalance->fci.function_name);
    } else {
        intern->cbs.rebalance = ecalloc(1, sizeof(*intern->cbs.rebalance));
    }

    intern->cbs.rebalance->fci = fci;
    intern->cbs.rebalance->fcc = fcc;

    rd_kafka_conf_set_rebalance_cb(intern->u.conf, kafka_conf_rebalance_cb);
}
/* }}} */
#endif /* HAVE_NEW_KAFKA_CONSUMER */

/* {{{ proto RdKafka\TopicConf::__construct() */
PHP_METHOD(RdKafka__TopicConf, __construct)
{
    kafka_conf_object *intern;
    zend_error_handling error_handling;

    zend_replace_error_handling(EH_THROW, spl_ce_InvalidArgumentException, &error_handling TSRMLS_CC);

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "") == FAILURE) {
        zend_restore_error_handling(&error_handling TSRMLS_CC);
        return;
    }

    intern = get_custom_object_zval(kafka_conf_object, getThis());
    intern->type = KAFKA_TOPIC_CONF;
    intern->u.topic_conf = rd_kafka_topic_conf_new();

    zend_restore_error_handling(&error_handling TSRMLS_CC);
}
/* }}} */

/* {{{ proto RdKafka\TopicConf::setPartitioner(int $partitioner) */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_topic_conf_set_partitioner, 0, 0, 1)
    ZEND_ARG_INFO(0, partitioner)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__TopicConf, setPartitioner)
{
    kafka_conf_object *intern;
    long id;
    int32_t (*partitioner) (const rd_kafka_topic_t * rkt, const void * keydata, size_t keylen, int32_t partition_cnt, void * rkt_opaque, void * msg_opaque);

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "l", &id) == FAILURE) {
        return;
    }

    intern = get_kafka_conf_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    switch (id) {
        case MSG_PARTITIONER_RANDOM:
            partitioner = rd_kafka_msg_partitioner_random;
            break;
#ifdef HAVE_RD_KAFKA_MSG_PARTIIONER_CONSISTENT
        case MSG_PARTITIONER_CONSISTENT:
            partitioner = rd_kafka_msg_partitioner_consistent;
            break;
#endif
        default:
            zend_throw_exception_ex(NULL, 0 TSRMLS_CC, "Invalid partitioner" TSRMLS_CC);
            return;
    }

    rd_kafka_topic_conf_set_partitioner_cb(intern->u.topic_conf, partitioner);
}
/* }}} */

static const zend_function_entry kafka_topic_conf_fe[] = {
    PHP_ME(RdKafka__TopicConf, __construct, arginfo_kafka_conf___construct, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Conf, dump, arginfo_kafka_conf_dump, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Conf, set, arginfo_kafka_conf_dump, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__TopicConf, setPartitioner, arginfo_kafka_topic_conf_set_partitioner, ZEND_ACC_PUBLIC)
    PHP_FE_END
};

static const zend_function_entry kafka_conf_fe[] = {
    PHP_ME(RdKafka__Conf, __construct, arginfo_kafka_conf___construct, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Conf, dump, arginfo_kafka_conf_dump, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Conf, set, arginfo_kafka_conf_set, ZEND_ACC_PUBLIC)
#ifdef HAVE_NEW_KAFKA_CONSUMER
    PHP_ME(RdKafka__Conf, setDefaultTopicConf, arginfo_kafka_conf_set_default_topic_conf, ZEND_ACC_PUBLIC)
#endif /* HAVE_NEW_KAFKA_CONSUMER */
    PHP_ME(RdKafka__Conf, setErrorCb, arginfo_kafka_conf_set_error_cb, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Conf, setDrMsgCb, arginfo_kafka_conf_set_dr_msg_cb, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Conf, setStatsCb, arginfo_kafka_conf_set_stats_cb, ZEND_ACC_PUBLIC)
#ifdef HAVE_NEW_KAFKA_CONSUMER
    PHP_ME(RdKafka__Conf, setRebalanceCb, arginfo_kafka_conf_set_rebalance_cb, ZEND_ACC_PUBLIC)
#endif /* HAVE_NEW_KAFKA_CONSUMER */
    PHP_FE_END
};

void kafka_conf_minit(TSRMLS_D)
{
    zend_class_entry tmpce;

    handlers = kafka_default_object_handlers;
    set_object_handler_free_obj(&handlers, kafka_conf_free);
    set_object_handler_offset(&handlers, XtOffsetOf(kafka_conf_object, std));

    INIT_NS_CLASS_ENTRY(tmpce, "RdKafka", "Conf", kafka_conf_fe);
    ce_kafka_conf = zend_register_internal_class(&tmpce TSRMLS_CC);
    ce_kafka_conf->create_object = kafka_conf_new;

    INIT_NS_CLASS_ENTRY(tmpce, "RdKafka", "TopicConf", kafka_topic_conf_fe);
    ce_kafka_topic_conf = zend_register_internal_class(&tmpce TSRMLS_CC);
    ce_kafka_topic_conf->create_object = kafka_conf_new;
}
