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

CREATE TABLE customer_t (
   id    NUMBER(12)     		CONSTRAINT pk_customer PRIMARY KEY,
   first_name VARCHAR2(20)		NOT NULL,
   last_name  VARCHAR2(50)   	NOT NULL,
   title VARCHAR2(20),
   notification_on VARCHAR2(40),		
   email VARCHAR2(50),
   slack_handle VARCHAR2(20),
   twitter_handle VARCHAR2(20),
   created_at TIMESTAMP(0)			NOT NULL,
   modified_at TIMESTAMP(0)		NOT NULL
);



