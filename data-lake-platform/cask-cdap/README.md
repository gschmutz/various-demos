# Cask CDAP

[CDAP](http://cask.co/) is an open source, Apache 2.0 licensed, distributed, application framework for 
delivering Hadoop solutions. It integrates and abstracts the underlying Hadoop 
technologies to provide simple and easy-to-use APIs and a graphical UI to build, deploy, 
and manage complex data analytics applications in the cloud or on-premises.

CDAP provides a container architecture for your data and applications on Hadoop. Simplied 
abstractions and deep integrations with diverse Hadoop technologies dramatically increase 
productivity and quality. This accelerates development and reduces time-to-production to get 
your Hadoop projects to market faster. 

## Setup

```
export DOCKER_HOST_IP=nnn.nnn.nnn.nnnn
export PUBLIC_HOST_IP=
```

```
cd $VARIOUS_DEMOS/data-lake-platform/cask-cdap/docker
```

```
docker-compose up -d
```

to connect a terminal to the CDAP environment

```
docker exec -ti docker_cdap_1 bash

cd /
mkdir datalake
cd /datalake
mkdir poc 
```



* CDAP UI: <http://cdap:11011>