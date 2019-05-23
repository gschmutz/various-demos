# Event Sourcing

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


