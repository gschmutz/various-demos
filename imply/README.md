# Working with Imply

Documentation: <https://github.com/implydata/distribution-docker>

```
git clone https://github.com/implydata/distribution-docker.git
```

```
cd distribution-docker 
export implyversion=2.7.2
wget https://static.imply.io/release/imply-2.7.2.tar.gz
tar -xzf imply-$implyversion.tar.gz
rm *.tar.gz
docker build -t imply:$implyversion --build-arg implyversion=$implyversion .
```



```
docker run -p 28081-28110:8081-8110 -p 28200:8200 -p 29095:9095 -d --name imply imply:$implyversion
```

```
docker exec -it imply bin/post-index-task -f quickstart/wikipedia-index.json
```

```
docker exec -it imply /bin/bash
```

<http://streamingplatform:29095>
