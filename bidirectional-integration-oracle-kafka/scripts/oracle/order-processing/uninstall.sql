/*
* Copyright 2016 Philipp Salvisberg <philipp.salvisberg@trivadis.com>
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
WHENEVER SQLERROR CONTINUE
SPOOL uninstall.log

PROMPT ======================================================================
PROMPT This script uninstalls Oracle objects created by install.sql
PROMPT Errors are thrown if an expected object does not exist.
PROMPT ======================================================================

PROMPT ======================================================================
PROMPT drop demo tables
PROMPT ======================================================================
DROP TABLE emp PURGE;
DROP TABLE dept PURGE;

PROMPT ======================================================================
PROMPT drop monitoring views
PROMPT ======================================================================
PROMPT
DROP VIEW monitor_requests_v;
DROP VIEW monitor_responses_v;
DROP VIEW monitor_req_res_v;

PROMPT ======================================================================
PROMPT stop queues
PROMPT ======================================================================
PROMPT
BEGIN
   dbms_aqadm.stop_queue(queue_name => 'REQUESTS_AQ');
END;
/

BEGIN
   dbms_aqadm.stop_queue(queue_name => 'RESPONSES_AQ');
END;
/

PROMPT ======================================================================
PROMPT unregister PL/SQL callback procedures
PROMPT ======================================================================
PROMPT
BEGIN
   FOR r IN (
      SELECT subscription_name, location_name
        FROM user_subscr_registrations
   ) LOOP
       dbms_aq.unregister(
          reg_list => sys.aq$_reg_info_list(
                         sys.aq$_reg_info(
                            name      => r.subscription_name,
                            namespace => DBMS_AQ.NAMESPACE_AQ,
                            callback  => r.location_name,
                            context   => NULL
                         )
                      ),
          reg_count => 1
       );
   END LOOP;
END;
/

PROMPT ======================================================================
PROMPT drop callback procedure
PROMPT ======================================================================
PROMPT
DROP PROCEDURE raw_enq_callback;

PROMPT ======================================================================
PROMPT drop tweet function
PROMPT ======================================================================
PROMPT
DROP FUNCTION tweet;

PROMPT ======================================================================
PROMPT drop queues
PROMPT ======================================================================
PROMPT
BEGIN
   dbms_aqadm.drop_queue (queue_name => 'REQUESTS_AQ');
END;
/

BEGIN
   dbms_aqadm.drop_queue (queue_name => 'RESPONSES_AQ');
END;
/

PROMPT ======================================================================
PROMPT drop queue tables
PROMPT ======================================================================
PROMPT
BEGIN
   dbms_aqadm.drop_queue_table (queue_table => 'REQUESTS_QT');
END;
/

BEGIN
   dbms_aqadm.drop_queue_table (queue_table => 'RESPONSES_QT');
END;
/

SPOOL OFF
