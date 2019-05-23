# Event Sourcing Sample using Axon Framework

## Prepare environment

### Create necessary Kafka topics
```
docker exec -ti broker-1 kafka-topics --zookeeper zookeeper-1:2181 --create --topic account-v1 --replication-factor 3 --partitions 3
```

### Start services
```
cd finance-axon-discovery
mvn spring-boot:run
```

```
cd finance-axon-command
mvn spring-boot:run
```

```
cd finance-axon-query
mvn spring-boot:run
```

## Work with the system

<http://localhost:9090/ui>



Create a new account

```
curl -X POST -H 'Content-Type: application/json' -i http://analyticsplatform:8080/api/accounts --data '{
  "id": "abc983",
  "forCustomerId": "983",
  "accountType": "Savings"
}'
```

Deposit some money

```
curl -X PUT -H 'Content-Type: application/json' -i http://analyticsplatform:8080/api/deposit/abc983 --data '{
  "id": "abc983",
  "amount": "200"
}'
```


