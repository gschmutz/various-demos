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

#if PHP_MAJOR_VERSION == 5 && PHP_MINOR_VERSION < 4
void rdkafka_object_properties_init_53(zend_object *object, zend_class_entry *class_type);
#define object_properties_init rdkafka_object_properties_init_53
#endif

#if PHP_MAJOR_VERSION == 5 && PHP_MINOR_VERSION < 6
#   define KAFKA_ZVAL_ZVAL(z, zv, copy, dtor) do {  \
        zval * ___z = (z);                          \
        zval * ___zv = (zv);                        \
        ZVAL_ZVAL(___z, ___zv, copy, dtor);         \
    } while (0)
#else
#   define KAFKA_ZVAL_ZVAL ZVAL_ZVAL
#endif

