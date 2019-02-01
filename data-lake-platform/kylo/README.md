# Kylo

[Kylo](http://kylo.io) is a data lake platform built on Apache Hadoop and Apache Spark. Kylo provides a business-friendly data lake solution and enables self-service data ingestion, data wrangling, data profiling, data validation, data cleansing/standardization, and data discovery. Its intuitive user interface allows IT professionals to access the data lake (without having to code).

## Setup
In order to get a Kylo sandbox environment, eihter a virtual machine can be downloaded from the Kylo website or the Kylo sandbox can be provisioned on AWS: <https://kylo.io/quickstart.html>.

The following is based on a local virtual machine, downloaded and imported into VMWare Fusion.

You can login to the sandbox using user `root` and password `kylo`. Be aware that the keyboard is US! See below how to change it. 


### Change Keyboard to Swiss-German

```
yum install kbd

localectl set-keymap ch-de_mac
```

### Change /etc/hosts
Add the IP address as `kylo` to the `/etc/hosts` file.

You can get the address using

```
ip addr
```

### Configure HDFS to allow admin user

Configure proxy user for user admin

In Ambari click on **Services** | **HDFS**. Navigate to tab **Configs** and then **Advanced**. 
Jump forward to **Custom core-site** and add the following two properties:

```
hadoop.proxyuser.root.groups=*
hadoop.proxyuser.root.hosts=*
```
 
Now create the folder /user/admin

```
hadoop fs -mkdir /user/admin
hadoop fs -chmod +777 /user/admin
```

and both the File View and Hive view in Abmari should work.

## Using Kylo

* Kylo UI: <http://kylo:8400> login: dladmin/thinkbig
* Kylo NiFi: <http://kylo:8079/nifi/>
* API Doc: <http://kylo:8400/api-docs/index.html>
* Ambari UI: <http://kylo:8080> - login admin/admin
* ActiveMQ Admin: <http:kylo:8161>


Create a folder ini the dropzone

```
cd /var/dropzone
mkdir airplane

chmod nifi:hdfs airplane
```

Drop the files here

```
scp flights.csv root@kylo:/var/dropzone/airplane
scp airports.csv root@kylo:/var/dropzone/airplane
scp airlines.csv root@kylo:/var/dropzone/airplane
```
