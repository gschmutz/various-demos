# Kafka GeoFencing Demo
This demo shows how to use GeoFencing with Kafka. 

## Links

  * <http://geojson.io/>
  * <http://geohash.gofreerange.com/>
  * <https://www.google.com/maps/d/u/0/>

## Setup Static Data in PostgreSQL (not needed anymore)

The infrastructure we have started above also contains an instance of Postgresql in a separate docker container.

Let's connect to that container

```
docker exec -ti postgresql psql -d sample -U sample
```


### Vehicle Table

```
DROP TABLE vehicle;
CREATE TABLE vehicle (id BIGINT
            , name CHARACTER VARYING(45)
            , last_update TIMESTAMP);
ALTER TABLE vehicle ADD CONSTRAINT vehicle_pk PRIMARY KEY (id);
```

```
INSERT INTO vehicle (id, name, last_update)
VALUES (1, 'Vehicle-1', CURRENT_TIMESTAMP);

INSERT INTO vehicle (id, name, last_update)
VALUES (2, 'Vehicle-2', CURRENT_TIMESTAMP);

INSERT INTO vehicle (id, name, last_update)
VALUES (3, 'Vehicle-3', CURRENT_TIMESTAMP);

INSERT INTO vehicle (id, name, last_update)
VALUES (4, 'Vehicle-4', CURRENT_TIMESTAMP);

INSERT INTO vehicle (id, name, last_update)
VALUES (10, 'Vehicle-10', CURRENT_TIMESTAMP);
```

### Geo Fence Table

```
DROP TABLE geo_fence;
CREATE TABLE geo_fence (id BIGINT
            , name CHARACTER VARYING(45)
            , geometry_wkt CHARACTER VARYING(2000)
            , last_update TIMESTAMP);
ALTER TABLE geo_fence ADD CONSTRAINT geo_fence_pk PRIMARY KEY (id);
```

```
INSERT INTO geo_fence (id, name, geometry_wkt, last_update)
VALUES (1, 'Colombia, Missouri', 'POLYGON ((-90.23345947265625 38.484769753492536, -90.25886535644531 38.47455675836861, -90.25886535644531 38.438530965643004, -90.23826599121092 38.40356337960024, -90.19088745117188 38.39818224865764, -90.16685485839844 38.435841752321856, -90.16891479492188 38.47616943274547, -90.23345947265625 38.484769753492536))', CURRENT_TIMESTAMP);

INSERT INTO geo_fence (id, name, geometry_wkt, last_update)
VALUES (2, 'St. Louis, Missouri', 'POLYGON ((-90.25749206542969 38.71551876930462, -90.31723022460938 38.69301319283493, -90.3247833251953 38.64744452237617, -90.31997680664062 38.58306291549108, -90.27053833007812 38.55460931253295, -90.22109985351562 38.54601733154524, -90.15037536621094 38.55299839430547, -90.11123657226562 38.566421609878674, -90.08583068847656 38.63028174397134, -90.08583068847656 38.66996443163297, -90.0933837890625 38.718197532760165, -90.15243530273436 38.720876195817276, -90.25749206542969 38.71551876930462))', CURRENT_TIMESTAMP);

INSERT INTO geo_fence (id, name, geometry_wkt, last_update)
VALUES (3, 'Berlin, Germany', 'POLYGON ((13.297920227050781 52.56195151687443, 13.2440185546875 52.530216577830124, 13.267364501953125 52.45998421679598, 13.35113525390625 52.44826791583386, 13.405036926269531 52.44952338289473, 13.501167297363281 52.47148826410652, 13.509750366210938 52.489261333143126, 13.509063720703125 52.53710835019913, 13.481597900390625 52.554854904263195, 13.41156005859375 52.57217696877135, 13.37207794189453 52.5748894436198, 13.297920227050781 52.56195151687443))', CURRENT_TIMESTAMP);
```

```
docker exec -ti broker-1 kafka-topics --create --zookeeper zookeeper-1:2181 --topic geo_fence --replication-factor 3 --partitions 8
```

Enrichment of geo_fence by vehicle

```
ALTER TABLE geo_fence RENAME TO geo_fence_t;

CREATE VIEW geo_fence
AS
SELECT veh.id      AS vehicle_id
,   veh.name       AS vehicle_name
,	geof.geometry_wkt
,  GREATEST(geof.last_update, veh.last_update)    AS last_update
FROM geo_fence_t geof
CROSS JOIN vehicle veh;
```

## Using Kafka Connect to integrate with Kafka

### Sync GeoFences

To sync the `geo_fence` table from PostgreSQL into the Kafka Topic `geo_fence` use the following script

```
#!/bin/bash

echo "removing JDBC Source Connector"

curl -X "DELETE" "http://$DOCKER_HOST_IP:8083/connectors/geo_fence_source"

echo "creating JDBC Source Connector"

## Request
curl -X "POST" "$DOCKER_HOST_IP:8083/connectors" \
     -H "Content-Type: application/json" \
     -d $'{
  "name": "geo_fence_source",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "tasks.max": "1",
    "connection.url":"jdbc:postgresql://postgresql/sample?user=sample&password=sample",
    "mode": "timestamp",
    "timestamp.column.name":"last_update",
    "table.whitelist":"geo_fence",
    "validate.non.null":"false",
    "topic.prefix":"",
    "key.converter":"org.apache.kafka.connect.storage.StringConverter",
    "key.converter.schemas.enable": "false",
    "value.converter":"org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "name": "geo_fence_source",
    "transforms":"createKey,extractInt",
    "transforms.createKey.type":"org.apache.kafka.connect.transforms.ValueToKey",
    "transforms.createKey.fields":"id",
    "transforms.extractInt.type":"org.apache.kafka.connect.transforms.ExtractField$Key",
    "transforms.extractInt.field":"id"
  }
}'
```

```
docker exec -ti broker-1 kafka-topics --create --zookeeper zookeeper-1:2181 --topic geo_fence --replication-factor 3 --partitions 8
```


### Sync Vehicles

To sync the `vehicle` table from PostgreSQL into the Kafka Topic `vehicle` use the following script

```
#!/bin/bash

echo "removing JDBC Source Connector"

curl -X "DELETE" "http://$DOCKER_HOST_IP:8083/connectors/vehicle_source"

echo "creating JDBC Source Connector"

## Request
curl -X "POST" "$DOCKER_HOST_IP:8083/connectors" \
     -H "Content-Type: application/json" \
     -d $'{
  "name": "vehicle_source",
  "config": {
    "connector.class": "io.confluent.connect.jdbc.JdbcSourceConnector",
    "tasks.max": "1",
    "connection.url":"jdbc:postgresql://postgresql/sample?user=sample&password=sample",
    "mode": "timestamp",
    "timestamp.column.name":"last_update",
    "table.whitelist":"vehicle",
    "validate.non.null":"false",
    "topic.prefix":"",
    "key.converter":"org.apache.kafka.connect.storage.StringConverter",
    "key.converter.schemas.enable": "false",
    "value.converter":"org.apache.kafka.connect.json.JsonConverter",
    "value.converter.schemas.enable": "false",
    "name": "geo_fence_source",
    "transforms":"createKey,extractInt",
    "transforms.createKey.type":"org.apache.kafka.connect.transforms.ValueToKey",
    "transforms.createKey.fields":"id",
    "transforms.extractInt.type":"org.apache.kafka.connect.transforms.ExtractField$Key",
    "transforms.extractInt.field":"id"
  }
}'
```

