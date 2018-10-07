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
#include "metadata_collection.h"

typedef struct _object_intern {
#if PHP_MAJOR_VERSION < 7
    zend_object                     std;
#endif
    zval                            zmetadata;
    const rd_kafka_metadata_partition_t *metadata_partition;
#if PHP_MAJOR_VERSION >= 7
    zend_object                     std;
#endif
} object_intern;

static HashTable *get_debug_info(zval *object, int *is_temp TSRMLS_DC);

static zend_class_entry * ce;
static zend_object_handlers handlers;

static void free_object(zend_object *object TSRMLS_DC) /* {{{ */
{
    object_intern *intern = get_custom_object(object_intern, object);

    if (intern->metadata_partition) {
        zval_dtor(&intern->zmetadata);
    }

    zend_object_std_dtor(&intern->std TSRMLS_CC);

    free_custom_object(intern);
}
/* }}} */

static zend_object_value create_object(zend_class_entry *class_type TSRMLS_DC) /* {{{ */
{
    zend_object_value retval;
    object_intern *intern;

    intern = alloc_object(intern, class_type);
    zend_object_std_init(&intern->std, class_type TSRMLS_CC);
    object_properties_init(&intern->std, class_type);

    STORE_OBJECT(retval, intern, (zend_objects_store_dtor_t) zend_objects_destroy_object, free_object, NULL);
    SET_OBJECT_HANDLERS(retval, &handlers);

    return retval;
}
/* }}} */

static object_intern * get_object(zval *zmt TSRMLS_DC)
{
    object_intern *omt = get_custom_object_zval(object_intern, zmt);

    if (!omt->metadata_partition) {
        zend_throw_exception_ex(NULL, 0 TSRMLS_CC, "RdKafka\\Metadata\\Partition::__construct() has not been called");
        return NULL;
    }

    return omt;
}

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

    add_assoc_long(&ary, "id", intern->metadata_partition->id);
    add_assoc_long(&ary, "err", intern->metadata_partition->err);
    add_assoc_long(&ary, "leader", intern->metadata_partition->leader);
    add_assoc_long(&ary, "replica_cnt", intern->metadata_partition->replica_cnt);
    add_assoc_long(&ary, "isr_cnt", intern->metadata_partition->isr_cnt);

    return Z_ARRVAL(ary);
}
/* }}} */

/* {{{ proto int RdKafka\Metadata\Partition::getId()
   Partition id */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_metadata_get_id, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Metadata__Partition, getId)
{
    object_intern *intern;

    if (zend_parse_parameters_none() == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    RETURN_LONG(intern->metadata_partition->id);
}
/* }}} */

/* {{{ proto int RdKafka\Metadata\Partition::getErr()
   Partition error reported by broker */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_metadata_get_err, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Metadata__Partition, getErr)
{
    object_intern *intern;

    if (zend_parse_parameters_none() == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    RETURN_LONG(intern->metadata_partition->err);
}
/* }}} */

/* {{{ proto int RdKafka\Metadata\Partition::getLeader()
   Leader broker */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_metadata_get_leader, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Metadata__Partition, getLeader)
{
    object_intern *intern;

    if (zend_parse_parameters_none() == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    RETURN_LONG(intern->metadata_partition->leader);
}
/* }}} */

void int32_ctor(zval *return_value, zval *zmetadata, const void *data TSRMLS_DC) {
    ZVAL_LONG(return_value, *(int32_t*)data);
}

/* {{{ proto array RdKafka\Metadata\Partition::getReplicas()
   Replica broker ids */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_metadata_get_replicas, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Metadata__Partition, getReplicas)
{
    object_intern *intern;

    if (zend_parse_parameters_none() == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    kafka_metadata_collection_init(return_value, getThis(), intern->metadata_partition->replicas, intern->metadata_partition->replica_cnt, sizeof(*intern->metadata_partition->replicas), int32_ctor TSRMLS_CC);
}
/* }}} */

/* {{{ proto array RdKafka\Metadata\Partition::getIsrs()
   In-Sync-Replica broker ids */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_metadata_get_isrs, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Metadata__Partition, getIsrs)
{
    object_intern *intern;

    if (zend_parse_parameters_none() == FAILURE) {
        return;
    }

    intern = get_object(getThis() TSRMLS_CC);
    if (!intern) {
        return;
    }

    kafka_metadata_collection_init(return_value, getThis(), intern->metadata_partition->isrs, intern->metadata_partition->isr_cnt, sizeof(*intern->metadata_partition->isrs), int32_ctor TSRMLS_CC);
}
/* }}} */

static const zend_function_entry fe[] = {
    PHP_ME(RdKafka__Metadata__Partition, getId, arginfo_kafka_metadata_get_id, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Metadata__Partition, getErr, arginfo_kafka_metadata_get_err, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Metadata__Partition, getLeader, arginfo_kafka_metadata_get_leader, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Metadata__Partition, getReplicas, arginfo_kafka_metadata_get_replicas, ZEND_ACC_PUBLIC)
    PHP_ME(RdKafka__Metadata__Partition, getIsrs, arginfo_kafka_metadata_get_isrs, ZEND_ACC_PUBLIC)
    PHP_FE_END
};

void kafka_metadata_partition_minit(TSRMLS_D)
{
    zend_class_entry tmpce;

    INIT_NS_CLASS_ENTRY(tmpce, "RdKafka", "Metadata\\Partition", fe);
    ce = zend_register_internal_class(&tmpce TSRMLS_CC);
    ce->create_object = create_object;

    handlers = kafka_default_object_handlers;
    handlers.get_debug_info = get_debug_info;
    set_object_handler_free_obj(&handlers, free_object);
    set_object_handler_offset(&handlers, XtOffsetOf(object_intern, std));
}

void kafka_metadata_partition_ctor(zval *return_value, zval *zmetadata, const void *data TSRMLS_DC)
{
    rd_kafka_metadata_partition_t *metadata_partition = (rd_kafka_metadata_partition_t*)data;
    object_intern *intern;

    if (object_init_ex(return_value, ce) != SUCCESS) {
        return;
    }

    intern = get_custom_object_zval(object_intern, return_value);
    if (!intern) {
        return;
    }

    ZVAL_ZVAL(&intern->zmetadata, zmetadata, 1, 0);
    intern->metadata_partition = metadata_partition;
}
