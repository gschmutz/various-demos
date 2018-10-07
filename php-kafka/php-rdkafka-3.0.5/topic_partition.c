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
#include "topic_partition.h"
#include "zeval.h"

typedef kafka_topic_partition_intern object_intern;

static HashTable *get_debug_info(zval *object, int *is_temp TSRMLS_DC);

zend_class_entry * ce_kafka_topic_partition;

static zend_object_handlers handlers;

static void free_object(zend_object *object TSRMLS_DC) /* {{{ */
{
    object_intern *intern = get_custom_object(object_intern, object);

    if (intern->topic) {
        efree(intern->topic);
    }

    zend_object_std_dtor(&intern->std TSRMLS_CC);

    free_custom_object(intern);
}
/* }}} */

static zend_object_value create_object(zend_class_entry *class_type TSRMLS_DC) /* {{{ */
{
    zend_object_value retval;
    object_intern *intern;

    intern = ecalloc(1, sizeof(*intern));
    zend_object_std_init(&intern->std, class_type TSRMLS_CC);
    object_properties_init(&intern->std, class_type);

    STORE_OBJECT(retval, intern, (zend_objects_store_dtor_t) zend_objects_destroy_object, free_object, NULL);
    SET_OBJECT_HANDLERS(retval, &handlers);

    return retval;
}
/* }}} */

static object_intern * get_object(zval *z TSRMLS_DC) /* {{{ */
{
    object_intern * intern = get_custom_object_zval(object_intern, z);

    if (!intern->topic) {
        zend_throw_exception_ex(NULL, 0 TSRMLS_CC, "RdKafka\\TopicPartition::__construct() has not been called");
        return NULL;
    }

    return intern;
} /* }}} */

kafka_topic_partition_intern * get_topic_partition_object(zval *z TSRMLS_DC) /* {{{ */
{
    return get_object(z TSRMLS_CC);
} /* }}} */

static HashTable *get_debug_info(zval *object, int *is_temp TSRMLS_DC) /* {{{ */
{
    zval ary;
    object_intern *intern;

    *is_temp = 1;

    array_init(&ary);

    intern = get_object(object TSRMLS_CC);
    if (!intern) {
        return Z_ARRVAL(ary);
    }

    if (intern->topic) {
        rdkafka_add_assoc_string(&ary, "topic", intern->topic);
    } else {
        add_assoc_null(&ary, "topic");
    }

    add_assoc_long(&ary, "partition", intern->partition);
    add_assoc_long(&ary, "offset", intern->offset);

    return Z_ARRVAL(ary);
}
/* }}} */

void kafka_topic_partition_init(zval *zobj, char * topic, int32_t partition, int64_t offset TSRMLS_DC) /* {{{ */
{
    object_intern *intern;

    intern = get_custom_object_zval(object_intern, zobj);
    if (!intern) {
        return;
    }

    if (intern->topic) {
        efree(intern->topic);
    }
    intern->topic = estrdup(topic);

    intern->partition = partition;
    intern->offset = offset;
} /* }}} */

void kafka_topic_partition_list_to_array(zval *return_value, rd_kafka_topic_partition_list_t *list TSRMLS_DC) /* {{{ */
{
    rd_kafka_topic_partition_t *topar;
    zeval ztopar;
    int i;

    array_init_size(return_value, list->cnt);

    for (i = 0; i < list->cnt; i++) {
        topar = &list->elems[i];
        MAKE_STD_ZEVAL(ztopar);
        object_init_ex(P_ZEVAL(ztopar), ce_kafka_topic_partition);
        kafka_topic_partition_init(P_ZEVAL(ztopar), topar->topic, topar->partition, topar->offset TSRMLS_CC);
        add_next_index_zval(return_value, P_ZEVAL(ztopar));
    }
} /* }}} */

rd_kafka_topic_partition_list_t * array_arg_to_kafka_topic_partition_list(int argnum, HashTable *ary TSRMLS_DC) { /* {{{ */

    HashPosition pos;
    rd_kafka_topic_partition_list_t *list;
    zeval *zv;

    list = rd_kafka_topic_partition_list_new(zend_hash_num_elements(ary));

    for (zend_hash_internal_pointer_reset_ex(ary, &pos);
            (zv = rdkafka_hash_get_current_data_ex(ary, &pos)) != NULL;
            zend_hash_move_forward_ex(ary, &pos)) {
        kafka_topic_partition_intern *topar_intern;
        rd_kafka_topic_partition_t *topar;

        if (Z_TYPE_P(ZEVAL(zv)) != IS_OBJECT || !instanceof_function(Z_OBJCE_P(ZEVAL(zv)), ce_kafka_topic_partition TSRMLS_CC)) {
            const char *space;
            const char *class_name = get_active_class_name(&space TSRMLS_CC);
            rd_kafka_topic_partition_list_destroy(list);
            php_error(E_ERROR,
                    "Argument %d passed to %s%s%s() must be an array of RdKafka\\TopicPartition, at least one element is a(n) %s",
                    argnum,
                    class_name, space,
                    get_active_function_name(TSRMLS_C),
                    zend_zval_type_name(ZEVAL(zv)));
            return NULL;
        }

        topar_intern = get_topic_partition_object(ZEVAL(zv) TSRMLS_CC);
        if (!topar_intern) {
            rd_kafka_topic_partition_list_destroy(list);
            return NULL;
        }

        topar = rd_kafka_topic_partition_list_add(list, topar_intern->topic, topar_intern->partition);
        topar->offset = topar_intern->offset;
    }

    return list;
} /* }}} */