```
docker exec -ti broker-1 kafka-topics --create --zookeeper zookeeper-1:2181 --topic vehicle --replication-factor 3 --partitions 8
```

## Create GeoFences (no longer needed, now in Zeppelin)

```
docker exec -ti broker-1 kafka-topics --create --zookeeper zookeeper-1:2181 --topic geo_fence --replication-factor 3 --partitions 8
```

```
echo '1:{"id":1,"name":"Colombia, Missouri","wkt":"POLYGON ((-90.23345947265625 38.484769753492536, -90.25886535644531 38.47455675836861, -90.25886535644531 38.438530965643004, -90.23826599121092 38.40356337960024, -90.19088745117188 38.39818224865764, -90.16685485839844 38.435841752321856, -90.16891479492188 38.47616943274547, -90.23345947265625 38.484769753492536))","last_update":1560632581060}' | kafkacat -b streamingplatform -t geo_fence -K:
```

```
echo '2:{"id":2,"name":"St. Louis, Missouri","wkt":"POLYGON ((-90.25749206542969 38.71551876930462, -90.31723022460938 38.69301319283493, -90.3247833251953 38.64744452237617, -90.31997680664062 38.58306291549108, -90.27053833007812 38.55460931253295, -90.22109985351562 38.54601733154524, -90.15037536621094 38.55299839430547, -90.11123657226562 38.566421609878674, -90.08583068847656 38.63028174397134, -90.08583068847656 38.66996443163297, -90.0933837890625 38.718197532760165, -90.15243530273436 38.720876195817276, -90.25749206542969 38.71551876930462))","last_update":1560632392130}' | kafkacat -b streamingplatform -t geo_fence -K:
```

```
echo '3:{"id":3,"name":"Berlin, Germany","wkt":"POLYGON ((13.297920227050781 52.56195151687443, 13.2440185546875 52.530216577830124, 13.267364501953125 52.45998421679598, 13.35113525390625 52.44826791583386, 13.405036926269531 52.44952338289473, 13.501167297363281 52.47148826410652, 13.509750366210938 52.489261333143126, 13.509063720703125 52.53710835019913, 13.481597900390625 52.554854904263195, 13.41156005859375 52.57217696877135, 13.37207794189453 52.5748894436198, 13.297920227050781 52.56195151687443))","last_update":1560669937877}' | kafkacat -b streamingplatform -t geo_fence -K:
```

Delete the geofence with ID 3

```
echo '3:' | kafkacat -b streamingplatform -t geo_fence -K:
```

## Simulating Vehicle Position

in Zeppelin Notebook

# KSQL

Connect to KSQL Server

```
docker run -it --network docker_default confluentinc/cp-ksql-cli:5.3.0 http://ksql-server-1:8088
```

## Create the Vehicle Position KSQL Stream


Create the stream with the vehicle positions

```
DROP STREAM IF EXISTS vehicle_position_s;

CREATE STREAM vehicle_position_s
  WITH (kafka_topic='vehicle_position',
        value_format='AVRO');
```

```
DESCRIBE vehicle_position_s;
```

## GeoFence UDF

```
SELECT latitude, longitude, geo_fence(latitude, longitude, 'POLYGON ((13.297920227050781 52.56195151687443, 13.2440185546875 52.530216577830124, 13.267364501953125 52.45998421679598, 13.35113525390625 52.44826791583386, 13.405036926269531 52.44952338289473, 13.501167297363281 52.47148826410652, 13.509750366210938 52.489261333143126, 13.509063720703125 52.53710835019913, 13.481597900390625 52.554854904263195, 13.41156005859375 52.57217696877135, 13.37207794189453 52.5748894436198, 13.297920227050781 52.56195151687443))') geo_fence_status
FROM test_geo_udf_s;
```

Test with a LatLong which is OUTSIDE of the geo fence

```
echo '10:{"id":"10", "latitude":"52.4497", "longitude":"13.3096" }' | kafkacat -b streamingplatform -t test_geo_udf -K:
```

Test with a LatLong which is INSIDE of the geo fence

```
echo '10:{"id":"10", "latitude":"52.4556", "longitude":"13.3178" }' | kafkacat -b streamingplatform -t test_geo_udf -K:
```


## Implementing GeoFence Analytics in KSQL


### Attempt 1: Perform a Cross-Join (does not work!)

```
DROP TABLE IF EXISTS geo_fence_t;

CREATE TABLE geo_fence_t 
WITH (KAFKA_TOPIC='geo_fence',
      VALUE_FORMAT='AVRO',
      KEY = 'id');
```

```
set 'auto.offset.reset'='earliest';
SELECT * FROM geo_fence_t;
```

```
CREATE STREAM a01_vehp_join_geof_s
WITH (PARTITIONS=8, KAFKA_TOPIC='vehp_join_geof', VALUE_FORMAT='AVRO')
AS
SELECT vehp.id, vehp.latitude, vehp.longitude,        geof.geometry_wkt
FROM vehicle_position_s vehp
CROSS JOIN geo_fence_t geof;
```

### Attempt 2: Perform an Inner-Join (does not work!)

What if we try with an inner join in KSQL on an artifical single group

```
SELECT vp.id, vp.latitude, vp.longitude,        gf.geometry_wkt
FROM vehicle_position_s vp
INNER JOIN a02_geo_fence_t gf
WHERE vp.group = gf.group;
```

But both `geo_fence` and `vehicle_position` do not contain this `group` column. But we can use an enrichment KSQL SELECT to add the group 

```
DROP STREAM IF EXISTS a02_geo_fence_s;

CREATE STREAM a02_geo_fence_s 
      (id BIGINT, 
      name VARCHAR, 
      geometry_wkt VARCHAR)
WITH (KAFKA_TOPIC='geo_fence', 
      VALUE_FORMAT='AVRO',
      KEY = 'id');
```

```
docker exec -ti broker-1 kafka-topics --create --zookeeper zookeeper-1:2181 --topic a02_geo_fence --replication-factor 3 --partitions 8
```

```
DROP TABLE IF EXISTS a02_geo_fence_t;

CREATE TABLE a02_geo_fence_t 
      (group_id BIGINT,
      id BIGINT,  
      name VARCHAR,
      geometry_wkt VARCHAR)
WITH (KAFKA_TOPIC='a02_geo_fence',
      VALUE_FORMAT='AVRO',
      KEY = 'group_id');
```

set 'auto.offset.reset'='earliest';

```
INSERT INTO a02_geo_fence_t
SELECT '1' AS group_id, geof.id, geof.name, geof.geometry_wkt
FROM a02_geo_fence_s geof;
```

```
CREATE TABLE a02a_geo_fence_t
WITH (PARTITIONS=8, KAFKA_TOPIC='a02a_geo_fence', VALUE_FORMAT='AVRO')
AS
SELECT max(1) AS group_id, geof.id, geof.name, geof.geometry_wkt
FROM a02_geo_fence_s geof
GROUP BY 1;
```

### Attempt 3: aggregate geofences by single group

```
DROP STREAM IF EXISTS a03_geo_fence_s;
CREATE STREAM a03_geo_fence_s 
WITH (KAFKA_TOPIC='geo_fence', 
        VALUE_FORMAT='AVRO',
        KEY='id');
```

```
DROP STREAM a03_geo_fence_by_group_s DELETE TOPIC;

CREATE STREAM a03_geo_fence_by_group_s
WITH (PARTITIONS=8, KAFKA_TOPIC='a03_geo_fence_by_group', VALUE_FORMAT='JSON')
AS
SELECT '1' AS group_id
,    id
,    name
,    wkt
FROM a03_geo_fence_s  geof
PARTITION BY group_id;
```

