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

CREATE TABLE order_t (
   id    NUMBER(12)     		CONSTRAINT pk_order PRIMARY KEY,
   order_date  DATE 			NOT NULL,
   order_mode  VARCHAR2(8)   	NOT NULL,
   customer_id NUMBER(12),
   order_status NUMBER(2)       NOT NULL,
   order_total NUMBER(8,2)		NOT NULL,
   promotion_id NUMBER(6),
   created_at TIMESTAMP			NOT NULL,
   modified_at TIMESTAMP		NOT NULL
);



