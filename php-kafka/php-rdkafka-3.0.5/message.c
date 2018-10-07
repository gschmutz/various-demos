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
#include "topic.h"
#include "message.h"

zend_class_entry * ce_kafka_message;

void kafka_message_new(zval *return_value, const rd_kafka_message_t *message TSRMLS_DC)
{
    object_init_ex(return_value, ce_kafka_message);

    zend_update_property_long(NULL, return_value, ZEND_STRL("err"), message->err TSRMLS_CC);
    if (message->rkt) {
        zend_update_property_string(NULL, return_value, ZEND_STRL("topic_name"), rd_kafka_topic_name(message->rkt) TSRMLS_CC);
    }
    zend_update_property_long(NULL, return_value, ZEND_STRL("partition"), message->partition TSRMLS_CC);
    if (message->payload) {
        zend_update_property_stringl(NULL, return_value, ZEND_STRL("payload"), message->payload, message->len TSRMLS_CC);
        zend_update_property_long(NULL, return_value, ZEND_STRL("len"), message->len TSRMLS_CC);
    }
    if (message->key) {
        zend_update_property_stringl(NULL, return_value, ZEND_STRL("key"), message->key, message->key_len TSRMLS_CC);
    }
    zend_update_property_long(NULL, return_value, ZEND_STRL("offset"), message->offset TSRMLS_CC);
}

/* {{{ proto string RdKafka\Message::errstr()
 *  Returns the error string for an errored KrKafka\Message or NULL if there was no error.
 */

ZEND_BEGIN_ARG_INFO_EX(arginfo_kafka_message_errstr, 0, 0, 0)
ZEND_END_ARG_INFO()

PHP_METHOD(RdKafka__Message, errstr)
{
    zval *zerr;
    zval *zpayload;
    const char *errstr;

    if (zend_parse_parameters(ZEND_NUM_ARGS() TSRMLS_CC, "") == FAILURE) {
        return;
    }

    zerr = rdkafka_read_property(NULL, getThis(), ZEND_STRL("err"), 0 TSRMLS_CC);

    if (!zerr || Z_TYPE_P(zerr) != IS_LONG) {
        return;
    }

    zpayload = rdkafka_read_property(NULL, getThis(), ZEND_STRL("payload"), 0 TSRMLS_CC);

    if (zpayload && Z_TYPE_P(zpayload) == IS_STRING) {
        RETURN_ZVAL(zpayload, 1, 0);
    }

    errstr = rd_kafka_err2str(Z_LVAL_P(zerr));

    if (errstr) {
        RDKAFKA_RETURN_STRING(errstr);
    }
}
/* }}} */

static const zend_function_entry kafka_message_fe[] = {
    PHP_ME(RdKafka__Message, errstr, arginfo_kafka_message_errstr, ZEND_ACC_PUBLIC)
    PHP_FE_END
};

void kafka_message_minit(TSRMLS_D) { /* {{{ */
    zend_class_entry ce;

    INIT_NS_CLASS_ENTRY(ce, "RdKafka", "Message", kafka_message_fe);
    ce_kafka_message = zend_register_internal_class(&ce TSRMLS_CC);

    zend_declare_property_null(ce_kafka_message, ZEND_STRL("err"), ZEND_ACC_PUBLIC TSRMLS_CC);
    zend_declare_property_null(ce_kafka_message, ZEND_STRL("topic_name"), ZEND_ACC_PUBLIC TSRMLS_CC);
    zend_declare_property_null(ce_kafka_message, ZEND_STRL("partition"), ZEND_ACC_PUBLIC TSRMLS_CC);
    zend_declare_property_null(ce_kafka_message, ZEND_STRL("payload"), ZEND_ACC_PUBLIC TSRMLS_CC);
    zend_declare_property_null(ce_kafka_message, ZEND_STRL("len"), ZEND_ACC_PUBLIC TSRMLS_CC);
    zend_declare_property_null(ce_kafka_message, ZEND_STRL("key"), ZEND_ACC_PUBLIC TSRMLS_CC);
    zend_declare_property_null(ce_kafka_message, ZEND_STRL("offset"), ZEND_ACC_PUBLIC TSRMLS_CC);
} /* }}} */
