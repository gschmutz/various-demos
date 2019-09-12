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


CREATE TYPE order_item_objt AS OBJECT (
   id NUMBER,
   product_id NUMBER,
   unit_price NUMBER(8,2)
   quantity NUMBER(8)
);
/

CREATE TYPE order_item_collt AS TABLE OF order_item_objt;
/

CREATE TYPE order_objt AS OBJECT (
	id NUMBER,
   order_date  DATE,
   order_mode  VARCHAR2(8),
   customer_id NUMBER(12),
   order_status NUMBER(2),
   order_total NUMBER(8,2),
   promotion_id NUMBER(6),
   order_item_coll order_item_collt
);
/
