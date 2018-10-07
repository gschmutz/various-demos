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

#ifdef HAVE_NEW_KAFKA_CONSUMER

typedef struct _kafka_topic_partition_intern {
#if PHP_MAJOR_VERSION < 7
    zend_object std;
#endif
    char        *topic;
    int32_t     partition;
    int64_t     offset;
#if PHP_MAJOR_VERSION >= 7
    zend_object std;
#endif
} kafka_topic_partition_intern;

void kafka_metadata_topic_partition_minit(TSRMLS_D);

kafka_topic_partition_intern * get_topic_partition_object(zval *z TSRMLS_DC);
void kafka_topic_partition_init(zval *z, char *topic, int32_t partition, int64_t offset TSRMLS_DC);

void kafka_topic_partition_list_to_array(zval *return_value, rd_kafka_topic_partition_list_t *list TSRMLS_DC);
rd_kafka_topic_partition_list_t * array_arg_to_kafka_topic_partition_list(int argnum, HashTable *ary TSRMLS_DC);

extern zend_class_entry * ce_kafka_topic_partition;

#else /* HAVE_NEW_KAFKA_CONSUMER */

static inline void kafka_metadata_topic_partition_minit(TSRMLS_D) { }

#endif /* HAVE_NEW_KAFKA_CONSUMER */
