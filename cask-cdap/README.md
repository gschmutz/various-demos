### Cask CDAP

export DOCKER_HOST_IP=
export PUBLIC_HOST_IP=

```
export SAMPLE_HOME=/mnt/hgfs/git/gschmutz/various-demos/kylo
```

```
cd $SAMPLE_HOME/docker
```

```
docker-compose up -d
```

```
docker pull caskdata/cdap-sandbox
```

```
docker run -d --name cdap-sandbox -p 11011:11011 -p 11015:11015 caskdata/cdap-sandbox:latest
```

* CDAP UI: <http://cdap:11011>