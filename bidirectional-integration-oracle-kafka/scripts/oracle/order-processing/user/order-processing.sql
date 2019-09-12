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

SET DEFINE ON
SET ECHO ON
SPOOL order_processing.log
DEFINE username = ORDER_PROCESSING

PROMPT ====================================================================
PROMPT This script creates the user &&username with all required privileges.
PROMPT Run this script as SYS.
PROMPT Please change default tablespace and password.
PROMPT ====================================================================

PROMPT ====================================================================
PROMPT User
PROMPT ====================================================================

CREATE USER &&username IDENTIFIED BY order_processing
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP;

PROMPT ====================================================================
PROMPT Grants
PROMPT ====================================================================

-- to access tablespace
ALTER USER &&username QUOTA UNLIMITED ON users;

-- to connect
GRANT connect, resource TO &&username;
ALTER USER &&username DEFAULT ROLE connect, resource;

-- to create views 
GRANT CREATE VIEW TO &&username;

-- to get access to DBA-views
GRANT SELECT_CATALOG_ROLE TO &&username;

-- to create views using DBA-views
GRANT SELECT ANY DICTIONARY TO &&username;

-- to use AQ
GRANT EXECUTE ON dbms_aqadm TO &&username;
GRANT EXECUTE ON dbms_aq TO &&username;
GRANT EXECUTE ON dbms_aqin to &&username;

-- to use ORDS
grant select on ORDS_METADATA.ORDS_PARAMETERS to  &&username;
grant select on ORDS_METADATA.ORDS_SCHEMAS to  &&username;

-- to debug in SQL Developer
GRANT DEBUG CONNECT SESSION, DEBUG ANY PROCEDURE TO &&username;
GRANT EXECUTE ON dbms_debug_jdwp to &&username;
BEGIN
  dbms_network_acl_admin.append_host_ace (
     host =>'*',
     ace  => sys.xs$ace_type(
                privilege_list => sys.xs$name_list('JDWP') ,
                principal_name => '&&username',
                principal_type => sys.xs_acl.ptype_db
             )
  );
END;
/
