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

BEGIN
   dbms_aqadm.create_queue_table (
      queue_table          => 'ORDER_QT',
      queue_payload_type   => 'SYS.AQ$_JMS_TEXT_MESSAGE',
      sort_list            => 'PRIORITY,ENQ_TIME',
      multiple_consumers   => FALSE,
      message_grouping     => dbms_aqadm.none
   );
END;
/
