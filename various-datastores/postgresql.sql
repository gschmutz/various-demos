DROP SCHEMA IF EXISTS "order_processing" CASCADE;
CREATE SCHEMA "order_processing";

/* Foreign Keys */

CREATE TABLE "order_t" (
  "order_id" serial NOT NULL,
  "customer_id" integer NOT NULL,
  "delivery_address_id" integer NOT NULL,
  "billing_address_id" integer NULL
);

ALTER TABLE "order_t"
ADD CONSTRAINT "order_t_order_id" PRIMARY KEY ("order_id");

CREATE TABLE "order_line_t" (
  "order_line_id" serial NOT NULL,
  "order_id" integer NOT NULL,
  "product_id" integer NOT NULL,
  "quantity" integer NOT NULL,
  "item_price" money NOT NULL
);

ALTER TABLE "order_line_t"
ADD CONSTRAINT "order_line_t_order_line_id" PRIMARY KEY ("order_line_id");

CREATE TABLE "customer_t" (
  "customer_id" serial NOT NULL,
  "first_name" character varying(50) NOT NULL,
  "last_name" character varying(50) NOT NULL,
  "gender" character(1) NOT NULL
);

ALTER TABLE "customer_t"
ADD CONSTRAINT "customer_t_customer_id" PRIMARY KEY ("customer_id");

CREATE TABLE "product_t" (
  "product_id" serial NOT NULL,
  "category_id" integer NOT NULL,
  "name" character varying(100) NOT NULL,
  "price" money NOT NULL,
  "description" character varying(1000) NOT NULL
);

ALTER TABLE "product_t"
ADD CONSTRAINT "product_t_product_id" PRIMARY KEY ("product_id");

CREATE TABLE "category_t" (
  "category_id" serial NOT NULL,
  "name" character varying(100) NOT NULL
);

ALTER TABLE "category_t"
ADD CONSTRAINT "category_t_category_id" PRIMARY KEY ("category_id");

CREATE TABLE "address_t" (
  "address_id" serial NOT NULL,
  "street" character varying(100) NOT NULL,
  "nr" character varying(10) NOT NULL,
  "zipCode" character varying(10) NOT NULL,
  "city" character varying(100) NOT NULL,
  "country_id" integer NULL
);

ALTER TABLE "address_t"
ADD CONSTRAINT "address_t_address_id" PRIMARY KEY ("address_id");

CREATE TABLE "customer_address_t" (
  "customer_address_id" serial NOT NULL,
  "customer_id" integer NOT NULL,
  "address_id" integer NOT NULL,
  "address_type" character varying(10) NOT NULL
);

ALTER TABLE "customer_address_t"
ADD CONSTRAINT "customer_address_t_customer_address_id" PRIMARY KEY ("customer_address_id");

CREATE TABLE "country_t" (
  "country_id" serial NOT NULL,
  "name" character varying(100) NOT NULL,
  "code" character varying(10) NOT NULL
);

ALTER TABLE "country_t"
ADD CONSTRAINT "country_t_country_id" PRIMARY KEY ("country_id");


/* Foreign Keys */

ALTER TABLE "order_line_t"
ADD FOREIGN KEY ("order_id") REFERENCES "order_t" ("order_id") ON DELETE CASCADE ON UPDATE RESTRICT;

ALTER TABLE "order_line_t"
ADD FOREIGN KEY ("product_id") REFERENCES "product_t" ("product_id") ON DELETE CASCADE ON UPDATE RESTRICT;

ALTER TABLE "order_t"
ADD FOREIGN KEY ("customer_id") REFERENCES "customer_t" ("customer_id") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "product_t"
ADD FOREIGN KEY ("category_id") REFERENCES "category_t" ("category_id") ON DELETE CASCADE ON UPDATE RESTRICT;

ALTER TABLE "customer_address_t"
ADD FOREIGN KEY ("customer_id") REFERENCES "customer_t" ("customer_id") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "customer_address_t"
ADD FOREIGN KEY ("address_id") REFERENCES "address_t" ("address_id") ON DELETE RESTRICT ON UPDATE RESTRICT;

ALTER TABLE "address_t"
ADD FOREIGN KEY ("country_id") REFERENCES "country_t" ("country_id") ON DELETE RESTRICT ON UPDATE RESTRICT;

INSERT INTO "country_t" ("country_id", "name", "code") VALUES
(1,	'Switzerland',	'CH'),
(2,	'Germany',	'GE');

INSERT INTO "category_t" ("category_id", "name") VALUES
(1,	'Electronics'),
(2,	'Food');

INSERT INTO "product_t" ("product_id", "category_id", "name", "price", "description") VALUES
(1,	1,	'SAMSUNG UE65MU8000',	1396.90,	'Samsung TV'),
(2,	1,	'APPLE iPhone X 64 GB Space Grau',	829.00,	'Beim iPhone X ist das Gerät das Display. Das Super-Retina-Display füllt die ganze Hand aus und lässt die Augen nicht mehr los. Auf kleinstem Raum arbeiten hier die fortschrittlichen Technologien. Dazu gehören auch die Kameras und Sensoren, die Face ID möglich machen.');

INSERT INTO "customer_t" ("customer_id", "first_name", "last_name", "gender") VALUES
(1,	'Peter',	'Muster',	'M'),
(2,	'Gaby',	'Steiner',	'F');

INSERT INTO "address_t" ("address_id", "street", "nr", "zipCode", "city", "country_id") VALUES
(1,	'Musterstrasse',	'5',	'3001',	'Bern',	1);
INSERT INTO "address_t" ("address_id", "street", "nr", "zipCode", "city", "country_id") VALUES
(2,	'Seeweg',	'15',	'3700',	'Spiez', 1);

INSERT INTO "customer_address_t" ("customer_address_id", "customer_id", "address_id", "address_type") VALUES
(1,	1,	1,	'HOME'),
(2,	1,	2,	'HOME');

INSERT INTO "order_t" ("order_id", "customer_id", "delivery_address_id", "billing_address_id") VALUES
(1,	1,	1,	NULL);

INSERT INTO "order_line_t" ("order_line_id", "order_id", "product_id", "quantity", "item_price") VALUES
(1,	1,	1,	1,	1396.90),
(2,	1,	2,	2,	829.00);

