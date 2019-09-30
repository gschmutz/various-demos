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

INSERT INTO customer_t (ID,FIRST_NAME,LAST_NAME,TITLE,NOTIFICATION_ON,EMAIL,SLACK_HANDLE,TWITTER_HANDLE) 
	VALUES (101,'Peter','Muster','Mr',NULL,'peter.muster@somecomp.com',NULL,NULL);

COMMIT;


