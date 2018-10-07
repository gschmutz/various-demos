#ifndef incl_ZEVAL_H
#define incl_ZEVAL_H

#ifdef HAVE_CONFIG_H
# include "config.h"
#endif

#include "php.h"

/***************************************************************************/
#ifdef ZEND_ENGINE_3

typedef zval zeval;

#define ZEVAL(v)      (v)
#define P_ZEVAL(v)  (&(v))

#define MAKE_STD_ZEVAL(v) ZVAL_NULL(&v)
#define ZEVAL_UNINIT(v) ZVAL_NULL(v)

#define ZE_ISDEF(v) (Z_TYPE(v) != IS_UNDEF)
#define ZE_TYPE(v)   Z_TYPE(v)
#define ZE_BVAL(v)   Z_BVAL(v)
#define ZE_LVAL(V)   Z_LVAL(v)
#define ZE_DVAL(v)   Z_DVAL(v)
#define ZE_STRVAL(v) Z_STRVAL(v)
#define ZE_STRLEN(v) Z_STRLEN(v)
#define ZE_ARRVAL(v) Z_ARRVAL(v)
#define ZE_OBJCE(v)  Z_OBJCE(v)

#define ZEVAL_DUP_C
#define ZEVAL_DUP_CC

/***************************************************************************/
#elif defined(ZEND_ENGINE_2)

typedef zval* zeval;

#define ZEVAL(v)    (*(v))
#define P_ZEVAL(v)    (v)

#define MAKE_STD_ZEVAL(v) MAKE_STD_ZVAL(v)
#define ZEVAL_UNINIT(v) (v = NULL)

#define ZE_ISDEF(v)  (v)
#define IS_TRUE      14
#define IS_FALSE     15
#define ZE_TYPE(v)   ((Z_TYPE_P(v) == IS_BOOL) \
                      ? (Z_BVAL_P(v) ? IS_TRUE : IS_FALSE) \
                      : Z_TYPE_P(v))
#define ZE_BVAL(v)   Z_BVAL_P(v)
#define ZE_LVAL(V)   Z_LVAL_P(v)
#define ZE_DVAL(v)   Z_DVAL_P(v)
#define ZE_STRVAL(v) Z_STRVAL_P(v)
#define ZE_STRLEN(v) Z_STRLEN_P(v)
#define ZE_ARRVAL(v) Z_ARRVAL_P(v)
#define ZE_OBJCE(v)  Z_OBJCE_P(v)

#define ZEVAL_DUP_C    1
#define ZEVAL_DUP_CC , 1

/***************************************************************************/
#else
# error "Unknown Zend Engine version"
#endif

#endif
