#!/bin/bash

cd ../data

rm -R kafka-1
rm -R kafka-2
rm -R kafka-3

rm -R zookeeper-1 
rm -R zookeeper-2
rm -R zookeeper-3 

mkdir kafka-1
mkdir kafka-2
mkdir kafka-3

mkdir -p zookeeper-1/data
mkdir -p zookeeper-2/data
mkdir -p zookeeper-3/data


mkdir -p zookeeper-1/log
mkdir -p zookeeper-2/log
mkdir -p zookeeper-3/log


echo 1 > zookeeper-1/data/myid
echo 2 > zookeeper-2/data/myid
echo 3 > zookeeper-3/data/myid