```
DROP TABLE a03_geo_fence_aggby_group_t DELETE TOPIC;

CREATE TABLE a03_geo_fence_aggby_group_t
WITH (PARTITIONS=8, KAFKA_TOPIC='a03_geo_fence_aggby_group', VALUE_FORMAT='AVRO')
AS
SELECT group_id
,   collect_set(id)					id_list
,   collect_set(CAST(id AS VARCHAR) + ':' + wkt) AS id_wkt_list
FROM a03_geo_fence_by_group_s	  geof
GROUP BY group_id;
```

The extra step with a stream and then the group by is necessary, to avoid an error later when joining `vehicle_pos_by_group` with `geofence_aggby_group`:


```
ksql> SELECT vehp.id, vehp.latitude, vehp.longitude, geofagg.geometry_wkt_list
>FROM a03_vehicle_position_by_group_s vehp
>INNER JOIN a03_geo_fence_aggby_group_t geofagg
>ON vehp.group_id = geofagg.group_id;
Source table (GEOFAGG) key column ('1') is not the column used in the join criteria (GROUP_ID).
Statement: SELECT vehp.id, vehp.latitude, vehp.longitude, geofagg.geometry_wkt_list
FROM a03_vehicle_position_by_group_s vehp
INNER JOIN a03_geo_fence_aggby_group_t geofagg
ON vehp.group_id = geofagg.group_id;
Caused by: Source table (GEOFAGG) key column ('1') is not the column used in the
	join criteria (GROUP_ID).
```

Create a new stream `a03_vehicle_position_by_group_s` which enriches the vehicle_position with the "artificial" group id

```
DROP STREAM a03_vehicle_position_by_group_s DELETE TOPIC;

CREATE STREAM a03_vehicle_position_by_group_s
WITH (PARTITIONS=8, KAFKA_TOPIC='a03_vehicle_position_by_group', VALUE_FORMAT='AVRO')
AS
SELECT '1' group_id, vehp.vehicleId, vehp.latitude, vehp.longitude
FROM vehicle_position_s vehp
PARTITION BY group_id;
```

```
DROP STREAM a03_geo_fence_status_s DELETE TOPIC;

CREATE STREAM a03_geo_fence_status_s
WITH (PARTITIONS=8, KAFKA_TOPIC='vehp_join_geof_aggby_vehp', VALUE_FORMAT='AVRO')
AS
SELECT vehp.vehicleId, vehp.latitude, vehp.longitude, geo_fence_bulk(vehp.latitude, vehp.longitude, geofagg.id_wkt_list) geofence_status
FROM a03_vehicle_position_by_group_s vehp
LEFT JOIN a03_geo_fence_aggby_group_t geofagg
ON vehp.group_id = geofagg.group_id;
```


### Attempt 4: aggregate by geohash

Create a Stream on the `geo_fence`topic

```
DROP STREAM a04_geo_fence_s;

CREATE STREAM a04_geo_fence_s 
  WITH (kafka_topic='geo_fence', 
        value_format='AVRO',
        key='id');
```

Create a new stream `a04_geo_fence_by_geohash_s` which enriches the GeoFences with the GeoHashes they belong to (currently using precision of 3, but can be increased or reduced upon use-case). As there can be multiple geo-fences covering the geometry of the geo-fence, we have to "explode" it using a first create followed by multiple inserts. Currently we do it for a total of 4 geo hashes (array position 0 to 3).

```
DROP STREAM IF EXISTS a04_geo_fence_by_geohash_s DELETE TOPIC;

CREATE STREAM a04_geo_fence_by_geohash_s
WITH (PARTITIONS=8, kafka_topic='a04_geo_fence_by_geohash', value_format='AVRO')
AS
SELECT geo_hash(wkt, 3)[0] geo_hash, id, name, wkt
FROM a04_geo_fence_s
PARTITION by geo_hash;
```

```
INSERT INTO a04_geo_fence_by_geohash_s
SELECT geo_hash(wkt, 3)[1] geo_hash, id, name, wkt
FROM a04_geo_fence_s
WHERE geo_hash(wkt, 3)[1] IS NOT NULL
PARTITION BY geo_hash;
```

```
INSERT INTO a04_geo_fence_by_geohash_s
SELECT geo_hash(wkt, 3)[2] geo_hash, id, name, wkt
FROM a04_geo_fence_s
WHERE geo_hash(wkt, 3)[2] IS NOT NULL
PARTITION BY geo_hash;
```

```
INSERT INTO a04_geo_fence_by_geohash_s
SELECT geo_hash(wkt, 3)[3] geo_hash, id, name, wkt
FROM a04_geo_fence_s
WHERE geo_hash(wkt, 3)[3] IS NOT NULL
PARTITION BY geo_hash;
```


Now we create a table which groups the geo-fences by geohash and creates a set with all geometries per geohash. Can be 1 to many, depending on how many geo-fence geometries belong to a given geohash. 

```
DROP TABLE IF EXISTS a04_geo_fence_by_geohash_t DELETE TOPIC;

CREATE TABLE a04_geo_fence_by_geohash_t
WITH (PARTITIONS=8, KAFKA_TOPIC='geo_fence_by_geohash_t', VALUE_FORMAT='AVRO')
AS
SELECT geo_hash, COLLECT_SET(CAST (id AS VARCHAR) + ':' + wkt) id_wkt_list, COLLECT_SET(wkt) wkt_list, COLLECT_SET(id) id_list
FROM a04_geo_fence_by_geohash_s
GROUP BY geo_hash;
```

