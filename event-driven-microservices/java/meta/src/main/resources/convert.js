const jsonSchemaAvro = require('jsonschema-avro')

const inJson = {
 "type": "object",
 "properties": {
   "order_id": {
     "type": "string",
     "description": "System generated Order ID"
   },
   "shoppingCart_id": {
     "type": "string",
     "description": "ID for the shopping cart. Should be a combination of user Id + timestamp \n    of the first item added to the cart."
   },
   "status": {
     "type": "string",
     "enum": [
       "SHOPPING_CART",
       "PROCESSING",
       "SUCCESS",
       "DELIVERING",
       "DELIVERED",
       "CANCELED"
     ]
   },
   "created_at": {
     "type": "string"
   },
   "updated_at": {
     "type": "string"
   },
   "total_price": {
     "type": "number"
   },
   "currency": {
     "type": "string",
     "enum": [
       "GBP",
       "USD",
       "EUR"
     ]
   },
   "payment": {
     "type": "object",
     "properties": {
       "card_type": {
         "type": "string",
         "enum": [
           "VISA_CREDIT",
           "VISA_DEBIT",
           "MASTER_CREDIT",
           "MASTER_DEBIT",
           "AMEX_CREDIT"
         ],
         "description": "Credit card payment methods supported"
       },
       "card_number": {
         "type": "string",
         "description": "Credit card number"
       },
       "start_year": {
         "type": "number",
         "description": "Credit Card issue year"
       },
       "start_month": {
         "type": "number",
         "description": "Credit Card issue month"
       },
       "expiry_year": {
         "type": "number",
         "description": "Credit Card expiry year"
       },
       "expiry_month": {
         "type": "number",
         "description": "Credit Card expiry month"
       }
     },
     "required": [
       "card_type",
       "start_year",
       "start_month",
       "expiry_year",
       "expiry_month"
     ]
   },
   "customer": {
     "type": "object",
     "properties": {
       "customer_id": {
         "type": "string"
       },
       "first_name": {
         "type": "string"
       },
       "last_name": {
         "type": "string"
       },
       "phone": {
         "type": "string"
       },
       "email": {
         "type": "string"
       }
     },
     "required": [
       "customer_id",
       "first_name",
       "last_name",
       "email"
     ]
   },
   "address": {
     "type": "array"
   },
   "line_items": {
     "type": "array"
   },
   "_links": {
     "type": "object",
     "properties": {
       "self": {
         "type": "object",
         "properties": {
           "href": {
             "type": "string"
           }
         },
         "required": [
           "href"
         ]
       }
     }
   },
   "_id": {
     "type": "string",
     "description": "Database generated record ID"
   }
 },
 "required": [
   "status"
 ]

}

const avro = jsonSchemaAvro.convert(inJson)
console.log(avro)
