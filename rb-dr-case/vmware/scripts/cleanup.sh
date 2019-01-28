#!/bin/bash

cd ../data
rm -R *
mkdir kafka-1
mkdir kafka-2
mkdir kafka-3
mkdir kafka-4
mkdir kafka-5
mkdir kafka-6

mkdir -p zookeeper-1/data
mkdir -p zookeeper-2/data
mkdir -p zookeeper-3/data
mkdir -p zookeeper-4/data
mkdir -p zookeeper-5/data
mkdir -p zookeeper-6/data

mkdir -p zookeeper-1/log
mkdir -p zookeeper-2/log
mkdir -p zookeeper-3/log
mkdir -p zookeeper-4/log
mkdir -p zookeeper-5/log
mkdir -p zookeeper-6/log

echo 1 > zookeeper-1/data/myid
echo 2 > zookeeper-2/data/myid
echo 3 > zookeeper-3/data/myid
echo 4 > zookeeper-4/data/myid
echo 5 > zookeeper-5/data/myid
echo 6 > zookeeper-6/data/myid