Create a new stream `a04_vehicle_position_by_geohash_s` which enriches the vehicle positions with the geohash the LatLong belongs to ((currently using precision of 3, but can be increased or reduced upon use-case, but needs to be the same as above for the geo fences).

```
DROP STREAM IF EXISTS a04_vehicle_position_by_geohash_s DELETE TOPIC;

CREATE STREAM a04_vehicle_position_by_geohash_s
WITH (PARTITIONS=8, KAFKA_TOPIC='vehicle_position_by_geohash', value_format='AVRO')
AS
SELECT vp.vehicleId, vp.latitude, vp.longitude, geo_hash(vp.latitude, vp.longitude, 3) geo_hash
FROM vehicle_position_s vp
PARTITION BY geo_hash;
```

now call the geo_fence UDF

```
DROP STREAM a04_geo_fence_status_s DELETE TOPIC;

CREATE STREAM a04_geo_fence_status_s
WITH (PARTITIONS=8, KAFKA_TOPIC='vehicle_position_by_geohash', value_format='AVRO')
AS
SELECT vp.vehicleId, vp.latitude, vp.longitude, vp.geo_hash, gf.wkt_list,
geo_fence_bulk (vp.latitude, vp.longitude, gf.id_wkt_list) fence_status
FROM a04_vehicle_position_by_geohash_s vp \
LEFT JOIN a04_geo_fence_by_geohash_t gf \
ON (vp.geo_hash = gf.geo_hash);
```

### Attempt 4a: call Kafka from geo_fence UDF

```
DROP STREAM a04b_geofence_udf_status_s DELETE TOPIC;

CREATE STREAM a04a_geofence_udf_status_s
WITH (PARTITIONS=8, KAFKA_TOPIC='04a_geofence_udf_status', value_format='AVRO')
AS
SELECT vp.vehicleId, vp.latitude, vp.longitude, vp.geo_hash, gf.wkt_list,
geo_fence_bulk (vp.latitude, vp.longitude, gf.id_wkt_list, 'broker-1:9092,broker-2:9093') fence_status
FROM a04_vehicle_position_by_geohash_s vp \
LEFT JOIN a04_geo_fence_by_geohash_t gf \
ON (vp.geo_hash = gf.geo_hash);
```

### Attempt 4b: Explode geometry_wkt_list before calling geofence

```
DROP STREAM a04b_geofence_udf_status_s DELETE TOPIC;

CREATE STREAM a04b_geofence_udf_status_s
WITH (PARTITIONS=8, KAFKA_TOPIC='04b_geofence_udf_status', value_format='AVRO')
AS 
SELECT vehicleId, latitude, longitude, id_list[0] geofence_id, geo_fence(latitude, longitude, wkt_list[0]) geofence_status
FROM a04_vehicle_position_by_geohash_s vp \
LEFT JOIN a04_geo_fence_by_geohash_t gf \
ON (vp.geo_hash = gf.geo_hash);

INSERT INTO a04b_geofence_udf_status_s
SELECT vehicleId, latitude, longitude, id_list[1] geofence_id, geo_fence(latitude, longitude, wkt_list[1]) geofence_status
FROM a04_vehicle_position_by_geohash_s vp \
LEFT JOIN a04_geo_fence_by_geohash_t gf \
ON (vp.geo_hash = gf.geo_hash)
WHERE id_list[1] IS NOT NULL;

INSERT INTO a04b_geofence_udf_status_s
SELECT vehicleId, latitude, longitude, id_list[2] geofence_id, geo_fence(latitude, longitude, wkt_list[2]) geofence_status
FROM a04_vehicle_position_by_geohash_s vp \
LEFT JOIN a04_geo_fence_by_geohash_t gf \
ON (vp.geo_hash = gf.geo_hash)
WHERE id_list[2] IS NOT NULL;

```

## Working with Tile 38

```
docker exec -ti tile38 tile38-cli
```

```
SET city berlin OBJECT {"type":"Polygon","coordinates": [[[13.297920227050781,52.56195151687443],[13.2440185546875,52.530216577830124],[13.267364501953125,52.45998421679598],[13.35113525390625,52.44826791583386],[13.405036926269531,52.44952338289473],[13.501167297363281,52.47148826410652],[13.509750366210938,52.489261333143126],[13.509063720703125,52.53710835019913],[13.481597900390625,52.554854904263195],[13.41156005859375,52.57217696877135],[13.37207794189453,52.5748894436198],[13.297920227050781,52.56195151687443]]]}
```

### Geofence CHANNEL

```
SETCHAN berlin WITHIN vehicle FENCE OBJECT {"type":"Polygon","coordinates":[[[13.297920227050781,52.56195151687443],[13.2440185546875,52.530216577830124],[13.267364501953125,52.45998421679598],[13.35113525390625,52.44826791583386],[13.405036926269531,52.44952338289473],[13.501167297363281,52.47148826410652],[13.509750366210938,52.489261333143126],[13.509063720703125,52.53710835019913],[13.481597900390625,52.554854904263195],[13.41156005859375,52.57217696877135],[13.37207794189453,52.5748894436198],[13.297920227050781,52.56195151687443]]]}
```

```
SUBSCRIBE berlin
```

Point OUTSIDE

```
SET vehicle 10 POINT 52.4497 13.3096
```

Point INSIDE

```
SET vehicle 10 POINT 52.4556 13.3178
```


### geofence HOOK


```
docker exec -ti broker-1 kafka-topics --create --zookeeper zookeeper-1:2181 --topic tile38_geofence_status --replication-factor 3 --partitions 1
```


```
SETHOOK berlin_hook kafka://broker-1:9092/tile38_geofence_status WITHIN vehicle FENCE OBJECT {"type":"Polygon","coordinates":[[[13.297920227050781,52.56195151687443],[13.2440185546875,52.530216577830124],[13.267364501953125,52.45998421679598],[13.35113525390625,52.44826791583386],[13.405036926269531,52.44952338289473],[13.501167297363281,52.47148826410652],[13.509750366210938,52.489261333143126],[13.509063720703125,52.53710835019913],[13.481597900390625,52.554854904263195],[13.41156005859375,52.57217696877135],[13.37207794189453,52.5748894436198],[13.297920227050781,52.56195151687443]]]}
```

Point OUTSIDE

```
SET vehicle 10 POINT 52.4497 13.3096
```

Point INSIDE

```
SET vehicle 10 POINT 52.4556 13.3178
```

### Integration with KSQL

### Integration with Kafka Connect

```
curl -X PUT \
  /api/kafka-connect-1/connectors/Tile38SinkConnector/config \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json' \
  -d '{
  "connector.class": "com.trivadis.geofence.kafka.connect.Tile38SinkConnector",
  "topics": "vehicle_position",
  "tasks.max": "1",
  "tile38.key": "vehicle",
  "tile38.operation": "SET",
  "tile38.hosts": "tile38:9851"
}'
```

-------

### Geo Fences aggregated

1st approach: Create aggregation of all the geometries

```
CREATE VIEW geo_fence_gpt_v
AS
SELECT '1' AS id
      , string_agg(geometry_wkt, ', ') AS geometry_list
      , max(last_update) AS last_update
FROM geo_fence
GROUP BY 1;
```

and join it to the vehicle table

```
CREATE VIEW geo_fence_v
AS
SELECT ve.id      vehicle_id
,   ve.name.      vehicle_name
,	geometry_list
,  GREATEST(gf.last_update, ve.last_update)    AS last_update
FROM geo_fence_gpt_v gf
CROSS JOIN vehicle ve;
```

2nd approach: Build a cross-join between table `geo_fence` and `vehicle`

```
DROP VIEW geo_fence_v;

CREATE VIEW geo_fence_v
AS
SELECT concat (ve.id, ':', gf.id) AS id  
,  ve.id AS vehicle_id
,  ve.name AS vehicle_name
,  gf.name AS geofence_name
,  gf.geometry_wkt
,  GREATEST(gf.last_update, ve.last_update) AS last_update
FROM geo_fence gf
CROSS JOIN vehicle ve;
```


## Geo Fence KSQL Table

```
docker run -it --network docker_default confluentinc/cp-ksql-cli:5.2.1 http://ksql-server-1:8088
```


```
DROP TABLE geo_fence_t;

CREATE TABLE geo_fence_t  \
      (id BIGINT,  \
      name VARCHAR, \
      geometry_wkt VARCHAR) \
WITH (KAFKA_TOPIC='geo_fence', \
      VALUE_FORMAT='JSON', \
      KEY = 'id');
```

```
DROP TABLE geo_fence2_t;

CREATE TABLE geo_fence2_t  \
      (id VARCHAR,
      vehicle_id BIGINT,  \
      vehicle_name VARCHAR, \
      geofence_name VARCHAR, \
      geometry_wkt VARCHAR) \
WITH (KAFKA_TOPIC='geo_fence_v', \
      VALUE_FORMAT='JSON', \
      KEY = 'id');
```

```
SELECT vehicle_id, geofence_name FROM geo_fence2_t;
```

```
./scripts/start-connect-jdbc.sh
```


## Geo Fence KSQL

Create the stream with the vehicle positions

```
DROP STREAM vehicle_position_s;
CREATE STREAM vehicle_position_s \
  (id VARCHAR, \
   truck_id VARCHAR, \
   latitude DOUBLE, \
   longitude DOUBLE) \
  WITH (kafka_topic='vehicle_position', \
        value_format='DELIMITED');
```

Publish position outside of geofence #1 (Columbia, Missouri)

```
echo '10:1,1,38.3900,-90.1840' | kafkacat -b streamingplatform -t vehicle_position -K:
```

Publish position inside geofence #1 (Columbia, Missouri)

```
echo '10:1,1,38.4147,-90.1981' | kafkacat -b streamingplatform -t vehicle_position -K:
```

Create the Geo Fence Stream holding status of INSIDE or OUTSIDE

```
DROP STREAM vehicle_geofence_s;
CREATE STREAM vehicle_geofence_s \
WITH (kafka_topic='vehicle_geofence', \
        value_format='DELIMITED', \
        partitions=8) \ 
AS \
SELECT vp.truck_id, geo_fence(vp.latitude, vp.longitude, gf.geometry_wkt) as geofence_status, gf.geometry_wkt \ 
FROM vehicle_position_s vp \
LEFT JOIN geo_fence2_t gf \
ON (vp.truck_id = gf.vehicle_id)
PARTITION BY truck_id;
```

Create a KSQL Table which keeps the last value of the geofence_status for 20sec by truck_id

```
DROP TABLE vehicle_geofence_status_t;

CREATE TABLE vehicle_geofence_status_t \
WITH (kafka_topic='vehicle_geofence_status', \
        value_format='DELIMITED', \
        partitions=8) \ 
AS \
SELECT truck_id, LASTVALUE(geofence_status) last_status \
FROM vehicle_geofence_s \
WINDOW SESSION (20 SECONDS)  \
GROUP BY truck_id;
```

Create a KSQL Table which keeps the last value of the geofence_status  by truck_id

```
DROP TABLE vehicle_geofence_status_t;

CREATE TABLE vehicle_geofence_status_t \
WITH (kafka_topic='vehicle_geofence_status', \
        value_format='DELIMITED', \
        partitions=8) \ 
AS \
SELECT truck_id, LASTVALUE(geofence_status) last_status \
FROM vehicle_geofence_s \
GROUP BY truck_id;
```

Join the latest status to the last (previous) status in the table

```
SELECT vg.truck_id, vg.geofence_status, vgs.last_status
FROM vehicle_geofence_s vg
LEFT JOIN vehicle_geofence_status_t vgs
ON (vg.truck_id = vgs.truck_id);
```

```
CASE
   WHEN orderunits < 2.0 THEN 'small'
   WHEN orderunits < 4.0 THEN 'medium'
   ELSE 'large'
 END AS case_result
```


```
SELECT vg.truck_id, geo_fence(vp.latitude, vp.longitude, vgs.last_status, vp.geometry_wkt) status
FROM vehicle_position_s vp
LEFT JOIN vehicle_geofence_status_t vgs
ON (vg.truck_id = vgs.truck_id);
```


```
SELECT LASTVALUE( GEOFENCE(latitude,longitude,'POLYGON ((-90.23345947265625 38.484769753492536, -90.25886535644531 38.47455675836861, -90.25886535644531 38.438530965643004, -90.23826599121092 38.40356337960024, -90.19088745117188 38.39818224865764, -90.16685485839844 38.435841752321856, -90.16891479492188 38.47616943274547, -90.23345947265625 38.484769753492536))') ) \ 
FROM vehicle_position_s G
WINDOW SESSION (20 SECONDS)
GROUP BY truckId;
```



## Misc

```
GEOMETRYCOLLECTION(POINT (-90.21316232463931 38.44383114670336),POLYGON ((-90.23345947265625 38.484769753492536, -90.25886535644531 38.47455675836861, -90.25886535644531 38.438530965643004, -90.23826599121092 38.40356337960024, -90.19088745117188 38.39818224865764, -90.16685485839844 38.435841752321856, -90.16891479492188 38.47616943274547, -90.23345947265625 38.484769753492536)))
```

```
GEOMETRYCOLLECTION(POLYGON ((-90.23345947265625 38.484769753492536, -90.25886535644531 38.47455675836861, -90.25886535644531 38.438530965643004, -90.23826599121092 38.40356337960024, -90.19088745117188 38.39818224865764, -90.16685485839844 38.435841752321856, -90.16891479492188 38.47616943274547, -90.23345947265625 38.484769753492536)),
POLYGON ((-90.16685485839844 38.39818224865764, -90.16685485839844 38.48476975349254, -90.25886535644531 38.48476975349254, -90.25886535644531 38.39818224865764, -90.16685485839844 38.39818224865764)), 
LINESTRING (-90.15387 38.36086, -90.15398 38.36125, -90.15405 38.36154, -90.15407 38.36165, -90.1541 38.36192, -90.15393 38.36349, -90.15393 38.3636, -90.15392 38.36375, -90.15392 38.3639, -90.15393 38.36405, -90.15397 38.36438, -90.154 38.36456, -90.15403999999999 38.36472, -90.15411 38.36495, -90.15430000000001 38.36539, -90.15434 38.36546, -90.15438 38.36555, -90.15443999999999 38.36566, -90.1545 38.36575, -90.15456 38.36585, -90.15483999999999 38.36627, -90.15586 38.36767, -90.15591000000001 38.36775, -90.15629 38.36829, -90.15640999999999 38.36848, -90.15646 38.36855, -90.15658999999999 38.3688, -90.15657 38.36894, -90.15669 38.36922, -90.1568 38.36955, -90.15688 38.36986, -90.15692 38.37005, -90.15716 38.37141, -90.15724 38.37177, -90.15738 38.37231, -90.15748000000001 38.3726, -90.15755 38.37278, -90.15759 38.3729, -90.15804 38.3739, -90.15942 38.37714, -90.15958999999999 38.37751, -90.16063 38.37995, -90.16083999999999 38.38037, -90.16095 38.38057, -90.16125 38.38105, -90.16333 38.38412, -90.16357000000001 38.38449, -90.16382 38.38491, -90.16388999999999 38.38504, -90.16391 38.38509, -90.16414 38.38552, -90.16591 38.38912, -90.16710999999999 38.3915, -90.16727 38.39188, -90.16735 38.39209, -90.16739 38.39221, -90.16745 38.39242, -90.16757 38.39307, -90.16773000000001 38.39423, -90.16776 38.39437, -90.16784 38.39468, -90.16788 38.39479, -90.16791000000001 38.3949, -90.16817 38.39552, -90.16825 38.39569, -90.16829 38.39576, -90.16829 38.39578, -90.16903000000001 38.3974, -90.16916999999999 38.39774, -90.16924 38.39793, -90.16930000000001 38.39813, -90.16934000000001 38.39824, -90.16943000000001 38.39857, -90.16945 38.39867, -90.16947999999999 38.39878, -90.16952000000001 38.39899, -90.16952999999999 38.39911, -90.16954 38.39914, -90.1696 38.39978, -90.1696 38.40032, -90.16956 38.40077, -90.16951 38.40109, -90.16943999999999 38.40144, -90.16939000000001 38.40164, -90.16763 38.407, -90.16755999999999 38.40725, -90.16745 38.40771, -90.1674 38.40797, -90.16712 38.40982, -90.16705 38.41017, -90.16699 38.41055, -90.16698 38.41059, -90.16696 38.41073, -90.16694 38.41094, -90.16695 38.41127, -90.16697000000001 38.41149, -90.16701 38.41181, -90.16701999999999 38.41182, -90.16719000000001 38.41267, -90.16726 38.41298, -90.16736 38.4133, -90.16746999999999 38.4136, -90.16761 38.41391, -90.16775 38.41417, -90.16798 38.41454, -90.16811 38.41471, -90.16858999999999 38.4153, -90.16864 38.41537, -90.16979000000001 38.41673, -90.17043 38.41746, -90.17112 38.41818, -90.17144999999999 38.4185, -90.17319999999999 38.42032, -90.17359 38.4208, -90.17395999999999 38.42134, -90.17424 38.42183, -90.17435 38.42204, -90.17574999999999 38.42539, -90.17589 38.42568, -90.17626 38.42634, -90.17641 38.42656, -90.17694 38.42722, -90.1772 38.42752, -90.17738 38.4277, -90.17779 38.42808, -90.17825999999999 38.42846, -90.17854 38.42867, -90.17883999999999 38.42887, -90.17945 38.42923, -90.18073 38.42994, -90.18120999999999 38.43022, -90.18165999999999 38.43052, -90.18205 38.43081, -90.18272 38.43136, -90.18312 38.43171, -90.18365 38.43215, -90.18388 38.43231, -90.18409 38.43247, -90.18451 38.43275, -90.18455 38.43277, -90.18495 38.43302, -90.18499 38.43305, -90.18606 38.43362, -90.18879 38.43497, -90.18939 38.43525, -90.18976000000001 38.43544, -90.19078 38.43589, -90.19141999999999 38.43614, -90.19233 38.43643, -90.19286 38.43657, -90.19341 38.43669, -90.19395 38.43679, -90.19761 38.43735, -90.19959 38.43769, -90.2003 38.43784, -90.2038 38.43868, -90.20929 38.44005, -90.20976 38.44018, -90.2102 38.44032, -90.211 38.44062, -90.21172 38.44093, -90.2123 38.44123, -90.21257 38.44138, -90.21352 38.44198, -90.21409 38.4424, -90.21442999999999 38.44268, -90.21496 38.44317, -90.21528000000001 38.4435, -90.21559999999999 38.44386, -90.21568000000001 38.44396, -90.2158 38.44409, -90.21617000000001 38.44457, -90.21626999999999 38.44472, -90.21638 38.44486, -90.21684999999999 38.44554, -90.21723 38.44616, -90.21729999999999 38.4463, -90.21798 38.44747, -90.21847 38.44853, -90.21850000000001 38.44858, -90.21863999999999 38.44891, -90.21905 38.44999, -90.21933 38.45086, -90.21948999999999 38.45144, -90.22051 38.45552, -90.22059 38.4559, -90.22069 38.45632, -90.22078 38.45665, -90.22082 38.45683, -90.22111 38.45794, -90.2214 38.45888, -90.22167 38.45957, -90.22252 38.46163, -90.22264 38.4619, -90.223 38.46278, -90.22302000000001 38.46284, -90.22302999999999 38.46285, -90.22309 38.46301, -90.2231 38.46302, -90.22328 38.46347, -90.22369 38.46443, -90.22410000000001 38.46554, -90.22417 38.46579, -90.22423000000001 38.46595, -90.22432000000001 38.46625, -90.22445999999999 38.46678, -90.22451 38.467, -90.22454999999999 38.46715, -90.22495000000001 38.4692, -90.22499999999999 38.46934, -90.22521999999999 38.47024, -90.22532 38.4706, -90.22537 38.47075, -90.22546 38.47098, -90.22553000000001 38.47111, -90.22557999999999 38.47123, -90.22569 38.47144, -90.22578 38.47159, -90.22598000000001 38.47189, -90.22599 38.47191, -90.22614 38.47211, -90.22635 38.47237, -90.22659 38.47263, -90.22698 38.47296, -90.22709999999999 38.47304, -90.22727999999999 38.47318, -90.22750000000001 38.47334, -90.22759000000001 38.47339, -90.2277 38.47346, -90.22801 38.47364, -90.22845 38.47387, -90.22869 38.47397, -90.22895 38.47406, -90.2291 38.47412, -90.22984 38.47433, -90.22986 38.47434, -90.23025 38.47442, -90.23062 38.47448, -90.2313 38.47455, -90.23157 38.47455, -90.23188 38.47456, -90.23208 38.47456, -90.23263 38.47458, -90.23296999999999 38.47458, -90.23339 38.4746, -90.23414 38.47467, -90.23455 38.47473, -90.23553 38.47492, -90.23554 38.47492, -90.23577 38.47504, -90.23604 38.47511, -90.23618 38.47514, -90.24784 38.47859, -90.24805000000001 38.47866, -90.25381 38.48037, -90.25382999999999 38.48037, -90.25834 38.48172, -90.26107 38.4825, -90.26112000000001 38.48252, -90.265 38.48364, -90.26501 38.48364, -90.2667 38.48412, -90.26814 38.48459, -90.27489 38.48657, -90.27509999999999 38.48664, -90.27618 38.48695, -90.27896 38.48779, -90.27924 38.48789, -90.28066 38.48832, -90.28747 38.49019, -90.2876 38.49022, -90.28968999999999 38.4908, -90.28979 38.49082, -90.29071 38.49107, -90.29174 38.49137, -90.29174999999999 38.49138, -90.29213 38.49149, -90.29331000000001 38.49187, -90.30221 38.49495, -90.30354 38.49549, -90.31444999999999 38.50054, -90.31542 38.50094, -90.31582 38.50108, -90.31583000000001 38.50108, -90.31609 38.50117, -90.31695999999999 38.50141, -90.31753 38.50155, -90.31795 38.50163, -90.31822 38.50169, -90.31870000000001 38.50177, -90.31939 38.50186, -90.31998 38.50191, -90.32001 38.50192, -90.32861 38.50256, -90.33362 38.50298))
```
```
GEOMETRYCOLLECTION(LINESTRING (-90.15387 38.36086, -90.15398 38.36125, -90.15405 38.36154, -90.15407 38.36165, -90.1541 38.36192, -90.15393 38.36349, -90.15393 38.3636, -90.15392 38.36375, -90.15392 38.3639, -90.15393 38.36405, -90.15397 38.36438, -90.154 38.36456, -90.15403999999999 38.36472, -90.15411 38.36495, -90.15430000000001 38.36539, -90.15434 38.36546, -90.15438 38.36555, -90.15443999999999 38.36566, -90.1545 38.36575, -90.15456 38.36585, -90.15483999999999 38.36627, -90.15586 38.36767, -90.15591000000001 38.36775, -90.15629 38.36829, -90.15640999999999 38.36848, -90.15646 38.36855, -90.15658999999999 38.3688, -90.15657 38.36894, -90.15669 38.36922, -90.1568 38.36955, -90.15688 38.36986, -90.15692 38.37005, -90.15716 38.37141, -90.15724 38.37177, -90.15738 38.37231, -90.15748000000001 38.3726, -90.15755 38.37278, -90.15759 38.3729, -90.15804 38.3739, -90.15942 38.37714, -90.15958999999999 38.37751, -90.16063 38.37995, -90.16083999999999 38.38037, -90.16095 38.38057, -90.16125 38.38105, -90.16333 38.38412, -90.16357000000001 38.38449, -90.16382 38.38491, -90.16388999999999 38.38504, -90.16391 38.38509, -90.16414 38.38552, -90.16591 38.38912, -90.16710999999999 38.3915, -90.16727 38.39188, -90.16735 38.39209, -90.16739 38.39221, -90.16745 38.39242, -90.16757 38.39307, -90.16773000000001 38.39423, -90.16776 38.39437, -90.16784 38.39468, -90.16788 38.39479, -90.16791000000001 38.3949, -90.16817 38.39552, -90.16825 38.39569, -90.16829 38.39576, -90.16829 38.39578, -90.16903000000001 38.3974, -90.16916999999999 38.39774, -90.16924 38.39793, -90.16930000000001 38.39813, -90.16934000000001 38.39824, -90.16943000000001 38.39857, -90.16945 38.39867, -90.16947999999999 38.39878, -90.16952000000001 38.39899, -90.16952999999999 38.39911, -90.16954 38.39914, -90.1696 38.39978, -90.1696 38.40032, -90.16956 38.40077, -90.16951 38.40109, -90.16943999999999 38.40144, -90.16939000000001 38.40164, -90.16763 38.407, -90.16755999999999 38.40725, -90.16745 38.40771, -90.1674 38.40797, -90.16712 38.40982, -90.16705 38.41017, -90.16699 38.41055, -90.16698 38.41059, -90.16696 38.41073, -90.16694 38.41094, -90.16695 38.41127, -90.16697000000001 38.41149, -90.16701 38.41181, -90.16701999999999 38.41182, -90.16719000000001 38.41267, -90.16726 38.41298, -90.16736 38.4133, -90.16746999999999 38.4136, -90.16761 38.41391, -90.16775 38.41417, -90.16798 38.41454, -90.16811 38.41471, -90.16858999999999 38.4153, -90.16864 38.41537, -90.16979000000001 38.41673, -90.17043 38.41746, -90.17112 38.41818, -90.17144999999999 38.4185, -90.17319999999999 38.42032, -90.17359 38.4208, -90.17395999999999 38.42134, -90.17424 38.42183, -90.17435 38.42204, -90.17574999999999 38.42539, -90.17589 38.42568, -90.17626 38.42634, -90.17641 38.42656, -90.17694 38.42722, -90.1772 38.42752, -90.17738 38.4277, -90.17779 38.42808, -90.17825999999999 38.42846, -90.17854 38.42867, -90.17883999999999 38.42887, -90.17945 38.42923, -90.18073 38.42994, -90.18120999999999 38.43022, -90.18165999999999 38.43052, -90.18205 38.43081, -90.18272 38.43136, -90.18312 38.43171, -90.18365 38.43215, -90.18388 38.43231, -90.18409 38.43247, -90.18451 38.43275, -90.18455 38.43277, -90.18495 38.43302, -90.18499 38.43305, -90.18606 38.43362, -90.18879 38.43497, -90.18939 38.43525, -90.18976000000001 38.43544, -90.19078 38.43589, -90.19141999999999 38.43614, -90.19233 38.43643, -90.19286 38.43657, -90.19341 38.43669, -90.19395 38.43679, -90.19761 38.43735, -90.19959 38.43769, -90.2003 38.43784, -90.2038 38.43868, -90.20929 38.44005, -90.20976 38.44018, -90.2102 38.44032, -90.211 38.44062, -90.21172 38.44093, -90.2123 38.44123, -90.21257 38.44138, -90.21352 38.44198, -90.21409 38.4424, -90.21442999999999 38.44268, -90.21496 38.44317, -90.21528000000001 38.4435, -90.21559999999999 38.44386, -90.21568000000001 38.44396, -90.2158 38.44409, -90.21617000000001 38.44457, -90.21626999999999 38.44472, -90.21638 38.44486, -90.21684999999999 38.44554, -90.21723 38.44616, -90.21729999999999 38.4463, -90.21798 38.44747, -90.21847 38.44853, -90.21850000000001 38.44858, -90.21863999999999 38.44891, -90.21905 38.44999, -90.21933 38.45086, -90.21948999999999 38.45144, -90.22051 38.45552, -90.22059 38.4559, -90.22069 38.45632, -90.22078 38.45665, -90.22082 38.45683, -90.22111 38.45794, -90.2214 38.45888, -90.22167 38.45957, -90.22252 38.46163, -90.22264 38.4619, -90.223 38.46278, -90.22302000000001 38.46284, -90.22302999999999 38.46285, -90.22309 38.46301, -90.2231 38.46302, -90.22328 38.46347, -90.22369 38.46443, -90.22410000000001 38.46554, -90.22417 38.46579, -90.22423000000001 38.46595, -90.22432000000001 38.46625, -90.22445999999999 38.46678, -90.22451 38.467, -90.22454999999999 38.46715, -90.22495000000001 38.4692, -90.22499999999999 38.46934, -90.22521999999999 38.47024, -90.22532 38.4706, -90.22537 38.47075, -90.22546 38.47098, -90.22553000000001 38.47111, -90.22557999999999 38.47123, -90.22569 38.47144, -90.22578 38.47159, -90.22598000000001 38.47189, -90.22599 38.47191, -90.22614 38.47211, -90.22635 38.47237, -90.22659 38.47263, -90.22698 38.47296, -90.22709999999999 38.47304, -90.22727999999999 38.47318, -90.22750000000001 38.47334, -90.22759000000001 38.47339, -90.2277 38.47346, -90.22801 38.47364, -90.22845 38.47387, -90.22869 38.47397, -90.22895 38.47406, -90.2291 38.47412, -90.22984 38.47433, -90.22986 38.47434, -90.23025 38.47442, -90.23062 38.47448, -90.2313 38.47455, -90.23157 38.47455, -90.23188 38.47456, -90.23208 38.47456, -90.23263 38.47458, -90.23296999999999 38.47458, -90.23339 38.4746, -90.23414 38.47467, -90.23455 38.47473, -90.23553 38.47492, -90.23554 38.47492, -90.23577 38.47504, -90.23604 38.47511, -90.23618 38.47514, -90.24784 38.47859, -90.24805000000001 38.47866, -90.25381 38.48037, -90.25382999999999 38.48037, -90.25834 38.48172, -90.26107 38.4825, -90.26112000000001 38.48252, -90.265 38.48364, -90.26501 38.48364, -90.2667 38.48412, -90.26814 38.48459, -90.27489 38.48657, -90.27509999999999 38.48664, -90.27618 38.48695, -90.27896 38.48779, -90.27924 38.48789, -90.28066 38.48832, -90.28747 38.49019, -90.2876 38.49022, -90.28968999999999 38.4908, -90.28979 38.49082, -90.29071 38.49107, -90.29174 38.49137, -90.29174999999999 38.49138, -90.29213 38.49149, -90.29331000000001 38.49187, -90.30221 38.49495, -90.30354 38.49549, -90.31444999999999 38.50054, -90.31542 38.50094, -90.31582 38.50108, -90.31583000000001 38.50108, -90.31609 38.50117, -90.31695999999999 38.50141, -90.31753 38.50155, -90.31795 38.50163, -90.31822 38.50169, -90.31870000000001 38.50177, -90.31939 38.50186, -90.31998 38.50191, -90.32001 38.50192, -90.32861 38.50256, -90.33362 38.50298),
POLYGON ((-90.23345947265625 38.484769753492536, -90.25886535644531 38.47455675836861, -90.25886535644531 38.438530965643004, -90.23826599121092 38.40356337960024, -90.19088745117188 38.39818224865764, -90.16685485839844 38.435841752321856, -90.16891479492188 38.47616943274547, -90.23345947265625 38.484769753492536)),
POLYGON ((-90.23081789920012 38.50459453822468, -90.16627322146574 38.49599421747762, -90.1643249060413 38.49563563217073, -90.16242162335251 38.49508605831588, -90.16058204705975 38.49435088794208, -90.15882422578142 38.49343733401984, -90.15716540601365 38.49235435969258, -90.15562186292 38.49111259033661, -90.15420874065097 38.48972420931247, -90.15293990376043 38.48820283843047, -90.15182780117637 38.48656340430311, -90.15088334406093 38.48482199189576, -90.15011579875772 38.48299568671235, -90.14953269587713 38.48110240716457, -90.14913975641119 38.47916072876907, -90.14894083560338 38.47718970189771, -90.14688089907995 38.4368620214741, -90.14688203543342 38.43479947461972, -90.14709558779319 38.43274801265139, -90.14751928497606 38.43072945338423, -90.14814862085569 38.42876526470479, -90.14897690228641 38.42687633625475, -90.14999532028641 38.42508275726427, -90.17402791305985 38.38742325360005, -90.17520415524115 38.38577119597267, -90.17654165464239 38.38424675003237, -90.17802665896077 38.38286559028504, -90.1796438992331 38.38164191795357, -90.18137674683287 38.38058831495945, -90.18320738444747 38.37971561455424, -90.18511698927755 38.37903278993092, -90.18708592657492 38.37854686196069, -90.18909395152905 38.37826282700361, -90.19112041742619 38.37818360553556, -90.19314448794096 38.37831001211962, -90.24052302798 38.38369114306222, -90.24255068209438 38.38402773463947, -90.24453308264155 38.38457065559867, -90.24644929212599 38.38531417177816, -90.24827907214143 38.38625043039224, -90.25000309712276 38.38736954296976, -90.25160315845642 38.38865968979316, -90.25306235679425 38.39010724473452, -90.25436528053905 38.39169691917056, -90.25549816861708 38.39341192345628, -90.27609753385147 38.42837950949905, -90.27707876542273 38.43026829456027, -90.27785371536606 38.43225066107262, -90.27841360674486 38.43430415712761, -90.27875209833522 38.43640552521701, -90.27886535644531 38.438530965643, -90.27886535644531 38.47455675836861, -90.27876733209382 38.4765344755593, -90.27847421991667 38.4784928063055, -90.27798889312699 38.48041255419704, -90.27731610910917 38.48227490102974, -90.27646246278488 38.48406159126952, -90.27543632196688 38.48575511100117, -90.27424774533398 38.48733885960736, -90.27290838383156 38.48879731249509, -90.27143136646387 38.49011617327449, -90.2698311715979 38.49128251389831, -90.26812348504002 38.49228490138822, -90.26632504627699 38.49311350990586, -90.24091916248793 38.50332650502979, -90.23897285459638 38.50399480536716, -90.23696817736069 38.50445957297614, -90.23492635399681 38.50471588743204, -90.23286900098113 38.50476103517233, -90.23081789920012 38.50459453822468)))
```

## Setup GeoFences

```
"Columbia", "POLYGON ((-90.23345947265625 38.484769753492536, -90.25886535644531 38.47455675836861, -90.25886535644531 38.438530965643004, -90.23826599121092 38.40356337960024, -90.19088745117188 38.39818224865764, -90.16685485839844 38.435841752321856, -90.16891479492188 38.47616943274547, -90.23345947265625 38.484769753492536))"
"St. Louis", "POLYGON ((-90.31997680664062 38.74337300148123, -90.38589477539062 38.66942832560808, -90.35018920898438 38.613651383524335, -90.31448364257812 38.55460931253295, -90.17303466796875 38.55460931253295, -90.07965087890625 38.58359966761715, -90.05905151367186 38.634036452919226, -90.05905151367186 38.68658172716673, -90.0714111328125 38.72730457751627, -90.120849609375 38.74444410121548, -90.31997680664062 38.74337300148123))"
```


## Demo

DROP STREAM demo_geo_fence_s;

CREATE STREAM demo_geo_fence_s 
  (id VARCHAR,
   name VARCHAR,
   geometry_wkt VARCHAR)
  WITH (kafka_topic='geo_fence', 
        value_format='JSON',
        key='id');

```
DROP STREAM IF EXISTS demo_geo_fence_by_geohash_s DELETE TOPIC;

CREATE STREAM demo_geo_fence_by_geohash_s
WITH (PARTITIONS=8, kafka_topic='a04_geo_fence_by_geohash', value_format='JSON')
AS
SELECT geo_hash(geometry_wkt, 3)[0] geo_hash, id, name, geometry_wkt
FROM demo_geo_fence_s
PARTITION by geo_hash;
```

```
INSERT INTO demo_geo_fence_by_geohash_s
SELECT geo_hash(geometry_wkt, 3)[1] geo_hash, id, name, geometry_wkt
FROM demo_geo_fence_s
WHERE geo_hash(geometry_wkt, 3)[1] IS NOT NULL
PARTITION BY geo_hash;
```

```
INSERT INTO demo_geo_fence_by_geohash_s
SELECT geo_hash(geometry_wkt, 3)[2] geo_hash, id, name, geometry_wkt
FROM demo_geo_fence_s
WHERE geo_hash(geometry_wkt, 3)[2] IS NOT NULL
PARTITION BY geo_hash;
```

```
INSERT INTO a04_geo_fence_by_geohash_s
SELECT geo_hash(geometry_wkt, 3)[3] geo_hash, id, name, geometry_wkt
FROM a04_geo_fence_s
WHERE geo_hash(geometry_wkt, 3)[3] IS NOT NULL
PARTITION BY geo_hash;
```


Now we create a table which groups the geo-fences by geohash and creates a set with all geometries per geohash. Can be 1 to many, depending on how many geo-fence geometries belong to a given geohash. 

```
DROP TABLE IF EXISTS demo_geo_fence_by_geohash_t DELETE TOPIC;

CREATE TABLE demo_geo_fence_by_geohash_t
WITH (PARTITIONS=8, KAFKA_TOPIC='geo_fence_by_geohash_t', VALUE_FORMAT='JSON')
AS
SELECT geo_hash, COLLECT_SET(id + ':' + geometry_wkt) id_geometry_wkt_list, COLLECT_SET(geometry_wkt) geometry_wkt_list, COLLECT_SET(id) id_list
FROM demo_geo_fence_by_geohash_s
GROUP BY geo_hash;
```

Create a new stream `a04_vehicle_position_by_geohash_s` which enriches the vehicle positions with the geohash the LatLong belongs to ((currently using precision of 3, but can be increased or reduced upon use-case, but needs to be the same as above for the geo fences).

```
DROP STREAM IF EXISTS demo_vehicle_position_by_geohash_s DELETE TOPIC;

CREATE STREAM demo_vehicle_position_by_geohash_s
WITH (PARTITIONS=8, KAFKA_TOPIC='vehicle_position_by_geohash', value_format='AVRO')
AS
SELECT vp.id, vp.latitude, vp.longitude, geo_hash(vp.latitude, vp.longitude, 3) geo_hash
FROM vehicle_position_s vp
PARTITION BY geo_hash;
```

now call the geo_fence UDF

```
DROP STREAM demo_geo_fence_status_s DELETE TOPIC;

CREATE STREAM demo_geo_fence_status_s
WITH (PARTITIONS=8, KAFKA_TOPIC='demo_geo_fence_status', value_format='AVRO')
AS
SELECT vp.id, vp.latitude, vp.longitude, vp.geo_hash, gf.geometry_wkt_list,
geo_fence_bulk (vp.latitude, vp.longitude, gf.id_geometry_wkt_list) fence_status
FROM demo_vehicle_position_by_geohash_s vp \
LEFT JOIN demo_geo_fence_by_geohash_t gf \
ON (vp.geo_hash = gf.geo_hash);
```

