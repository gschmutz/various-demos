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

CREATE TABLE order_item_t (
   id       NUMBER(3)   CONSTRAINT pk_order_item PRIMARY KEY,
   order_id NUMBER(12)  NOT NULL CONSTRAINT fk_order REFERENCES order_t,
   product_id NUMBER(6) NOT NULL,
   product_name VARCHAR2(50) NOT NULL,
   unit_price NUMBER(8,2) NOT NULL,
   quantity NUMBER(8) NOT NULL,
   created_at TIMESTAMP(0)	NOT NULL,
   modified_at TIMESTAMP(0)	NOT NULL
);