/* {{{ proto void RdKafka\TopicPartition::__construct(string $topic, int $partition[, int $offset])
   Constructor */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_topic_partition___construct, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__TopicPartition, __construct)
{
    char *topic;
    arglen_t topic_len;
    long partition;
    long offset = 0;
    zend_error_handling error_handling;

    zend_replace_error_handling(EH_THROW, spl_ce_InvalidArgumentException, &error_handling TSRMLS_CC);

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "sl|l", &topic, &topic_len, &partition, &offset) == FAILURE) {
        zend_restore_error_handling(&error_handling TSRMLS_CC);
        return;
    }

    kafka_topic_partition_init(getThis(), topic, partition, offset TSRMLS_CC);

    zend_restore_error_handling(&error_handling TSRMLS_CC);
}
/* }}} */

/* {{{ proto string RdKafka\TopicPartition::getTopic()
   Returns topic name */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_topic_partition_get_topic, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__TopicPartition, getTopic)
{
    object_intern *intern;

    if (zend_parse_parameters_none() == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    if (intern->topic) {
        RDKAFKA_RETURN_STRING(intern->topic);
    } else {
        RETURN_NULL();
    }
}
/* }}} */

/* {{{ proto TopicPartition RdKafka\TopicPartition::setTopic($topicName)
   Sets topic name */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_topic_partition_set_topic, 0, 0, 1)
    ZEND_ARG_INFO(0, topic_name)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__TopicPartition, setTopic)
{
    char * topic;
    arglen_t topic_len;
    object_intern *intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "s", &topic, &topic_len) == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    if (intern->topic) {
        efree(intern->topic);
    }

    intern->topic = estrdup(topic);

    RETURN_ZVAL(getThis(), 1, 0);
}
/* }}} */

/* {{{ proto int RdKafka\TopicPartition::getPartition()
   Returns partition */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_topic_partition_get_partition, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__TopicPartition, getPartition)
{
    object_intern *intern;

    if (zend_parse_parameters_none() == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    RETURN_LONG(intern->partition);
}
/* }}} */

/* {{{ proto TopicPartition RdKafka\TopicPartition::setPartition($partition)
   Sets partition */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_topic_partition_set_partition, 0, 0, 1)
    ZEND_ARG_INFO(0, partition)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__TopicPartition, setPartition)
{
    long partition;
    object_intern *intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "l", &partition) == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    intern->partition = partition;

    RETURN_ZVAL(getThis(), 1, 0);
}
/* }}} */

/* {{{ proto int RdKafka\TopicPartition::getOffset()
   Returns offset */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_topic_partition_get_offset, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__TopicPartition, getOffset)
{
    object_intern *intern;

    if (zend_parse_parameters_none() == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    RETURN_LONG(intern->offset);
}
/* }}} */

/* {{{ proto TopicPartition RdKafka\TopicPartition::setOffset($offset)
   Sets offset */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_topic_partition_set_offset, 0, 0, 1)
    ZEND_ARG_INFO(0, offset)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__TopicPartition, setOffset)
{
    long offset;
    object_intern *intern;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "l", &offset) == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    intern->offset = offset;

    RETURN_ZVAL(getThis(), 1, 0);
}
/* }}} */

static const zend_function_entry fe[] = { /* {{{ */
    PHP_ME(RdKafka__TopicPartition, __construct, arginfo_kafka_topic_partition___construct, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__TopicPartition, getTopic, arginfo_kafka_topic_partition_get_topic, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__TopicPartition, setTopic, arginfo_kafka_topic_partition_set_topic, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__TopicPartition, getPartition, arginfo_kafka_topic_partition_get_partition, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__TopicPartition, setPartition, arginfo_kafka_topic_partition_set_partition, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__TopicPartition, getOffset, arginfo_kafka_topic_partition_get_offset, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__TopicPartition, setOffset, arginfo_kafka_topic_partition_set_offset, ZEND_ACC_PUBLIC)
    PHP_FE_END
}; /* }}} */

void kafka_metadata_topic_partition_minit(TSRMLS_D) /* {{{ */
{
    zend_class_entry tmpce;

    INIT_NS_CLASS_ENTRY(tmpce, "RdKafka", "TopicPartition", fe);
    ce_kafka_topic_partition = zend_register_internal_class(&tmpce TSRMLS_CC);
    ce_kafka_topic_partition->create_object = create_object;

    handlers = kafka_default_object_handlers;
    handlers.get_debug_info = get_debug_info;
    set_object_handler_free_obj(&handlers, free_object);
    set_object_handler_offset(&handlers, XtOffsetOf(object_intern, std));
} /* }}} */
