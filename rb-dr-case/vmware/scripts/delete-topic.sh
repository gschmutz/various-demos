#!/bin/bash

/home/gus/confluent-5.0.1/bin/kafka-topics --zookeeper zookeeper-1:2181, zookeeper-4:2185 --delete --topic sequence
