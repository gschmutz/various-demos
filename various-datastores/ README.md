# Various Datastores


## RDBMS

```
SELECT o.order_id, o.customer_id, ol.product_id, ol.quantity
FROM order_t o
LEFT JOIN order_line_t ol
ON (o.order_id = ol.order_id)
```


```
select SUM(quantity * item_price) from order_line_t
```

## Redis

```
SET "product:1 " "{
productId: 1
, name: 'SAMSUNG UE65MU8000'
, description: 'Samsung TV'
, price: 1396.90
, category: 'Electronics'
}"
```

```
SET "product:2 " "{
productId: 2
, name: 'APPLE iPhone X 64 GB Space Grau'
, description: 'Beim iPhone X ist das Gerät das Display. Das Super-Retina-Display füllt die ganze Hand aus und lässt die Augen nicht mehr los. Auf kleinstem Raum arbeiten hier die fortschrittlichen Technologien. Dazu gehören auch die Kameras und Sensoren, die Face ID möglich machen.'
, price: 829.00
, category: 'Electronics'
}"
```

Add customers

```
SET "customer:1" "{
customerId: 1
, firstName: 'Peter'
, lastName: 'Muster'
, gender: 'male'
, addresses: [1,2]
}
"
```
Add addresses

```
SET "address:1" "{
id: 1
, street: 'Musterstrasse'
, nr: '5'
, zipCode: '3001'
, city: 'Bern'
, country: 'Switzerland'
}
"
```

```
SET "address:2" "{
id: 2
, street: 'Seeweg'
, nr: '15'
, zipCode: '3700'
, city: 'Spiez'
, country: 'Switzerland'
}
"
```

```
SET "order:1" "{
orderId: 1
, customerId: 1
, deliveryAddressId: 1
, billingAddressId: 1
, orderDetails: [ { productid: 1, quantity: 1, price: 1396.90 },
	 { productid: 2 quantity: 2, price: 829.00 } 
 ]
}
"
```

<http://localhost:5001>

## Cassandra

```
CREATE KEYSPACE order_processing
  WITH REPLICATION = { 
   'class' : 'SimpleStrategy', 
   'replication_factor' : 1 
  };
```


```
DROP TABLE IF EXISTS order_t;
CREATE TABLE order_t (
	order_id int
	, order_date text STATIC
	, customer_id int STATIC
	, delivery_street text STATIC
	, delivery_city text STATIC
	, billing_street text STATIC
	, billing_city text STATIC
	, order_line_id int
	, product_id int
	, quantity int
	, price double
	, PRIMARY KEY (order_id, order_line_id))
	WITH CLUSTERING ORDER BY (order_line_id ASC);
```
```
INSERT INT order_t (order_id, order_date, customer_id, delivery_street,
 				delivery_city, billing_street, billing_city, order_line_id,
 				product_id, quantity, price)
 			VALUES (1, '10.1.2018', 1, 'Musterstrasse 5', '3000 Bern', 					'Musterstrasse 5', '3000 Bern', 1, 1001, 1, 483.65);
```

## Mongo DB

```
{
orderId: 1
, customerId: 1
, deliveryAddress: { id:1, street:'Musterstrasse',nr:5, city:'Bern', zipCode:'3015'}
, billingAddress: { id:1, street: 'Musterstrasse',nr:5, city:'Bern', zipCode:'3015'}
, orderDetails: [ { productid: 1, quantity: 1, price: 110.00 },
	 { productid: 10, quantity: 1, price: 110.00 } ]
}
```