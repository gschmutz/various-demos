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

#ifndef PHP_RDKAFKA_PRIV_H
#define PHP_RDKAFKA_PRIV_H

#if PHP_MAJOR_VERSION >= 7

typedef zend_object* zend_object_value;

typedef size_t arglen_t;

#define STORE_OBJECT(retval, intern, dtor, free, clone) do { \
    retval = &intern->std; \
} while (0)

#define SET_OBJECT_HANDLERS(retval, _handlers) do { \
    retval->handlers = _handlers; \
} while (0)

#define alloc_object(intern, ce) ecalloc(1, sizeof(*intern) + zend_object_properties_size(ce))

static inline zval * is_zval(zval * zv) {
    return zv;
}

#define get_custom_object_zval(type, zobject) \
    ((type*)((char *)Z_OBJ_P(is_zval(zobject)) - XtOffsetOf(type, std)))

static inline zend_object * is_zend_object(zend_object * object) {
    return object;
}

#define get_custom_object(type, object) \
    ((type*)((char *)is_zend_object(object) - XtOffsetOf(type, std)))

#define free_custom_object(object) /* no-op */

static inline zend_class_entry *rdkafka_register_internal_class_ex(zend_class_entry *class_entry, zend_class_entry *parent_ce TSRMLS_DC)
{
    return zend_register_internal_class_ex(class_entry, parent_ce);
}

static inline void set_object_handler_free_obj(zend_object_handlers * handlers, zend_object_free_obj_t free_obj)
{
    handlers->free_obj = free_obj;
}
static inline void set_object_handler_offset(zend_object_handlers * handlers, size_t offset)
{
    handlers->offset = offset;
}

static inline void rdkafka_call_function(zend_fcall_info *fci, zend_fcall_info_cache *fci_cache, zval *retval, uint32_t param_count, zval params[] TSRMLS_DC)
{
    int local_retval;
    zval local_retval_zv;

    if (retval) {
        local_retval = 0;
    } else {
        local_retval = 1;
        retval = &local_retval_zv;
    }

    fci->retval = retval;
    fci->params = params;
    fci->param_count = param_count;

    zend_call_function(fci, fci_cache TSRMLS_CC);

    if (local_retval) {
        zval_ptr_dtor(retval);
    }
}

static inline zval *rdkafka_read_property(zend_class_entry *scope, zval *object, const char *name, size_t name_length, zend_bool silent TSRMLS_DC)
{
    zval rv;
    return zend_read_property(scope, object, name, name_length, silent, &rv TSRMLS_CC);
}

static inline zval *rdkafka_hash_get_current_data_ex(HashTable *ht, HashPosition *pos)
{
    return zend_hash_get_current_data_ex(ht, pos);
}

#define rdkafka_add_assoc_string(arg, key, str) add_assoc_string(arg, key, str)

#define RDKAFKA_RETURN_STRING(str) RETURN_STRING(str)
#define RDKAFKA_ZVAL_STRING(zv, str) ZVAL_STRING(zv, str)
#else /* PHP < 7 */

typedef int arglen_t;

#define STORE_OBJECT(retval, intern, dtor, free, clone) do { \
    void (*___free_object_storage)(zend_object *object TSRMLS_DC) = free; \
    retval.handle = zend_objects_store_put(&intern->std, dtor, (zend_objects_free_object_storage_t)___free_object_storage, clone TSRMLS_CC); \
} while (0)

#define SET_OBJECT_HANDLERS(retval, _handlers) do { \
    retval.handlers = _handlers; \
} while (0)

#define alloc_object(intern, ce) ecalloc(1, sizeof(*intern))

#define get_custom_object_zval(type, zobject) \
    ((type*)zend_object_store_get_object(zobject TSRMLS_CC))

#define get_custom_object(type, object) \
    ((type*)object)

#define free_custom_object(object) efree(object)

static inline void *zend_hash_str_add_ptr(HashTable *ht, const char *str, size_t len, void *pData)
{
    void *pDest;
    zend_hash_add(ht, str, len, &pData, sizeof(pData), &pDest);
    return pDest;
}

static inline int zend_hash_str_del(HashTable *ht, const char *str, size_t len)
{
	return zend_hash_del(ht, str, len);
}

static inline zend_bool zend_hash_str_exists(const HashTable *ht, const char *str, size_t len)
{
    return zend_hash_exists(ht, str, len);
}

static inline void *zend_hash_index_add_ptr(HashTable *ht, zend_ulong h, void *pData)
{
    void *pDest;
    zend_hash_index_update(ht, h, &pData, sizeof(pData), &pDest);
    return pDest;
}

static inline zend_class_entry *rdkafka_register_internal_class_ex(zend_class_entry *class_entry, zend_class_entry *parent_ce TSRMLS_DC)
{
    return zend_register_internal_class_ex(class_entry, parent_ce, NULL TSRMLS_CC);
}

typedef void (*zend_object_free_obj_t)(zend_object *object);
static inline void set_object_handler_free_obj(zend_object_handlers * handlers, zend_object_free_obj_t free_obj)
{
    /* no-op */
}

static inline void set_object_handler_offset(zend_object_handlers * handlers, size_t offset)
{
    /* no-op */
}

static inline void rdkafka_call_function(zend_fcall_info *fci, zend_fcall_info_cache *fci_cache, zval **retval, uint32_t param_count, zval *params[] TSRMLS_DC)
{
    uint32_t i;
    int local_retval;
    zval *local_retval_zv;
    zval ***params_array;

    if (retval) {
        local_retval = 0;
    } else {
        local_retval = 1;
        retval = &local_retval_zv;
    }

    params_array = (zval ***) emalloc(sizeof(zval **)*param_count);
	for (i = 0; i < param_count; i++) {
        params_array[i] = &params[i];
    }

    fci->retval_ptr_ptr = retval;
    fci->params = params_array;
    fci->param_count = param_count;

    zend_call_function(fci, fci_cache TSRMLS_CC);

    if (local_retval && *retval) {
        zval_ptr_dtor(retval);
    }

    efree(params_array);
}

static inline zval *rdkafka_read_property(zend_class_entry *scope, zval *object, const char *name, size_t name_length, zend_bool silent TSRMLS_DC)
{
    return zend_read_property(scope, object, name, name_length, silent TSRMLS_CC);
}

static inline zval **rdkafka_hash_get_current_data_ex(HashTable *ht, HashPosition *pos)
{
    zval **zv;

    if (zend_hash_get_current_data_ex(ht, (void**)&zv, pos) == SUCCESS) {
        return zv;
    }

    return NULL;
}

#define rdkafka_add_assoc_string(arg, key, str) add_assoc_string(arg, key, str, 1)

#define RDKAFKA_RETURN_STRING(str) RETURN_STRING(str, 1)
#define RDKAFKA_ZVAL_STRING(zv, str) ZVAL_STRING(zv, str, 1)
#endif

kafka_object * get_kafka_object(zval *zrk TSRMLS_DC);
void add_consuming_toppar(kafka_object * intern, rd_kafka_topic_t * rkt, int32_t partition);
void del_consuming_toppar(kafka_object * intern, rd_kafka_topic_t * rkt, int32_t partition);
int is_consuming_toppar(kafka_object * intern, rd_kafka_topic_t * rkt, int32_t partition);

#endif /* PHP_RDKAFKA_PRIV_H */
