# Truck Position Simulator

```
-s MQTT | KAFKA
-p PORT
-f JSON | CSV | AVRO
-m COMBINE | SPLIT
-b <broker-ip>:[<port>]
```



```
mvn exec:java -Dexec.args="-s MQTT -p 1883 -f JSON -m COMBINE -t sec"
```
