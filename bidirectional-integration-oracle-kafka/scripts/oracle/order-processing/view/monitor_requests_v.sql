/*
* Copyright 2019 Guido Schmutz <guido.schmutz	@trivadis.com>
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

CREATE OR REPLACE VIEW monitor_requests_v AS
SELECT q.msgid AS msg_id,
       q.corrid AS corr_id,
       to_number(q.enq_tid) AS enq_tid,
       q.step_no,
       decode(q.state,
              0,
              'READY',
              1,
              'WAIT',
              2,
              'PROCESSED',
              3,
              'EXPIRED',
              8,
              'DEFERRED',
              10,
              'BUFFERED_EXPIRED',
              'UNKNOWN') AS msg_state,
       h.retry_count AS retry_count,
       coalesce(s.name, h.name) AS consumer_name,
       q.user_data.get_string_property('msg_type') AS msg_type,
       q.user_data.get_string_property('ename') AS ename,
       q.user_data.get_double_property('old_sal') AS old_sal,
       q.user_data.get_double_property('new_sal') AS new_sal,
       q.user_data.text_vc AS msg_text,
       q.enq_time AS enq_timestamp,
       q.deq_time AS deq_timestamp, -- updated asynchronously, 30 seconds later than "real" dequeue time is not unusual
       q.deq_time - q.enq_time AS time_in_system -- time spent in the system until final status of message has be "registered"
  FROM requests_qt q
  JOIN aq$_requests_qt_h h
    ON h.msgid = q.msgid
  LEFT JOIN aq$_requests_qt_s s
    ON s.subscriber_id = h.subscriber#
 ORDER BY q.enq_time DESC;
