#!/bin/bash

cd ../data
rm -R kafka-4
rm -R kafka-5
rm -R kafka-6

rm -R zookeeper-4
rm -R zookeeper-5
rm -R zookeeper-6

mkdir kafka-4
mkdir kafka-5
mkdir kafka-6

mkdir -p zookeeper-4/data
mkdir -p zookeeper-5/data
mkdir -p zookeeper-6/data

mkdir -p zookeeper-4/log
mkdir -p zookeeper-5/log
mkdir -p zookeeper-6/log

echo 4 > zookeeper-4/data/myid
echo 5 > zookeeper-5/data/myid
echo 6 > zookeeper-6/data/myid

