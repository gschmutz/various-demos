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

CREATE OR REPLACE PACKAGE order_pck 
AS

PROCEDURE insert_order (in_order_obj IN order_objt);
PROCEDURE update_status(in_order_id IN NUMBER, in_new_status IN INTEGER);

END;
/

create or replace PACKAGE BODY order_pck
IS

PROCEDURE send_aq_event(in_order_id IN NUMBER)
IS
      l_enqueue_options sys.dbms_aq.enqueue_options_t;
      l_message_props   sys.dbms_aq.message_properties_t;
      l_jms_message     sys.aq$_jms_text_message := sys.aq$_jms_text_message.construct;
      l_msgid           RAW(16);

      order_json CLOB;

		CURSOR order_sel
		IS
		SELECT json_object('orderId' VALUE po.id,
		          'orderDate' VALUE po.order_date,
		          'orderMode' VALUE po.order_mode,
		          'orderStatus' VALUE DECODE (po.order_status,2,'PROCESSING'),
		          'totalPrice' VALUE po.order_total,
		          'customer' VALUE
		              json_object('firstName' VALUE cu.first_name,
		                          'lastName' VALUE cu.last_name,
		                          'emailAddress' VALUE cu.email),
		          'items' VALUE (SELECT json_arrayagg(
		              json_object('itemNumber' VALUE li.id,
		                     'Product' VALUE
		                       json_object('id' VALUE li.product_id,
		                                   'name' VALUE li.product_name,
		                                   'unitPrice' VALUE li.unit_price),
		                      'quantity' VALUE li.quantity))
		                      FROM order_item_t li WHERE po.id = li.order_id),
		         'offset' VALUE TO_CHAR(po.modified_at, 'YYYYMMDDHH24MISS'))
		FROM order_t po LEFT JOIN customer_t cu ON (po.customer_id = cu.id)
		WHERE po.id = in_order_id;

BEGIN

      OPEN order_sel;
      FETCH order_sel INTO order_json;

      l_jms_message.clear_properties();
      l_message_props.correlation := sys_guid;
      l_message_props.priority := 3;
      l_message_props.expiration := 5;
      l_jms_message.set_string_property('msg_type', 'test');
      l_jms_message.set_text(order_json);
      dbms_aq.enqueue(queue_name         => 'order_aq',
                      enqueue_options    => l_enqueue_options,
                      message_properties => l_message_props,
                      payload            => l_jms_message,
                      msgid              => l_msgid);
END send_aq_event;


PROCEDURE insert_order (in_order_obj IN order_objt)
IS
BEGIN
	INSERT INTO order_t (id, order_date, order_mode, customer_id, order_status, order_total, promotion_id)
	VALUES (in_order_obj.id,
			in_order_obj.order_date,
			in_order_obj.order_mode,
			in_order_obj.customer_id,
			in_order_obj.order_status,
			in_order_obj.order_total,
			in_order_obj.promotion_id);

	FOR i IN 1 .. in_order_obj.order_item_coll.count()
	LOOP
		INSERT INTO order_item_t (id, order_id, product_id, product_name, unit_price, quantity)
		VALUES (in_order_obj.order_item_coll(i).id, 
			    in_order_obj.id,
				in_order_obj.order_item_coll(i).product_id, 
				in_order_obj.order_item_coll(i).product_name,
				in_order_obj.order_item_coll(i).unit_price, 
				in_order_obj.order_item_coll(i).quantity);
	END LOOP;

    -- publish event to AQ
    send_aq_event(in_order_obj.id);
END insert_order;

PROCEDURE update_status(in_order_id IN NUMBER, in_new_status IN INTEGER)
IS
BEGIN
    UPDATE order_t SET order_status = in_new_status
    WHERE id = in_order_id;

    -- publish event to AQ
    send_aq_event(in_order_id);
END update_status;

END;
/