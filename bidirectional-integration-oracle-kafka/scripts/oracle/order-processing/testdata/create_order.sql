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

DECLARE
    order_id INTEGER := 6;
	order_item_obj order_item_objt := order_item_objt((order_id * 10) + 1, 2289, 100, 1);
	order_item_coll order_item_collt := order_item_collt(order_item_obj);
	order_obj order_objt := order_objt(order_id, SYSDATE, 'direct', 101, 1, 100, null, order_item_coll);
BEGIN
    order_pck.insert_order(order_obj);
    commit;
END;    
/

DECLARE
BEGIN
   order_pck.update_status(3, 1);
   commit;
END;
/
