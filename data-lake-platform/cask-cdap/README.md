### Cask CDAP

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