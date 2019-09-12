/*
* Copyright 2019 Guido Schmutz <guido.schmutz@trivadis.com>
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

SET DEFINE OFF
SET SCAN OFF
SET ECHO OFF
SET SERVEROUTPUT ON SIZE 1000000
SPOOL install.log

PROMPT ======================================================================
PROMPT This script installs Oracle database objects for emptracker.
PROMPT
PROMPT Connect to the target user (schema) of your choice.
PROMPT See user/emptracker.sql for required privileges.
PROMPT ======================================================================
PROMPT

PROMPT ======================================================================
PROMPT create order and customer queue tables
PROMPT ======================================================================
PROMPT
@./queue_table/order_qt.sql
@./queue_table/customer_qt.sql

PROMPT ======================================================================
PROMPT create order and customer queues and enable enqueue/dequeue ops
PROMPT ======================================================================
PROMPT
@./queue/order_aq.sql
@./queue/customer_aq.sql


PROMPT ======================================================================
PROMPT create object types
PROMPT ======================================================================
PROMPT
@./object/order_objt.sql

PROMPT ======================================================================
PROMPT create PL/SQL packages
PROMPT ======================================================================
PROMPT
@./package/order_pck.sql

PROMPT ======================================================================
PROMPT create monitoring views
PROMPT ======================================================================
PROMPT
@./view/monitor_order_v.sql
@./view/monitor_customer_v.sql

PROMPT ======================================================================
PROMPT create tables
PROMPT ======================================================================
PROMPT
@./table/order_t.sql
@./table/order_item_t.sql

@./table/customer_t.sql
@./table/address_t.sql

PROMPT ======================================================================
PROMPT create trigger to enqueue sal changes
PROMPT ======================================================================
PROMPT
@./trigger/order_biu_trg.sql
@./trigger/order_item_biu_trg.sql

@./trigger/customer_biu_trg.sql
@./trigger/address_biu_trg.sql

PROMPT ======================================================================
PROMPT create ORDS structures
PROMPT ======================================================================
PROMPT
@./ords/ords-order-orderdetail.sql
@./ords/ords-customer.sql


PROMPT ======================================================================
PROMPT insert test data to order_t and order_item_t
PROMPT ======================================================================
PROMPT
@./testdata/order_t.sql
@./testdata/order_item_t.sql


SPOOL OFF
